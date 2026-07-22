#!/bin/bash
# 版本升级便捷脚本
# 用法: ./scripts/bump-version.sh <版本号>
# 示例: ./scripts/bump-version.sh 2.5.3

set -e
cd "$(dirname "$0")/.."
node scripts/bump-version.js "$@"
