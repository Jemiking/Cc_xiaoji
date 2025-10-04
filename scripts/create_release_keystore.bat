@echo off
echo ================================
echo CC小记 - 创建发布版本密钥库
echo ================================
echo.

set /p alias_name="请输入密钥别名 (建议: ccxiaoji_release): "
if "%alias_name%"=="" set alias_name=ccxiaoji_release

set /p keystore_password="请输入密钥库密码 (至少6位): "
set /p key_password="请输入密钥密码 (至少6位): "
set /p validity="请输入有效期(年数，建议25): "
if "%validity%"=="" set validity=25

echo.
echo 生成密钥库...
set SECRETS_DIR=%~dp0..\tools\secrets
if not exist "%SECRETS_DIR%" mkdir "%SECRETS_DIR%"
set KEYSTORE_PATH=%SECRETS_DIR%\ccxiaoji_release.keystore
echo 位置: %KEYSTORE_PATH%

keytool -genkey -v -keystore "%KEYSTORE_PATH%" -alias %alias_name% -keyalg RSA -keysize 2048 -validity %validity%0 -keypass %key_password% -storepass %keystore_password%

if %errorlevel% equ 0 (
    echo.
    echo ================================
    echo 密钥库创建成功！
    echo ================================
    echo 文件位置: %KEYSTORE_PATH%
    echo 密钥别名: %alias_name%
    echo.
    echo 重要提醒：
    echo 1. 请妥善保管密钥库文件和密码
    echo 2. 密钥库丢失将无法更新应用
    echo 3. 不要将密钥库提交到版本控制系统
    echo.
    echo 下一步：
    echo   1) 复制 keystore.properties.example 为 keystore.properties 到 tools\secrets\
    echo   2) 编辑 tools\secrets\keystore.properties 并填写 storeFile/keyAlias/keyPassword/storePassword
    echo      或者设置环境变量 KEYSTORE_FILE/KEYSTORE_PASSWORD/KEY_ALIAS/KEY_PASSWORD
    echo ================================
) else (
    echo.
    echo 密钥库创建失败，请检查JDK是否正确安装
)

pause
