package com.ccxiaoji.feature.plan.data.repository

import com.ccxiaoji.feature.plan.data.local.dao.TemplateDao
import com.ccxiaoji.feature.plan.data.local.entity.TemplateEntity
import com.ccxiaoji.feature.plan.domain.model.*
import com.ccxiaoji.feature.plan.domain.repository.TemplateRepository
import com.ccxiaoji.feature.plan.util.core.safeDbCall
import com.ccxiaoji.common.base.BaseResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

/**
 * 模板仓库实现类
 */
class TemplateRepositoryImpl @Inject constructor(
    private val templateDao: TemplateDao
) : TemplateRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    override fun getAllTemplates(): Flow<List<Template>> {
        return templateDao.getAllTemplates().map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getTemplatesByCategory(category: TemplateCategory): Flow<List<Template>> {
        return templateDao.getTemplatesByCategory(category.name).map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getSystemTemplates(): Flow<List<Template>> {
        return templateDao.getSystemTemplates().map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getUserTemplates(userId: String): Flow<List<Template>> {
        return templateDao.getUserTemplates(userId).map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getPublicTemplates(): Flow<List<Template>> {
        return templateDao.getPublicTemplates().map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getPopularTemplates(limit: Int): Flow<List<Template>> {
        return templateDao.getPopularTemplates(limit).map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override fun getRecommendedTemplates(limit: Int): Flow<List<Template>> {
        return templateDao.getRecommendedTemplates(limit).map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override suspend fun getTemplateById(templateId: String): BaseResult<Template> {
        return try {
            val entity = templateDao.getTemplateById(templateId)
            if (entity != null) {
                BaseResult.Success(entityToTemplate(entity))
            } else {
                BaseResult.Error(Exception("模板不存在"))
            }
        } catch (e: Exception) {
            BaseResult.Error(e)
        }
    }
    
    override fun searchTemplates(keyword: String, category: TemplateCategory?): Flow<List<Template>> {
        return templateDao.searchTemplates(keyword, category?.name).map { entities ->
            entities.map { entityToTemplate(it) }
        }
    }
    
    override suspend fun createTemplate(template: Template): String = safeDbCall {
        val entity = templateToEntity(template)
        templateDao.insertTemplate(entity)
        entity.id
    }
    
    override suspend fun updateTemplate(template: Template) = safeDbCall {
        templateDao.updateTemplate(templateToEntity(template))
    }
    
    override suspend fun deleteTemplate(templateId: String) = safeDbCall {
        templateDao.deleteTemplateById(templateId)
    }
    
    override suspend fun useTemplate(templateId: String) = safeDbCall {
        templateDao.incrementUseCount(templateId)
    }
    
    override suspend fun rateTemplate(templateId: String, rating: Float) = safeDbCall {
        templateDao.updateRating(templateId, rating)
    }
    
    override suspend fun insertTemplates(templates: List<Template>) = safeDbCall {
        val entities = templates.map { templateToEntity(it) }
        templateDao.insertTemplates(entities)
    }
    
    override suspend fun hasSystemTemplates(): Boolean = safeDbCall {
        templateDao.getSystemTemplateCount() > 0
    }
    
    /**
     * 将数据库实体转换为领域模型
     */
    private fun entityToTemplate(entity: TemplateEntity): Template {
        return Template(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = TemplateCategory.fromString(entity.category),
            tags = json.decodeFromString<List<String>>(entity.tags),
            color = entity.color,
            duration = entity.duration,
            structure = json.decodeFromString<TemplateStructure>(entity.structure),
            useCount = entity.useCount,
            rating = entity.rating,
            isSystem = entity.isSystem,
            isPublic = entity.isPublic,
            createdBy = entity.createdBy,
            createdAt = Instant.fromEpochMilliseconds(entity.createdAt),
            updatedAt = Instant.fromEpochMilliseconds(entity.updatedAt),
            syncStatus = SyncStatus.valueOf(entity.syncStatus)
        )
    }
    
    /**
     * 将领域模型转换为数据库实体
     */
    private fun templateToEntity(template: Template): TemplateEntity {
        return TemplateEntity(
            id = template.id,
            title = template.title,
            description = template.description,
            category = template.category.name,
            tags = json.encodeToString(template.tags),
            color = template.color,
            duration = template.duration,
            structure = json.encodeToString(template.structure),
            useCount = template.useCount,
            rating = template.rating,
            isSystem = template.isSystem,
            isPublic = template.isPublic,
            createdBy = template.createdBy,
            createdAt = template.createdAt.toEpochMilliseconds(),
            updatedAt = template.updatedAt.toEpochMilliseconds(),
            syncStatus = template.syncStatus.name
        )
    }
}