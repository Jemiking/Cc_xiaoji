package com.ccxiaoji.feature.ledger.data.importer.qianji

import org.junit.Test
import org.junit.Assert.*

/**
 * 钱迹分类映射测试
 */
class QianjiCategoryMappingTest {
    
    @Test
    fun `映射钱迹分类到二级分类_支出类别`() {
        // 测试餐饮类
        val result1 = QianjiCategoryMapping.getMappedCategory("下馆子", null, "支出")
        assertEquals("餐饮", result1.first)
        assertEquals("外出就餐", result1.second)
        
        val result2 = QianjiCategoryMapping.getMappedCategory("早餐", null, "支出")
        assertEquals("餐饮", result2.first)
        assertEquals("早餐", result2.second)
        
        // 测试交通类
        val result3 = QianjiCategoryMapping.getMappedCategory("地铁", null, "支出")
        assertEquals("交通", result3.first)
        assertEquals("地铁", result3.second)
        
        val result4 = QianjiCategoryMapping.getMappedCategory("加油", null, "支出")
        assertEquals("交通", result4.first)
        assertEquals("加油", result4.second)
        
        // 测试购物类
        val result5 = QianjiCategoryMapping.getMappedCategory("衣服", null, "支出")
        assertEquals("购物", result5.first)
        assertEquals("服饰", result5.second)
    }
    
    @Test
    fun `映射钱迹分类到二级分类_收入类别`() {
        // 测试职业收入
        val result1 = QianjiCategoryMapping.getMappedCategory("工资", null, "收入")
        assertEquals("职业收入", result1.first)
        assertEquals("工资", result1.second)
        
        val result2 = QianjiCategoryMapping.getMappedCategory("奖金", null, "收入")
        assertEquals("职业收入", result2.first)
        assertEquals("奖金", result2.second)
        
        // 测试其他收入
        val result3 = QianjiCategoryMapping.getMappedCategory("外快", null, "收入")
        assertEquals("其他收入", result3.first)
        assertEquals("兼职", result3.second)
        
        val result4 = QianjiCategoryMapping.getMappedCategory("红包", null, "收入")
        assertEquals("其他收入", result4.first)
        assertEquals("红包", result4.second)
    }
    
    @Test
    fun `未映射的分类_使用原始名称`() {
        // 没有映射的分类，使用原始名称
        val result1 = QianjiCategoryMapping.getMappedCategory("未知分类", null, "支出")
        assertEquals("未知分类", result1.first)
        assertEquals("一般", result1.second) // 没有二级分类时创建默认"一般"
        
        // 有二级分类的未映射分类
        val result2 = QianjiCategoryMapping.getMappedCategory("未知分类", "子分类", "支出")
        assertEquals("未知分类", result2.first)
        assertEquals("子分类", result2.second)
    }
    
    @Test
    fun `优先使用二级分类映射`() {
        // 如果二级分类有映射，优先使用
        val result = QianjiCategoryMapping.getMappedCategory("其他", "午餐", "支出")
        assertEquals("餐饮", result.first)
        assertEquals("午餐", result.second)
    }
    
    @Test
    fun `获取分类图标建议_子分类`() {
        // 测试子分类图标
        assertEquals("☕", QianjiCategoryMapping.suggestCategoryIcon("餐饮", "早餐"))
        assertEquals("🍜", QianjiCategoryMapping.suggestCategoryIcon("餐饮", "午餐"))
        assertEquals("🍽️", QianjiCategoryMapping.suggestCategoryIcon("餐饮", "晚餐"))
        assertEquals("🥡", QianjiCategoryMapping.suggestCategoryIcon("餐饮", "外卖"))
        
        assertEquals("🚌", QianjiCategoryMapping.suggestCategoryIcon("交通", "公交"))
        assertEquals("🚇", QianjiCategoryMapping.suggestCategoryIcon("交通", "地铁"))
        assertEquals("🚕", QianjiCategoryMapping.suggestCategoryIcon("交通", "打车"))
        assertEquals("⛽", QianjiCategoryMapping.suggestCategoryIcon("交通", "加油"))
        
        assertEquals("💰", QianjiCategoryMapping.suggestCategoryIcon("职业收入", "工资"))
        assertEquals("🏆", QianjiCategoryMapping.suggestCategoryIcon("职业收入", "奖金"))
        assertEquals("🧧", QianjiCategoryMapping.suggestCategoryIcon("其他收入", "红包"))
    }
    
    @Test
    fun `获取分类图标建议_父分类`() {
        // 测试父分类图标（当没有子分类时）
        assertEquals("🍔", QianjiCategoryMapping.suggestCategoryIcon("餐饮", null))
        assertEquals("🚗", QianjiCategoryMapping.suggestCategoryIcon("交通", null))
        assertEquals("🛒", QianjiCategoryMapping.suggestCategoryIcon("购物", null))
        assertEquals("🎮", QianjiCategoryMapping.suggestCategoryIcon("娱乐", null))
        assertEquals("🏠", QianjiCategoryMapping.suggestCategoryIcon("居住", null))
        assertEquals("📱", QianjiCategoryMapping.suggestCategoryIcon("通讯", null))
        assertEquals("🏥", QianjiCategoryMapping.suggestCategoryIcon("医疗", null))
        assertEquals("📚", QianjiCategoryMapping.suggestCategoryIcon("教育", null))
        assertEquals("💰", QianjiCategoryMapping.suggestCategoryIcon("职业收入", null))
    }
    
    @Test
    fun `获取分类颜色建议`() {
        assertEquals("#FF9800", QianjiCategoryMapping.suggestCategoryColor("餐饮"))
        assertEquals("#2196F3", QianjiCategoryMapping.suggestCategoryColor("交通"))
        assertEquals("#E91E63", QianjiCategoryMapping.suggestCategoryColor("购物"))
        assertEquals("#9C27B0", QianjiCategoryMapping.suggestCategoryColor("娱乐"))
        assertEquals("#4CAF50", QianjiCategoryMapping.suggestCategoryColor("居住"))
        assertEquals("#00BCD4", QianjiCategoryMapping.suggestCategoryColor("通讯"))
        assertEquals("#FF5722", QianjiCategoryMapping.suggestCategoryColor("医疗"))
        assertEquals("#3F51B5", QianjiCategoryMapping.suggestCategoryColor("教育"))
        assertEquals("#4CAF50", QianjiCategoryMapping.suggestCategoryColor("职业收入"))
        assertEquals("#6200EE", QianjiCategoryMapping.suggestCategoryColor("未知分类")) // 默认颜色
    }
}