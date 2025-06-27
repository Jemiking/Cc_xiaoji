package com.ccxiaoji.feature.plan.domain.repository

import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import kotlinx.coroutines.flow.Flow

/**
 * 模板仓库接口
 * 定义模板相关的数据操作
 */
interface TemplateRepository {
    
    /**
     * 获取所有模板
     */
    fun getAllTemplates(): Flow<List<Template>>
    
    /**
     * 根据分类获取模板
     */
    fun getTemplatesByCategory(category: TemplateCategory): Flow<List<Template>>
    
    /**
     * 获取系统预置模板
     */
    fun getSystemTemplates(): Flow<List<Template>>
    
    /**
     * 获取用户创建的模板
     */
    fun getUserTemplates(userId: String): Flow<List<Template>>
    
    /**
     * 获取公开模板
     */
    fun getPublicTemplates(): Flow<List<Template>>
    
    /**
     * 获取热门模板
     */
    fun getPopularTemplates(limit: Int = 10): Flow<List<Template>>
    
    /**
     * 获取推荐模板
     */
    fun getRecommendedTemplates(limit: Int = 10): Flow<List<Template>>
    
    /**
     * 根据ID获取模板
     */
    suspend fun getTemplateById(templateId: String): com.ccxiaoji.common.base.BaseResult<Template>
    
    /**
     * 搜索模板
     */
    fun searchTemplates(keyword: String, category: TemplateCategory? = null): Flow<List<Template>>
    
    /**
     * 创建模板
     */
    suspend fun createTemplate(template: Template): String
    
    /**
     * 更新模板
     */
    suspend fun updateTemplate(template: Template)
    
    /**
     * 删除模板
     */
    suspend fun deleteTemplate(templateId: String)
    
    /**
     * 使用模板（增加使用次数）
     */
    suspend fun useTemplate(templateId: String)
    
    /**
     * 评价模板
     */
    suspend fun rateTemplate(templateId: String, rating: Float)
    
    /**
     * 批量插入模板（用于初始化系统模板）
     */
    suspend fun insertTemplates(templates: List<Template>)
    
    /**
     * 检查是否已初始化系统模板
     */
    suspend fun hasSystemTemplates(): Boolean
}