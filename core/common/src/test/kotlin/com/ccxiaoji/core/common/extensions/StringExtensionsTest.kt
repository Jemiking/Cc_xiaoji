package com.ccxiaoji.core.common.extensions

import org.junit.Assert.*
import org.junit.Test

/**
 * 字符串扩展函数的单元测试
 */
class StringExtensionsTest {
    
    @Test
    fun `test isNotNullOrBlank with null string`() {
        val str: String? = null
        assertFalse(str.isNotNullOrBlank())
    }
    
    @Test
    fun `test isNotNullOrBlank with empty string`() {
        val str = ""
        assertFalse(str.isNotNullOrBlank())
    }
    
    @Test
    fun `test isNotNullOrBlank with blank string`() {
        val str = "   "
        assertFalse(str.isNotNullOrBlank())
    }
    
    @Test
    fun `test isNotNullOrBlank with valid string`() {
        val str = "Hello"
        assertTrue(str.isNotNullOrBlank())
    }
    
    @Test
    fun `test toMD5`() {
        val str = "Hello World"
        val md5 = str.toMD5()
        assertEquals("b10a8db164e0754105b7a99be72e3fe5", md5)
    }
    
    @Test
    fun `test removeAllSpaces`() {
        val str = "Hello World Test"
        assertEquals("HelloWorldTest", str.removeAllSpaces())
    }
    
    @Test
    fun `test ellipsize when string is shorter than max length`() {
        val str = "Hello"
        assertEquals("Hello", str.ellipsize(10))
    }
    
    @Test
    fun `test ellipsize when string is longer than max length`() {
        val str = "Hello World Test"
        assertEquals("Hello W...", str.ellipsize(10))
    }
    
    @Test
    fun `test capitalizeFirst with lowercase string`() {
        val str = "hello"
        assertEquals("Hello", str.capitalizeFirst())
    }
    
    @Test
    fun `test capitalizeFirst with empty string`() {
        val str = ""
        assertEquals("", str.capitalizeFirst())
    }
}