package com.ccxiaoji.shared.user.domain.model

import com.ccxiaoji.shared.user.api.UserInfo

/**
 * 用户领域模型
 */
data class User(
    val id: String,
    val email: String,
    val createdAt: Long
) {
    /**
     * 转换为API模型
     */
    fun toUserInfo(): UserInfo {
        return UserInfo(
            id = id,
            email = email,
            createdAt = createdAt
        )
    }
}