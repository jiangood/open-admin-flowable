/**
 * 版本升级脚本 — 同时升级前后端所有相关文件
 *
 * 用法: node scripts/bump-version.js <新版本号>
 * 示例: node scripts/bump-version.js 2.5.3
 *
 * 通用设计：修改下方 FILE_GROUPS 即可适配任意项目。
 * 每个文件组指定文件列表和替换模式，支持三种内置模式：
 *   - "pom-root"   — Maven 根 POM：跳过 <parent> 块，只改项目自身的 <version>
 *   - "pom-module" — Maven 子模块：只改 <parent> 块内的 <version>
 *   - "package"    — 前端 package.json：改 "version" 字段
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const VERSION_REGEX = /^\d+\.\d+\.\d+$/;

// ======================== 在此处配置要更新的文件 ========================
const FILE_GROUPS = [
  {
    pattern: 'pom-root',
    files: ['pom.xml'],
    desc: 'Maven 根 POM',
  },
  {
    pattern: 'pom-module',
    files: ['open-admin-flowable/pom.xml', 'open-admin-flowable-example/pom.xml'],
    desc: 'Maven 子模块 POM',
  },
  {
    pattern: 'package',
    files: ['web/package.json'],
    desc: '前端 package.json',
  },
];
// =====================================================================

/** 替换策略 */
const REPLACERS = {
  /**
   * Maven 根 POM：跳过 <parent>…</parent>，只替换项目自身的 <version>。
   * 适配任意项目，不会误改 Spring Boot / 父 POM 版本。
   */
  'pom-root': (content, version) => {
    const parentMatch = content.match(/<parent>[\s\S]*?<\/parent>/);
    if (parentMatch) {
      const placeholder = `<!--__PARENT_BLOCK_${Date.now()}__-->`;
      const withoutParent = content.replace(parentMatch[0], placeholder);
      const updated = withoutParent.replace(
        /(<version>)\d+\.\d+\.\d+(<\/version>)/,
        `$1${version}$2`,
      );
      return updated.replace(placeholder, parentMatch[0]);
    }
    // 没有 parent 块则直接替换第一个 <version>
    return content.replace(
      /(<version>)\d+\.\d+\.\d+(<\/version>)/,
      `$1${version}$2`,
    );
  },

  /**
   * Maven 子模块 POM：只替换 <parent> 块内的 <version>。
   * 避免误改依赖项中写死的版本号。
   */
  'pom-module': (content, version) =>
    content.replace(
      /(<parent>[\s\S]*?<version>)\d+\.\d+\.\d+(<\/version>[\s\S]*?<\/parent>)/,
      `$1${version}$2`,
    ),

  /** 前端 package.json */
  package: (content, version) =>
    content.replace(
      /("version":\s*")\d+\.\d+\.\d+(")/,
      `$1${version}$2`,
    ),
};

function main() {
  const newVersion = process.argv[2];

  if (!newVersion) {
    console.error('❌ 请提供版本号，例如: node scripts/bump-version.js 2.5.3');
    process.exit(1);
  }

  if (!VERSION_REGEX.test(newVersion)) {
    console.error('❌ 版本号格式错误，应为 x.y.z 格式（如 2.5.3）');
    process.exit(1);
  }

  console.log(`\n🚀 开始升级版本至 v${newVersion}\n`);

  let updatedCount = 0;

  for (const group of FILE_GROUPS) {
    const replace = REPLACERS[group.pattern];
    if (!replace) {
      console.warn(`⚠️  未定义的替换模式 "${group.pattern}"，跳过`);
      continue;
    }

    for (const filePath of group.files) {
      const absPath = path.resolve(ROOT, filePath);

      if (!fs.existsSync(absPath)) {
        console.warn(`⚠️  文件不存在，跳过: ${filePath}`);
        continue;
      }

      const originalContent = fs.readFileSync(absPath, 'utf-8');
      const newContent = replace(originalContent, newVersion);

      if (originalContent === newContent) {
        console.warn(`⚠️  未能找到版本号，跳过: ${filePath}`);
        continue;
      }

      fs.writeFileSync(absPath, newContent, 'utf-8');
      console.log(`✅ 已更新: ${group.desc} (${filePath})`);
      updatedCount++;
    }
  }

  console.log(`\n🎉 升级完成！共更新 ${updatedCount} 个文件至 v${newVersion}\n`);
}

main();
