package com.ccxiaoji.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean = false
)