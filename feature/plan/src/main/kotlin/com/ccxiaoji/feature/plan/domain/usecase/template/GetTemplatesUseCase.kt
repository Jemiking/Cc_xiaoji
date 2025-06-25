package com.ccxiaoji.feature.plan.domain.usecase.template

import com.ccxiaoji.feature.plan.domain.model.Template
import com.ccxiaoji.feature.plan.domain.model.TemplateCategory
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 获取模板列表用例
 */
class GetTemplatesUseCase @Inject constructor(
    private val templateRepository: TemplateRepository
) {
    /**
     * 获取所有模板
     */
    fun getAllTemplates(): Flow<List<Template>> {
        return templateRepository.getAllTemplates()
    }
    
    /**
     * 根据分类获取模板
     */
    fun getTemplatesByCategory(category: TemplateCategory): Flow<List<Template>> {
        return templateRepository.getTemplatesByCategory(category)
    }
    
    /**
     * 获取系统预置模板
     */
    fun getSystemTemplates(): Flow<List<Template>> {
        return templateRepository.getSystemTemplates()
    }
    
    /**
     * 获取用户创建的模板
     */
    fun getUserTemplates(userId: String): Flow<List<Template>> {
        return templateRepository.getUserTemplates(userId)
    }
    
    /**
     * 获取热门模板
     */
    fun getPopularTemplates(limit: Int = 10): Flow<List<Template>> {
        return templateRepository.getPopularTemplates(limit)
    }
    
    /**
     * 获取推荐模板
     */
    fun getRecommendedTemplates(limit: Int = 10): Flow<List<Template>> {
        return templateRepository.getRecommendedTemplates(limit)
    }
    
    /**
     * 搜索模板
     */
    fun searchTemplates(keyword: String, category: TemplateCategory? = null): Flow<List<Template>> {
        return templateRepository.searchTemplates(keyword, category)
    }
    
    /**
     * 根据ID获取模板详情
     */
    suspend fun getTemplateById(templateId: String): com.ccxiaoji.common.base.BaseResult<Template> {
        return templateRepository.getTemplateById(templateId)
    }
}