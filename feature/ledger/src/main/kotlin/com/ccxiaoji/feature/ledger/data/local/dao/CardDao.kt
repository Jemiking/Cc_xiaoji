package com.ccxiaoji.feature.ledger.data.local.dao

import androidx.room.*
import com.ccxiaoji.feature.ledger.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE isDeleted = 0 AND userId = :userId ORDER BY updatedAt DESC")
    fun getCards(userId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE isDeleted = 0 AND id = :id LIMIT 1")
    suspend fun getById(id: String): CardEntity?

    @Query("SELECT * FROM cards WHERE isDeleted = 0 AND userId = :userId AND (name LIKE '%' || :query || '%' OR note LIKE '%' || :query || '%') ORDER BY updatedAt DESC")
    fun search(userId: String, query: String): Flow<List<CardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CardEntity)

    @Update
    suspend fun update(entity: CardEntity)

    @Query("UPDATE cards SET isDeleted = 1, updatedAt = :updateTime WHERE id = :id")
    suspend fun softDelete(id: String, updateTime: Long)
}

