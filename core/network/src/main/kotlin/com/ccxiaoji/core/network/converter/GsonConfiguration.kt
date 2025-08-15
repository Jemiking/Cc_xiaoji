package com.ccxiaoji.core.network.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder

/**
 * Gson配置
 * 提供统一的JSON序列化/反序列化配置
 */
object GsonConfiguration {
    
    /**
     * 创建配置好的Gson实例
     */
    fun create(): Gson {
        return GsonBuilder()
            .setLenient() // 宽松模式，允许一些非标准的JSON
            .create()
    }
}