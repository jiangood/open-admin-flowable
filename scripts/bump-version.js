/**
 * 版本升级脚本 — 自动发现 pom.xml，一键升级全部版本
 *
 * 用法: node scripts/bump-version.js <新版本号>
 * 示例: node scripts/bump-version.js 2.5.3
 *
 * 特性:
 *   - 自动递归扫描所有 pom.xml（排除 target、node_modules 等目录）
 *   - 从根 POM 的 <modules> 字段确定子模块列表，精准匹配替换策略
 *   - 根 POM 替换项目自身的 <version>，保留 <parent> 块不变
 *   - 子模块 POM 只替换 <parent> 块内的 <version>，不改其他任何版本
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

// 根 POM 文件名，用于识别
const ROOT_POM = 'pom.xml';

/* =================================================================
 * 从根 POM 中解析 <modules> 字段，获取子模块目录名
 * 返回 Set，例如：Set { 'open-admin-flowable', 'open-admin-flowable-example' }
 * ================================================================= */
function getChildModuleDirs(rootPomContent) {
  const modulesMatch = rootPomContent.match(/<modules>([\s\S]*?)<\/modules>/);
  if (!modulesMatch) return new Set();

  const moduleRegex = /<module>(.*?)<\/module>/g;
  const dirs = new Set();
  let m;
  while ((m = moduleRegex.exec(modulesMatch[1])) !== null) {
    dirs.add(m[1].trim());
  }
  return dirs;
}

/* =================================================================
 * 替换根 POM 的项目版本号
 *
 * 根 POM 特征：有 <parent> 块（如 Spring Boot parent），同时有自己独立的 <version>
 * 策略：跳过 <parent> 块，只替换项目自身的第一个 <version>
 * 场景：根 pom.xml
 * ================================================================= */
function replaceRootPomVersion(content, version) {
  const parentMatch = content.match(/<parent>[\s\S]*?<\/parent>/);
  if (!parentMatch) {
    // 没有 parent 块，直接替换第一个版本号
    return content.replace(
      /(<version>)\d+\.\d+\.\d+(<\/version>)/,
      `$1${version}$2`,
    );
  }

  const placeholder = '<!--__PARENT_BLOCK__-->';
  return content
    .replace(parentMatch[0], placeholder)
    .replace(
      /(<version>)\d+\.\d+\.\d+(<\/version>)/,
      `$1${version}$2`,
    )
    .replace(placeholder, parentMatch[0]);
}

/* =================================================================
 * 替换子模块 POM 的版本号
 *
 * 子模块特征：有 <parent> 块指向项目根 POM，无自己独立的 <version>
 * 策略：只替换 <parent> 块内的 <version>，其他任何地方都不改
 * 场景：open-admin-flowable/pom.xml、open-admin-flowable-example/pom.xml
 * ================================================================= */
function replaceChildModuleVersion(content, version) {
  return content.replace(
    /(<parent>[\s\S]*?<version>)\d+\.\d+\.\d+(<\/version>[\s\S]*?<\/parent>)/,
    `$1${version}$2`,
  );
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
    if (fullPath === __filename) continue;
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

  // ---------- 读取根 POM，获取子模块列表 ----------
  const rootPomPath = path.join(ROOT, ROOT_POM);
  let rootPomContent;
  try {
    rootPomContent = fs.readFileSync(rootPomPath, 'utf-8');
  } catch {
    console.error(`❌ 未找到根 POM: ${ROOT_POM}`);
    process.exit(1);
  }
  const childModuleDirs = getChildModuleDirs(rootPomContent);
  console.log(`📦 发现 ${childModuleDirs.size} 个子模块: ${[...childModuleDirs].join(', ') || '无'}\n`);

  // ---------- pom.xml（自动扫描） ----------
  const pomFiles = findPomFiles(ROOT);
  let updatedCount = 0;

  if (pomFiles.length === 0) {
    console.warn('⚠️  未找到任何 pom.xml 文件');
  } else {
    for (const absPath of pomFiles) {
      const content = fs.readFileSync(absPath, 'utf-8');
      const relPath = path.relative(ROOT, absPath);

      // 根据 POM 类型选择替换函数
      let type;
      let newContent;

      if (relPath === ROOT_POM) {
        // 根 POM
        type = '根 POM';
        newContent = replaceRootPomVersion(content, newVersion);
      } else if (childModuleDirs.has(path.dirname(relPath))) {
        // 子模块 POM（目录名匹配 <modules> 中的声明）
        type = '子模块';
        newContent = replaceChildModuleVersion(content, newVersion);
      } else {
        // 其他 pom.xml（如外部依赖或嵌套项目），按根 POM 方式处理
        type = '其他';
        newContent = replaceRootPomVersion(content, newVersion);
      }

      if (content === newContent) {
        console.warn(`⚠️  [${type}] 未能找到版本号，跳过: ${relPath}`);
        continue;
      }

      fs.writeFileSync(absPath, newContent, 'utf-8');
      console.log(`✅ [${type}] ${relPath}`);
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
