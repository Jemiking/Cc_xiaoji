# POI文件清理建议

> 日期：2025-01-29  
> 目的：清理已废弃的POI相关文件

## 一、需要清理的文件

### 1.1 POI相关实现文件（建议删除）
```
app/src/main/kotlin/com/ccxiaoji/app/data/excel/
├── ExcelReader.kt              # POI读取器，已被FastExcelReader替代
├── ExcelStyleManager.kt        # POI样式管理，已被FastExcelStyleHelper替代
├── SimpleExcelWriter.kt        # POI写入器，已被FastExcelWriter替代
├── SimpleXlsxReader.kt         # POI读取器，已被FastExcelReader替代
└── SafeExcelReader.kt          # POI安全读取器，功能已整合到FastExcelReader
```

### 1.2 已被FastExcel替代的文件（建议删除）
```
├── BalanceCalculator.kt        # 已被FastExcelBalanceValidator替代
├── BalanceValidator.kt         # 已被FastExcelBalanceValidator替代
├── BatchImportProcessor.kt     # 已被FastExcelBatchImportProcessor替代
├── ImportValidator.kt          # 已被FastExcelValidator替代
├── ExcelPerformanceOptimizer.kt # 已被FastExcelPerformanceOptimizer替代
└── ProgressManager.kt          # 功能已整合到FastExcelManager
```

### 1.3 功能重复或过时的文件（建议删除）
```
├── EnhancedBatchImportProcessor.kt  # 功能已整合
├── ExcelBatchImportEnhancer.kt      # 功能已整合
├── StreamingExcelImportManager.kt   # 功能已整合
├── ExcelToStandardAdapter.kt        # 不再需要适配器
├── DataIntegrityService.kt          # 功能已整合到验证器
└── FastExcelTest.kt                 # 临时测试文件
```

### 1.4 需要评估的文件（建议保留或整合）
```
├── ExcelImportManager.kt        # 检查是否还在使用
├── ColumnMappingDetector.kt     # 可能还有用，需要评估
└── mapping/
    └── ExcelColumnMappings.kt   # 列映射配置，可能需要保留
```

## 二、清理方案

### 方案一：直接删除（推荐）
```bash
# 创建备份
git checkout -b backup/poi-files

# 删除文件
rm app/src/main/kotlin/com/ccxiaoji/app/data/excel/ExcelReader.kt
rm app/src/main/kotlin/com/ccxiaoji/app/data/excel/ExcelStyleManager.kt
# ... 删除所有列出的文件

# 提交
git add -A
git commit -m "chore: 移除已废弃的POI相关文件"
```

### 方案二：移到legacy目录
```bash
# 创建legacy目录
mkdir -p app/src/main/kotlin/com/ccxiaoji/app/data/excel/legacy

# 移动文件
mv app/src/main/kotlin/com/ccxiaoji/app/data/excel/ExcelReader.kt \
   app/src/main/kotlin/com/ccxiaoji/app/data/excel/legacy/

# ... 移动所有文件
```

### 方案三：添加@Deprecated注解
```kotlin
@Deprecated(
    message = "已被FastExcelReader替代，请使用FastExcel实现",
    replaceWith = ReplaceWith("FastExcelReader"),
    level = DeprecationLevel.ERROR
)
class ExcelReader {
    // ...
}
```

## 三、清理前检查

### 3.1 依赖检查
```bash
# 搜索是否还有引用
grep -r "ExcelReader" app/src/main/kotlin/
grep -r "SimpleExcelWriter" app/src/main/kotlin/
grep -r "ExcelStyleManager" app/src/main/kotlin/
```

### 3.2 导入检查
```bash
# 检查import语句
grep -r "import.*excel.*ExcelReader" app/src/main/kotlin/
grep -r "import.*excel.*SimpleExcelWriter" app/src/main/kotlin/
```

## 四、清理收益

1. **代码量减少**：约3000行冗余代码
2. **维护成本降低**：避免维护两套实现
3. **避免混淆**：新开发者不会误用旧实现
4. **编译速度**：略微提升

## 五、风险评估

- **低风险**：这些文件已不再被使用
- **缓解措施**：创建备份分支，可随时恢复

## 六、执行计划

1. **第一步**：在git上创建备份分支
2. **第二步**：执行依赖检查，确认无引用
3. **第三步**：删除文件
4. **第四步**：运行编译，确保无错误
5. **第五步**：运行测试，确保功能正常
6. **第六步**：提交代码

---

**建议**：在v2.0发布后的下一个版本中执行此清理，避免在大版本发布时引入额外风险。