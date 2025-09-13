package com.ccxiaoji.feature.ledger.domain.repository

import com.ccxiaoji.feature.ledger.domain.model.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    fun getCards(userId: String): Flow<List<Card>>
    fun search(userId: String, query: String): Flow<List<Card>>
    suspend fun getById(id: String): Card?
    suspend fun upsert(card: Card)
    suspend fun delete(id: String)
}

