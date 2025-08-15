package com.ccxiaoji.feature.ledger.data.importer.converter

import com.ccxiaoji.feature.ledger.data.importer.DataLine
import com.ccxiaoji.feature.ledger.data.local.entity.CategoryEntity
import com.ccxiaoji.feature.ledger.domain.importer.ImportError
import com.ccxiaoji.common.model.CategoryType
import java.util.UUID
import javax.inject.Inject

/**
 * 分类数据转换器
 * CATEGORY格式: 创建日期,分类名称,分类类型,图标,颜色,父分类,显示顺序
 */
class CategoryConverter @Inject constructor() : DataConverter<CategoryEntity>() {
    
    override fun convert(dataLine: DataLine, userId: String): ConvertResult<CategoryEntity> {
        val data = dataLine.data
        
        if (data.size < 3) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "分类数据格式错误，至少需要3个字段")
            )
        }
        
        try {
            val createDate = safeGetString(data, 0)
            val name = safeGetString(data, 1)
            val typeStr = safeGetString(data, 2)
            val icon = safeGetString(data, 3)
            val color = safeGetString(data, 4)
            val parentName = safeGetString(data, 5) // 需要后续映射为parentId
            val displayOrder = safeGetInt(data, 6)
            
            // 验证必填字段
            if (name.isEmpty()) {
                return ConvertResult.Error(
                    ImportError.ValidationError(dataLine.line, "分类名称", "分类名称不能为空")
                )
            }
            
            // 解析分类类型
            val categoryType = when (typeStr) {
                "EXPENSE", "支出" -> "EXPENSE"
                "INCOME", "收入" -> "INCOME"
                else -> "EXPENSE"
            }
            
            // 创建分类实体
            val category = CategoryEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                name = name,
                type = categoryType,
                icon = icon.ifEmpty { "📝" },
                color = color.ifEmpty { "#6200EE" },
                parentId = null, // 暂时设置为null，后续处理父分类关系
                displayOrder = displayOrder,
                createdAt = parseDate(createDate) ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                syncStatus = com.ccxiaoji.common.model.SyncStatus.PENDING
            )
            
            return ConvertResult.Success(category)
            
        } catch (e: Exception) {
            return ConvertResult.Error(
                ImportError.FormatError(dataLine.line, "分类数据转换失败: ${e.message}")
            )
        }
    }
}