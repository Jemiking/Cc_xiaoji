# DataImport模块依赖关系图

> 创建日期：2025-07-20  
> 目的：清晰记录DataImport模块的所有依赖关系，便于后续维护和迁移  
> 状态：持续更新中

## 一、核心组件依赖

### DataImportViewModel
主要的ViewModel，负责协调数据导入流程。

```
DataImportViewModel
├── Context (@ApplicationContext)
├── ImportService (JSON导入服务)
├── ImportManagerAdapter (Excel导入适配器)
│   ├── FastExcelReader
│   ├── FastExcelValidator
│   ├── FastExcelBatchImportProcessor
│   │   ├── FastExcelDataParser
│   │   └── FastExcelValidator
│   ├── LedgerApi
│   ├── TodoApi
│   └── HabitApi
├── FileValidator (文件验证器)
├── ExcelToStandardAdapter (Excel到标准格式的适配器)
├── ColumnMappingDetector (列映射检测器)
└── BalanceValidator (余额验证器)
    └── FastExcelBalanceValidator (委托实现)
```

## 二、组件说明

### 1. ImportManagerAdapter
- **作用**：Excel导入管理器的适配器，替代原有的ExcelImportManager
- **职责**：
  - 分析Excel文件结构
  - 执行Excel数据导入
  - 提供进度反馈
- **实现状态**：临时解决方案，后续需要完善

### 2. ExcelToStandardAdapter
- **作用**：将Excel特定的数据格式转换为应用标准格式
- **职责**：
  - 转换导入进度格式
  - 转换导入结果格式
- **实现状态**：基础实现完成

### 3. BalanceValidator
- **作用**：验证导入数据的余额准确性
- **职责**：
  - 委托给FastExcelBalanceValidator执行实际验证
  - 提供统一的验证接口
- **实现状态**：委托模式实现

## 三、FastExcel组件依赖

### FastExcel核心组件
```
FastExcel组件族
├── FastExcelManager (主管理器)
├── FastExcelReader (读取器)
├── FastExcelWriter (写入器)
├── FastExcelValidator (验证器)
├── FastExcelBatchImportProcessor (批量处理器)
├── FastExcelDataParser (数据解析器)
├── FastExcelStyleHelper (样式助手)
├── FastExcelPerformanceOptimizer (性能优化器)
└── FastExcelBalanceValidator (余额验证器)
```

## 四、UI组件依赖

### ExcelImportWithValidation
```
ExcelImportWithValidation (Compose组件)
├── ImportManagerAdapter
├── BalanceValidator
├── ColumnMappingDetector
└── ExcelPreviewStep (子组件)
```

## 五、迁移状态

### 已删除的POI组件
- ❌ ExcelImportManager (已用ImportManagerAdapter替代)
- ❌ ExcelReader (功能由FastExcelReader承担)
- ❌ BatchImportProcessor (功能由FastExcelBatchImportProcessor承担)
- ❌ SimpleXlsxReader
- ❌ ExcelStyleManager
- ❌ SimpleExcelWriter

### 保留的组件
- ✅ ColumnMappingDetector
- ✅ FileValidator
- ✅ ImportService

## 六、依赖注入配置

### 需要Hilt配置的组件
1. ImportManagerAdapter - 已配置@Singleton和@Inject
2. ExcelToStandardAdapter - 已配置@Singleton和@Inject
3. BalanceValidator - 已配置@Singleton和@Inject
4. FastExcel系列组件 - 已配置相应注解

### 可能需要的Module配置
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ExcelModule {
    // 如果需要特殊的提供方法，在这里添加
}
```

## 七、潜在问题和风险

### 1. 类型不匹配
- ImportManagerAdapter的方法签名可能与原ExcelImportManager不完全一致
- 需要确保所有调用点都已更新

### 2. 功能缺失
- ImportManagerAdapter的某些方法可能只是占位实现
- 需要逐步完善实际功能

### 3. 性能差异
- FastExcel和POI的性能特性不同
- 需要进行性能测试和优化

## 八、后续工作

### 短期（1-2天）
1. 完善ImportManagerAdapter的实际实现
2. 补充单元测试
3. 验证导入功能的完整性

### 中期（3-5天）
1. 逐步移除对POI的间接依赖
2. 优化FastExcel的使用方式
3. 完善错误处理机制

### 长期（1周以上）
1. 完全迁移到FastExcel
2. 移除所有适配器层
3. 优化整体架构

---

*文档将随着开发进展持续更新*