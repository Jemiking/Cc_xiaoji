package com.ccxiaoji.feature.ledger.presentation.viewmodel

/**
 * 资产总览调试配置类
 * 
 * 用于控制AssetOverviewViewModel的调试输出级别
 * 根据不同使用场景调整日志输出量，优化性能
 */
object AssetOverviewDebugConfig {
    
    /**
     * 调试级别枚举
     */
    enum class DebugLevel {
        NONE,        // 无调试输出，生产环境使用
        BASIC,       // 基础调试信息，开发环境使用  
        DETAILED,    // 详细调试信息，问题排查使用
        VERBOSE      // 完整调试信息，性能分析使用
    }
    
    /**
     * 当前调试级别
     * 
     * 建议设置：
     * - 生产环境：NONE
     * - 开发环境：BASIC  
     * - 问题排查：DETAILED
     * - 性能分析：VERBOSE
     */
    var currentLevel: DebugLevel = DebugLevel.BASIC
    
    /**
     * 检查是否启用基础调试
     */
    fun isBasicDebugEnabled(): Boolean = currentLevel >= DebugLevel.BASIC
    
    /**
     * 检查是否启用详细调试
     */
    fun isDetailedDebugEnabled(): Boolean = currentLevel >= DebugLevel.DETAILED
    
    /**
     * 检查是否启用完整调试
     */
    fun isVerboseDebugEnabled(): Boolean = currentLevel >= DebugLevel.VERBOSE
    
    /**
     * 快速配置方法
     */
    fun configureForProduction() {
        currentLevel = DebugLevel.NONE
    }
    
    fun configureForDevelopment() {
        currentLevel = DebugLevel.BASIC
    }
    
    fun configureForDebugging() {
        currentLevel = DebugLevel.DETAILED
    }
    
    fun configureForProfiling() {
        currentLevel = DebugLevel.VERBOSE
    }
    
    /**
     * 性能影响说明
     */
    fun getPerformanceImpactDescription(): String {
        return when (currentLevel) {
            DebugLevel.NONE -> "无性能影响"
            DebugLevel.BASIC -> "轻微性能影响（<1%）"
            DebugLevel.DETAILED -> "中等性能影响（1-3%）"
            DebugLevel.VERBOSE -> "显著性能影响（3-10%），仅用于分析"
        }
    }
}