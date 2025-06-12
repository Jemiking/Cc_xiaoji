package com.ccxiaoji.core.database

import com.ccxiaoji.core.database.model.RecurringFrequency
import com.ccxiaoji.core.database.model.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Converters单元测试
 * 测试Room类型转换器的正确性
 */
class ConvertersTest {
    
    private val converters = Converters()
    
    @Test
    fun testSyncStatusConversion() {
        // 测试所有SyncStatus枚举值
        SyncStatus.values().forEach { status ->
            val stringValue = converters.fromSyncStatus(status)
            val convertedBack = converters.toSyncStatus(stringValue)
            assertEquals(status, convertedBack)
        }
    }
    
    @Test
    fun testRecurringFrequencyConversion() {
        // 测试所有RecurringFrequency枚举值
        RecurringFrequency.values().forEach { frequency ->
            val stringValue = converters.fromRecurringFrequency(frequency)
            val convertedBack = converters.toRecurringFrequency(stringValue!!)
            assertEquals(frequency, convertedBack)
        }
        
        // 测试null值
        assertNull(converters.fromRecurringFrequency(null))
        assertNull(converters.toRecurringFrequency(null))
    }
    
    @Test
    fun testListOfStringsConversion() {
        // 测试正常列表
        val originalList = listOf("tag1", "tag2", "tag3")
        val json = converters.fromListOfStrings(originalList)
        val convertedBack = converters.toListOfStrings(json!!)
        assertEquals(originalList, convertedBack)
        
        // 测试空列表
        val emptyList = emptyList<String>()
        val emptyJson = converters.fromListOfStrings(emptyList)
        val emptyConverted = converters.toListOfStrings(emptyJson!!)
        assertEquals(emptyList, emptyConverted)
        
        // 测试null值
        assertNull(converters.fromListOfStrings(null))
        assertNull(converters.toListOfStrings(null))
    }
    
    @Test
    fun testMapOfStringToDoubleConversion() {
        // 测试正常Map
        val originalMap = mapOf(
            "category1" to 100.5,
            "category2" to 200.75,
            "category3" to 300.0
        )
        val json = converters.fromMapOfStringToDouble(originalMap)
        val convertedBack = converters.toMapOfStringToDouble(json!!)
        assertEquals(originalMap, convertedBack)
        
        // 测试空Map
        val emptyMap = emptyMap<String, Double>()
        val emptyJson = converters.fromMapOfStringToDouble(emptyMap)
        val emptyConverted = converters.toMapOfStringToDouble(emptyJson!!)
        assertEquals(emptyMap, emptyConverted)
        
        // 测试null值
        assertNull(converters.fromMapOfStringToDouble(null))
        assertNull(converters.toMapOfStringToDouble(null))
    }
    
    @Test
    fun testInvalidSyncStatusConversion() {
        // 测试无效的SyncStatus字符串
        val invalidStatus = "INVALID_STATUS"
        val defaultStatus = converters.toSyncStatus(invalidStatus)
        assertEquals(SyncStatus.SYNCED, defaultStatus) // 应该返回默认值SYNCED
    }
    
    @Test
    fun testInvalidRecurringFrequencyConversion() {
        // 测试无效的RecurringFrequency字符串
        val invalidFrequency = "INVALID_FREQUENCY"
        val defaultFrequency = converters.toRecurringFrequency(invalidFrequency)
        assertNull(defaultFrequency) // 应该返回null
    }
    
    @Test
    fun testSpecialCharactersInListConversion() {
        // 测试包含特殊字符的列表
        val specialList = listOf(
            "tag with spaces",
            "tag,with,commas",
            "tag\"with\"quotes",
            "tag\\with\\backslashes"
        )
        val json = converters.fromListOfStrings(specialList)
        val convertedBack = converters.toListOfStrings(json!!)
        assertEquals(specialList, convertedBack)
    }
    
    @Test
    fun testLargeMapConversion() {
        // 测试较大的Map
        val largeMap = (1..100).associate { "category$it" to it * 10.5 }
        val json = converters.fromMapOfStringToDouble(largeMap)
        val convertedBack = converters.toMapOfStringToDouble(json!!)
        assertEquals(largeMap, convertedBack)
    }
}