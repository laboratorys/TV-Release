#!/bin/bash

# 参数说明：
# $1: Version Name (来自 steps.version.outputs.version，例如 5.1.7)
# $2: Default Description
VERSION=$1
DEFAULT_DESC=$2

# 提取数字作为 code (如 5.1.7 -> 517)
VERSION_CODE=$(echo "$VERSION" | sed 's/\.//g')

echo "🚀 Starting JSON process for Version: $VERSION (Code: $VERSION_CODE)"

process_file() {
    local input=$1
    local output=$2

    mkdir -p "$(dirname "$output")"

    # 默认的对象模板
    local DEFAULT_JSON="{\"code\": $VERSION_CODE, \"name\": \"$VERSION\", \"desc\": \"$DEFAULT_DESC\"}"

    # 逻辑判断：
    # 1. 检查文件是否存在且不为空
    if [ -f "$input" ] && [ -s "$input" ]; then
        # 2. 使用 jq 尝试匹配。如果结果为 null，则输出我们准备好的 DEFAULT_JSON
        RESULT=$(jq -c --arg VERSION "$VERSION" 'map(select(.name == $VERSION)) | .[0]' "$input")

        if [ "$RESULT" != "null" ] && [ -n "$RESULT" ]; then
            echo "$RESULT" > "$output"
            echo "✅ Matched version $VERSION in $input"
        else
            echo "$DEFAULT_JSON" > "$output"
            echo "⚠️ Version $VERSION not found in $input, using default object."
        fi
    else
        # 3. 文件不存在或为空，直接生成默认
        echo "$DEFAULT_JSON" > "$output"
        echo "ℹ️ $input missing or empty, generated default JSON."
    fi
}

# 执行处理
process_file "update/leanback.json" "processed_json/leanback.json"
process_file "update/mobile.json" "processed_json/mobile.json"

echo "🎉 All JSON files processed."