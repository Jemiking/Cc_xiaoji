# Update Claude Code MCP configuration to Windows native
Write-Host "========================================"
Write-Host "Update Claude Code MCP Configuration"
Write-Host "========================================"
Write-Host ""

Write-Host "[1] Removing old WSL configuration..."
claude mcp remove android-compiler -s user 2>$null
claude mcp remove android-compiler -s project 2>$null
claude mcp remove android-compiler -s local 2>$null

Write-Host ""
Write-Host "[2] Adding new Windows native configuration..."
Set-Location -Path "D:\kotlin\Cc_xiaoji"
claude mcp add android-compiler -s user -- node "D:/kotlin/Cc_xiaoji/android-compiler-mcp-windows/index.js"

Write-Host ""
Write-Host "[3] Listing current MCP configuration..."
claude mcp list

Write-Host ""
Write-Host "========================================"
Write-Host "Configuration update complete!"
Write-Host ""
Write-Host "Please follow these steps:"
Write-Host "1. Exit current Claude Code session (type exit)"
Write-Host "2. Restart Claude Code"
Write-Host "3. MCP server should now run natively on Windows"
Write-Host "========================================"