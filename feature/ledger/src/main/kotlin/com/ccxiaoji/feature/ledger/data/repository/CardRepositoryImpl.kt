package com.ccxiaoji.feature.ledger.data.repository

import com.ccxiaoji.feature.ledger.data.local.dao.CardDao
import com.ccxiaoji.feature.ledger.data.local.entity.CardEntity
import com.ccxiaoji.feature.ledger.domain.model.Card
import com.ccxiaoji.feature.ledger.domain.model.CardType
import com.ccxiaoji.feature.ledger.domain.model.InstitutionType
import com.ccxiaoji.feature.ledger.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val dao: CardDao
) : CardRepository {

    override fun getCards(userId: String): Flow<List<Card>> =
        dao.getCards(userId).map { list -> list.map { it.toDomain() } }

    override fun search(userId: String, query: String): Flow<List<Card>> =
        dao.search(userId, query).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): Card? = dao.getById(id)?.toDomain()

    override suspend fun upsert(card: Card) {
        val now = System.currentTimeMillis()
        val entity = CardEntity(
            id = if (card.id.isBlank()) UUID.randomUUID().toString() else card.id,
            userId = card.userId,
            name = card.name,
            cardType = card.cardType.name,
            maskedNumber = card.maskedNumber,
            frontImagePath = card.frontImagePath,
            backImagePath = card.backImagePath,
            expiryMonth = card.expiryMonth,
            expiryYear = card.expiryYear,
            holderName = card.holderName,
            institutionName = card.institutionName,
            institutionType = when (card.institutionType) {
                InstitutionType.BANK -> "BANK"
                InstitutionType.BROKER -> "BROKER"
                InstitutionType.NONE, null -> null
            },
            note = card.note,
            createdAt = card.createdAt.takeIf { it > 0 } ?: now,
            updatedAt = now,
            isDeleted = false
        )
        dao.insert(entity)
    }

    override suspend fun delete(id: String) {
        dao.softDelete(id, System.currentTimeMillis())
    }

    private fun CardEntity.toDomain() = Card(
        id = id,
        userId = userId,
        name = name,
        cardType = CardType.fromString(cardType),
        maskedNumber = maskedNumber,
        frontImagePath = frontImagePath,
        backImagePath = backImagePath,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        holderName = holderName,
        institutionName = institutionName,
        institutionType = when (institutionType) {
            "BANK" -> InstitutionType.BANK
            "BROKER" -> InstitutionType.BROKER
            else -> null
        },
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
