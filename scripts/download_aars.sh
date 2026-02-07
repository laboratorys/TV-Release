#!/bin/bash
set -e  # 遇到错误立即退出

REPO="laboratorys/Media3-Release"
TAG="v1.9.1-fongmi"
TEMP_DIR="temp_aars"
TARGET_DIR="app/libs"

echo "📦 开始下载 $REPO 的 AAR 文件 (Tag: $TAG)..."

# 创建目录
mkdir -p "$TEMP_DIR"
mkdir -p "$TARGET_DIR"

# 使用 GitHub API 获取所有 AAR 文件的下载链接
AAR_URLS=$(curl -s "https://api.github.com/repos/$REPO/releases/tags/$TAG" | \
  jq -r '.assets[] | select(.name | endswith(".aar")) | .browser_download_url')

if [ -z "$AAR_URLS" ]; then
  echo "❌ 未找到任何 AAR 文件，请检查 Tag 或仓库权限"
  exit 1
fi

# 下载所有 AAR 文件
echo "🔍 找到以下 AAR 文件："
echo "$AAR_URLS" | while read -r url; do
  filename=$(basename "$url")
  echo "📥 下载 $filename..."
  wget -q "$url" -P "$TEMP_DIR" || { echo "❌ 下载失败: $filename"; exit 1; }
done

# 移动文件到目标目录
echo "🚚 移动 AAR 文件到 $TARGET_DIR..."
mv "$TEMP_DIR"/*.aar "$TARGET_DIR/" || { echo "❌ 移动文件失败"; exit 1; }

# 清理临时目录
rm -rf "$TEMP_DIR"

echo "✅ 所有 AAR 文件已成功下载并移动到 $TARGET_DIR"
