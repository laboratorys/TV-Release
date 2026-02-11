# 1. 设置基础信息
$repoUrl = "https://github.com/laboratorys/Media3-Release/releases/download"
$version = "v1.9.1-fongmi"
$fileList = "move.txt"
$outputDir = "./downloads"

# 2. 创建下载目录
if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir
}

# 3. 读取文件并循环下载
if (Test-Path $fileList) {
    Get-Content $fileList | ForEach-Object {
        $fileName = $_.Trim()
        if ($fileName -ne "") {
            $downloadUrl = "$repoUrl/$version/$fileName"
            $outputPath = Join-Path $outputDir $fileName
            
            Write-Host "正在下载: $fileName ..." -ForegroundColor Cyan
            
            try {
                Invoke-WebRequest -Uri $downloadUrl -OutFile $outputPath -ErrorAction Stop
                Write-Host "下载成功: $fileName" -ForegroundColor Green
            } catch {
                Write-Host "下载失败: $fileName (请检查版本号或网络)" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host "错误: 找不到 $fileList" -ForegroundColor Yellow
}

Write-Host "所有任务处理完毕！" -ForegroundColor White