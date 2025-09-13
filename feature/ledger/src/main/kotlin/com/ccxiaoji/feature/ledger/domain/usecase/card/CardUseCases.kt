package com.ccxiaoji.feature.ledger.domain.usecase.card

import com.ccxiaoji.feature.ledger.domain.model.Card
import com.ccxiaoji.feature.ledger.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(private val repo: CardRepository) {
    operator fun invoke(userId: String): Flow<List<Card>> = repo.getCards(userId)
}

class SearchCardsUseCase @Inject constructor(private val repo: CardRepository) {
    operator fun invoke(userId: String, query: String): Flow<List<Card>> = repo.search(userId, query)
}

class GetCardByIdUseCase @Inject constructor(private val repo: CardRepository) {
    suspend operator fun invoke(id: String) = repo.getById(id)
}

class UpsertCardUseCase @Inject constructor(private val repo: CardRepository) {
    suspend operator fun invoke(card: Card) = repo.upsert(card)
}

class DeleteCardUseCase @Inject constructor(private val repo: CardRepository) {
    suspend operator fun invoke(id: String) = repo.delete(id)
}

