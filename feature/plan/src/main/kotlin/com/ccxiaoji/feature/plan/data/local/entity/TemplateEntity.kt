package com.ccxiaoji.feature.plan.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * 模板数据库实体
 */
@Entity(tableName = "template_table")
data class TemplateEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "category")
    val category: String, // WORK, STUDY, LIFE, HEALTH, FITNESS, SKILL, PROJECT, OTHER
    
    @ColumnInfo(name = "tags")
    val tags: String, // JSON数组格式
    
    @ColumnInfo(name = "color")
    val color: String,
    
    @ColumnInfo(name = "duration")
    val duration: Int, // 计划持续天数
    
    @ColumnInfo(name = "structure")
    val structure: String, // JSON格式的计划结构，包含子计划和里程碑
    
    @ColumnInfo(name = "use_count")
    val useCount: Int = 0,
    
    @ColumnInfo(name = "rating")
    val rating: Float = 0f,
    
    @ColumnInfo(name = "is_system")
    val isSystem: Boolean = false,
    
    @ColumnInfo(name = "is_public")
    val isPublic: Boolean = true,
    
    @ColumnInfo(name = "created_by")
    val createdBy: String = "system",
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING_SYNC"
)