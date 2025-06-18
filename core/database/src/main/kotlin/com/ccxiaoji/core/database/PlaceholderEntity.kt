package com.ccxiaoji.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 占位Entity，仅用于让Room编译通过
 * TODO: 待所有模块迁移完成后删除
 */
@Entity(tableName = "placeholder_table")
data class PlaceholderEntity(
    @PrimaryKey
    val id: String = "placeholder"
)