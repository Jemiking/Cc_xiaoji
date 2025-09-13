package com.ccxiaoji.feature.ledger.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.feature.ledger.domain.model.Card
import com.ccxiaoji.feature.ledger.domain.model.CardType
import com.ccxiaoji.feature.ledger.domain.usecase.card.*
import com.ccxiaoji.shared.user.api.UserApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class CardUiState(
    val cards: List<Card> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CardViewModel @Inject constructor(
    private val getCards: GetCardsUseCase,
    private val searchCards: SearchCardsUseCase,
    private val getCardById: GetCardByIdUseCase,
    private val upsertCard: UpsertCardUseCase,
    private val deleteCard: DeleteCardUseCase,
    private val userApi: UserApi
) : ViewModel() {

    private val mutableState = MutableStateFlow(CardUiState())
    val uiState: StateFlow<CardUiState> = mutableState.asStateFlow()

    private val currentUserId: String get() = userApi.getCurrentUserId()

    init {
        reload()
    }

    fun reload() {
        viewModelScope.launch {
            getCards(currentUserId).collect { list ->
                mutableState.update { it.copy(cards = list, isLoading = false, error = null) }
            }
        }
    }

    fun updateQuery(query: String) {
        mutableState.update { it.copy(query = query) }
        viewModelScope.launch {
            if (query.isBlank()) {
                reload()
            } else {
                searchCards(currentUserId, query).collect { list ->
                    mutableState.update { it.copy(cards = list) }
                }
            }
        }
    }

    fun saveCard(
        id: String? = null,
        name: String,
        cardType: CardType,
        maskedNumber: String?,
        holderName: String?,
        institutionName: String?,
        institutionType: com.ccxiaoji.feature.ledger.domain.model.InstitutionType?,
        frontImagePath: String?,
        backImagePath: String?,
        expiryMonth: Int?,
        expiryYear: Int?,
        note: String?
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val masked = sanitizeAndMask(maskedNumber)
            val card = Card(
                id = id ?: UUID.randomUUID().toString(),
                userId = currentUserId,
                name = name,
                cardType = cardType,
                maskedNumber = masked,
                frontImagePath = frontImagePath,
                backImagePath = backImagePath,
                expiryMonth = expiryMonth,
                expiryYear = expiryYear,
                holderName = holderName,
                institutionName = institutionName,
                institutionType = institutionType,
                note = note,
                createdAt = now,
                updatedAt = now
            )
            upsertCard(card)
        }
    }

    fun deleteCard(id: String) {
        viewModelScope.launch {
            deleteCard.invoke(id)
        }
    }

    // 将用户输入的可能为完整卡号的内容转为掩码：保留末4位，其余以*分组显示
    private fun sanitizeAndMask(input: String?): String? {
        if (input.isNullOrBlank()) return null
        val digits = input.filter { it.isDigit() }
        // 若长度像卡号（12-19位），进行掩码；否则直接返回原输入（允许用户自定义掩码样式）
        if (digits.length in 12..19) {
            val last4 = digits.takeLast(4)
            return "**** **** **** $last4"
        }
        return input
    }
}
