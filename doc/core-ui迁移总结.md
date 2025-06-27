# core-ui模块迁移总结

## 迁移时间
2025-06-18

## 迁移内容清单

### 1. 主题定义（theme/）
- ✅ `Color.kt` - 颜色定义（包含通用颜色和分类颜色）
- ✅ `Theme.kt` - 主题配置
- ✅ `Type.kt` - 字体样式定义

### 2. 通用组件（components/）
- ✅ `ColorPicker.kt` - 通用颜色选择器
- ✅ `IconPicker.kt` - 通用图标选择器

### 3. 未迁移内容说明
- `colors.xml` - 仅包含launcher背景色，保留在app模块
- `themes.xml` - 应用级主题配置，保留在app模块
- 图表组件（BarChart、LineChart、PieChart）- 需要参数化改造后才能迁移
- 业务组件（AccountSelector等）- 将在Feature模块迁移时处理

## 迁移过程记录

### 步骤1：创建模块目录结构
```
core/ui/src/main/kotlin/com/ccxiaoji/ui/
├── theme/
└── components/
```

### 步骤2：迁移文件
1. 迁移所有theme文件夹下的文件
2. 迁移ColorPicker和IconPicker组件
3. 更新包名

### 步骤3：更新导入语句
- 1个文件的`CcXiaoJiTheme`导入（MainActivity.kt）
- 1个文件的`ColorPicker`和`IconPicker`导入（CategoryDialog.kt）

### 步骤4：依赖配置
- 在app模块的build.gradle.kts中添加了`implementation(project(":core:ui"))`

## 验证结果
- ✅ 编译成功
- ✅ 所有导入正确更新
- ✅ 主题和组件功能正常

## 影响范围
- MainActivity.kt - 使用主题
- CategoryDialog.kt - 使用ColorPicker和IconPicker

## 后续建议

### 1. 图表组件改造计划
需要将图表组件改造为通用组件，移除对具体业务模型的依赖：
- BarChart：接受通用数据结构`List<BarChartData>`
- LineChart：接受通用数据结构`List<LineChartData>`
- PieChart：接受通用数据结构`List<PieChartData>`

### 2. 组件分类建议
- **保留在core-ui**：纯UI组件，无业务依赖
- **迁移到feature模块**：包含业务逻辑的组件
- **保留在app模块**：应用级组件（如BottomNavBar）

### 3. 主题扩展建议
- 可以考虑添加更多主题相关的通用组件
- 提供主题切换的工具函数
- 支持深色模式的完整配置