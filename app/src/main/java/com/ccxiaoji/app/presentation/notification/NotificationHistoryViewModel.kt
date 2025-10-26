package com.ccxiaoji.app.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ccxiaoji.core.database.dao.NotificationQueueDao
import com.ccxiaoji.core.database.entity.NotificationQueueEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    private val queueDao: NotificationQueueDao
) : ViewModel() {

    private val _items = MutableStateFlow<List<NotificationQueueEntity>>(emptyList())
    val items: StateFlow<List<NotificationQueueEntity>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            queueDao.observeRecent(limit = 200).collectLatest { list ->
                _items.value = list
            }
        }
    }
}

