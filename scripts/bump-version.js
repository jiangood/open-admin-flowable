/**
 * 版本升级脚本 — 自动发现 pom.xml，一键升级全部版本
 *
 * 用法: node scripts/bump-version.js <新版本号>
 * 示例: node scripts/bump-version.js 2.5.3
 *
 * 特性:
 *   - 自动递归扫描所有 pom.xml（排除 target、node_modules 等目录）
 *   - 自动识别根 POM vs 子模块 POM，采用不同替换策略
 *   - 无论 pom.xml 层级多深，都安全替换，不误改 parent 或 dependency 的版本
 *   - 同时升级 web/package.json（如存在）
 */

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const VERSION_REGEX = /^\d+\.\d+\.\d+$/;

// package.json 路径（如无前端可删此行）
const PACKAGE_JSON_PATH = 'web/package.json';

// 扫描时排除的目录
const EXCLUDE_DIRS = new Set([
  'node_modules', '.git', '.svn', 'target', '.mvn',
  'dist', 'build', '.gradle',
]);

/* =================================================================
 * 替换策略（自动判断）
 *
 * 对于每个 pom.xml：
 *   1. 临时移除 <parent>…</parent> 块
 *   2. 如果剩余内容还有 <version>x.y.z</version>
 *      → 视为"根 POM"，在外层替换第一个 <version>
 *       （<parent> 块的版本被完整保留，绝不误改）
 *   3. 如果剩余内容没有 <version>
 *      → 视为"子模块 POM"，只替换 <parent> 块内的 <version>
 *      （防止误改 dependency 中写死的版本）
 * ================================================================= */
function replacePom(content, version) {
  const parentMatch = content.match(/<parent>[\s\S]*?<\/parent>/);

  if (!parentMatch) {
    // 没有 parent 块，直接替换第一个版本号
    return content.replace(
      /(<version>)\d+\.\d+\.\d+(<\/version>)/,
      `$1${version}$2`,
    );
  }

  // 检查 <parent> 块外是否还有 <version>
  const withoutParent = content.replace(parentMatch[0], '');

  if (/<version>\d+\.\d+\.\d+<\/version>/.test(withoutParent)) {
    // 根 POM：跳过 parent 块，替换外层版本
    const placeholder = '<!--__PARENT_BLOCK__-->';
    const result = content
      .replace(parentMatch[0], placeholder)
      .replace(
        /(<version>)\d+\.\d+\.\d+(<\/version>)/,
        `$1${version}$2`,
      )
      .replace(placeholder, parentMatch[0]);
    return result;
  } else {
    // 子模块 POM：只替换 parent 块内的版本
    return content.replace(
      /(<parent>[\s\S]*?<version>)\d+\.\d+\.\d+(<\/version>[\s\S]*?<\/parent>)/,
      `$1${version}$2`,
    );
  }
}

function replacePackageJson(content, version) {
  return content.replace(
    /("version":\s*")\d+\.\d+\.\d+(")/,
    `$1${version}$2`,
  );
}

/** 递归查找所有 pom.xml */
function findPomFiles(dir) {
  const results = [];

  let entries;
  try {
    entries = fs.readdirSync(dir, { withFileTypes: true });
  } catch {
    return results;
  }

  for (const entry of entries) {
    if (EXCLUDE_DIRS.has(entry.name)) continue;
    const fullPath = path.join(dir, entry.name);
    if (fullPath === __filename) continue; // 脚本自身所在目录
    if (entry.isDirectory()) {
      results.push(...findPomFiles(fullPath));
    } else if (entry.name === 'pom.xml') {
      results.push(fullPath);
    }
  }

  return results;
}

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

  // ---------- pom.xml（自动扫描） ----------
  const pomFiles = findPomFiles(ROOT);
  if (pomFiles.length === 0) {
    console.warn('⚠️  未找到任何 pom.xml 文件');
  } else {
    for (const absPath of pomFiles) {
      const content = fs.readFileSync(absPath, 'utf-8');
      const newContent = replacePom(content, newVersion);

      if (content === newContent) {
        console.warn(`⚠️  未能找到版本号，跳过: ${path.relative(ROOT, absPath)}`);
        continue;
      }

      fs.writeFileSync(absPath, newContent, 'utf-8');
      console.log(`✅ pom.xml: ${path.relative(ROOT, absPath)}`);
      updatedCount++;
    }
  }

  // ---------- package.json（固定路径） ----------
  const pkgPath = path.resolve(ROOT, PACKAGE_JSON_PATH);
  if (fs.existsSync(pkgPath)) {
    const content = fs.readFileSync(pkgPath, 'utf-8');
    const newContent = replacePackageJson(content, newVersion);

    if (content !== newContent) {
      fs.writeFileSync(pkgPath, newContent, 'utf-8');
      console.log(`✅ package.json: ${PACKAGE_JSON_PATH}`);
      updatedCount++;
    } else {
      console.warn(`⚠️  未能找到版本号，跳过: ${PACKAGE_JSON_PATH}`);
    }
  }

  console.log(`\n🎉 升级完成！共更新 ${updatedCount} 个文件至 v${newVersion}\n`);
}

main();
