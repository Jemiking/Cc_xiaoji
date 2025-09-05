# Android Compiler MCP Wrapper for WSL (PowerShell版本)
# 提供更好的错误处理和路径转换

param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$Arguments
)

# 检查WSL是否可用
try {
    $wslVersion = wsl --version 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Error "WSL未安装或不可用"
        exit 1
    }
} catch {
    Write-Error "无法检测WSL: $_"
    exit 1
}

# 检查Node.js是否在WSL中可用
$nodeCheck = wsl -d Ubuntu -- test -f /home/ua/.nvm/versions/node/v22.17.0/bin/node
if ($LASTEXITCODE -ne 0) {
    Write-Error "Node.js未在WSL中找到: /home/ua/.nvm/versions/node/v22.17.0/bin/node"
    Write-Host "请确保在WSL中安装了Node.js v22.17.0"
    exit 1
}

# 检查android-compiler-mcp是否存在
$mcpCheck = wsl -d Ubuntu -- test -f /home/ua/android-compiler-mcp/index.js
if ($LASTEXITCODE -ne 0) {
    Write-Error "android-compiler-mcp未找到: /home/ua/android-compiler-mcp/index.js"
    exit 1
}

# 转换Windows路径为WSL路径
function Convert-WindowsPathToWSL {
    param([string]$Path)
    
    if ($Path -match '^([A-Za-z]):\\(.*)') {
        $drive = $Matches[1].ToLower()
        $remainingPath = $Matches[2] -replace '\\', '/'
        return "/mnt/$drive/$remainingPath"
    }
    return $Path
}

# 处理参数
$convertedArgs = @()
foreach ($arg in $Arguments) {
    # 检查是否是Windows路径
    if ($arg -match '^[A-Za-z]:\\') {
        $convertedArgs += Convert-WindowsPathToWSL $arg
    } else {
        $convertedArgs += $arg
    }
}

# 构建命令
$wslCommand = @(
    '/home/ua/.nvm/versions/node/v22.17.0/bin/node',
    '/home/ua/android-compiler-mcp/index.js'
) + $convertedArgs

# 执行命令
Write-Host "执行WSL命令: wsl -d Ubuntu -- $($wslCommand -join ' ')" -ForegroundColor Cyan
try {
    wsl -d Ubuntu -- $wslCommand
    exit $LASTEXITCODE
} catch {
    Write-Error "执行失败: $_"
    exit 1
}