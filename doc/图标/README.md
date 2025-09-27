# 图标清单（Google Fonts Icons）

- 风格：Material Symbols Rounded
- 填充：Fill=0（描边）
- 尺寸：24px
- 格式：SVG
- 来源：fonts.google.com/icons（gstatic CDN）

## 文件命名
- 规则：Google 原始短名（下划线）→ 小驼峰
- 例：`account_balance_wallet` → `accountBalanceWallet.svg`

## 目录结构
- 本目录存放已下载的 SVG：`doc/图标/*.svg`
- 清单：`doc/图标/icons_manifest.json`

## 批量下载
- 需要 Python 3：
  - 运行：`python tools/fetch_material_symbols.py`
  - 脚本会优先尝试 Material Symbols Rounded（Fill=0, 24px）的 gstatic 路径；
    若个别图标暂不可用，将回退到旧版 Material Icons Round/Outlined。

## 许可
- Google 提供的 Material Symbols / Material Icons 受其许可条款约束。
  请参考：https://developers.google.com/fonts/docs/material_symbols

## 备注
- 若后续需要 PNG 预览或填充版本（Fill=1），可在 `tools/fetch_material_symbols.py` 中调整路径或新增导出脚本。
