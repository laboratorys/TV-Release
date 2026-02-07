#!/bin/bash

# 参数说明：
# $1: Version Name (来自 steps.version.outputs.version，例如 5.1.7)
# $2: Default Description
VERSION=$1
DEFAULT_DESC=$2
BASE_URL="https://raw.githubusercontent.com/laboratorys/TV-Release/refs/heads/main/update"

# 提取数字作为 code (如 5.1.7 -> 517)
VERSION_CODE=$(echo "$VERSION" | sed 's/\.//g')

echo "🚀 Starting JSON process for Version: $VERSION (Code: $VERSION_CODE)"

process_file() {
    local filename=$1
    local output="processed_json/$filename"
    local temp_input="temp_$filename"

    mkdir -p "$(dirname "$output")"

    # 默认的对象模板
    local DEFAULT_JSON="{\"code\": $VERSION_CODE, \"name\": \"$VERSION\", \"desc\": \"$DEFAULT_DESC\"}"

    # 1. 尝试从远程 main 分支下载最新的 JSON
    echo "📡 Downloading $filename from main branch..."
    if curl -fsSL "$BASE_URL/$filename" -o "$temp_input"; then
        # 2. 如果下载成功且文件不为空，使用 jq 尝试匹配
        if [ -s "$temp_input" ]; then
            RESULT=$(jq -c --arg VERSION "$VERSION" 'map(select(.name == $VERSION)) | .[0]' "$temp_input" 2>/dev/null)

            if [ "$RESULT" != "null" ] && [ -n "$RESULT" ]; then
                echo "$RESULT" > "$output"
                echo "✅ Matched version $VERSION in remote $filename"
            else
                echo "$DEFAULT_JSON" > "$output"
                echo "⚠️ Version $VERSION not found in remote $filename, using default object."
            fi
        else
            echo "$DEFAULT_JSON" > "$output"
            echo "⚠️ Remote $filename is empty, using default object."
        fi
    else
        # 3. 下载失败（例如 404 或网络问题），直接生成默认
        echo "$DEFAULT_JSON" > "$output"
        echo "ℹ️ Could not download $filename (404 or network error), generated default JSON."
    fi

    # 清理临时文件
    rm -f "$temp_input"
}

# 执行处理
process_file "leanback.json"
process_file "mobile.json"

echo "🎉 All JSON files processed."