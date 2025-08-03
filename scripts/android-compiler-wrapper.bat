@echo off
REM Android Compiler MCP Wrapper for WSL
REM This script properly invokes the android-compiler-mcp from WSL

setlocal enabledelayedexpansion

REM 转换参数中的Windows路径为WSL路径
set "args="
for %%i in (%*) do (
    set "arg=%%i"
    REM 检查是否是Windows路径格式
    if "!arg:~1,1!"==":" (
        REM 转换Windows路径为WSL路径
        set "drive=!arg:~0,1!"
        set "path=!arg:~2!"
        set "path=!path:\=/!"
        set "arg=/mnt/!drive!!path!"
    )
    set "args=!args! !arg!"
)

REM 使用完整的Node路径并传递转换后的参数
wsl -d Ubuntu -- /home/ua/.nvm/versions/node/v22.17.0/bin/node /home/ua/android-compiler-mcp/index.js !args!