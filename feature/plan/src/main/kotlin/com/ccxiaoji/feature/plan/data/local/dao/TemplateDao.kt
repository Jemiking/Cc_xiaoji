package com.ccxiaoji.feature.plan.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.plan.data.local.entity.TemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * 模板数据访问对象
 */
@Dao
interface TemplateDao {
    
    // 插入模板
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: TemplateEntity)
    
    // 批量插入模板
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<TemplateEntity>)
    
    // 更新模板
    @Update
    suspend fun updateTemplate(template: TemplateEntity)
    
    // 删除模板
    @Delete
    suspend fun deleteTemplate(template: TemplateEntity)
    
    // 根据ID删除模板
    @Query("DELETE FROM template_table WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: String)
    
    // 根据ID获取模板
    @Query("SELECT * FROM template_table WHERE id = :templateId")
    suspend fun getTemplateById(templateId: String): TemplateEntity?
    
    // 获取所有模板
    @Query("SELECT * FROM template_table ORDER BY use_count DESC, created_at DESC")
    fun getAllTemplates(): Flow<List<TemplateEntity>>
    
    // 根据分类获取模板
    @Query("SELECT * FROM template_table WHERE category = :category ORDER BY use_count DESC, created_at DESC")
    fun getTemplatesByCategory(category: String): Flow<List<TemplateEntity>>
    
    // 获取系统预置模板
    @Query("SELECT * FROM template_table WHERE is_system = 1 ORDER BY category, created_at DESC")
    fun getSystemTemplates(): Flow<List<TemplateEntity>>
    
    // 获取用户创建的模板
    @Query("SELECT * FROM template_table WHERE is_system = 0 AND created_by = :userId ORDER BY created_at DESC")
    fun getUserTemplates(userId: String): Flow<List<TemplateEntity>>
    
    // 获取公开模板
    @Query("SELECT * FROM template_table WHERE is_public = 1 ORDER BY rating DESC, use_count DESC")
    fun getPublicTemplates(): Flow<List<TemplateEntity>>
    
    // 搜索模板
    @Query("""
        SELECT * FROM template_table 
        WHERE (title LIKE '%' || :keyword || '%' OR description LIKE '%' || :keyword || '%')
        AND (:category IS NULL OR category = :category)
        ORDER BY use_count DESC, rating DESC
    """)
    fun searchTemplates(keyword: String, category: String? = null): Flow<List<TemplateEntity>>
    
    // 更新模板使用次数
    @Query("UPDATE template_table SET use_count = use_count + 1, updated_at = :timestamp WHERE id = :templateId")
    suspend fun incrementUseCount(templateId: String, timestamp: Long = System.currentTimeMillis())
    
    // 更新模板评分
    @Query("UPDATE template_table SET rating = :rating, updated_at = :timestamp WHERE id = :templateId")
    suspend fun updateRating(templateId: String, rating: Float, timestamp: Long = System.currentTimeMillis())
    
    // 获取热门模板（使用次数最多）
    @Query("SELECT * FROM template_table WHERE is_public = 1 ORDER BY use_count DESC LIMIT :limit")
    fun getPopularTemplates(limit: Int = 10): Flow<List<TemplateEntity>>
    
    // 获取推荐模板（评分最高）
    @Query("SELECT * FROM template_table WHERE is_public = 1 AND rating > 0 ORDER BY rating DESC LIMIT :limit")
    fun getRecommendedTemplates(limit: Int = 10): Flow<List<TemplateEntity>>
    
    // 检查是否存在系统模板
    @Query("SELECT COUNT(*) FROM template_table WHERE is_system = 1")
    suspend fun getSystemTemplateCount(): Int
}