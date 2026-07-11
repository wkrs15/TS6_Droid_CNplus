package dev.tsdroid.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.tsdroid.data.ChatMessage
import dev.tsdroid.data.FileAttachment
import dev.tsdroid.viewmodel.DownloadState
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val INITIAL_PAGE = 15
private const val PAGE_SIZE = 20

private val dateHeaderFormat = SimpleDateFormat("yyyy年M月d日", Locale.CHINESE)
private val todayFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

private fun formatDateHeader(timestamp: Long): String {
    val cal = Calendar.getInstance()
    val today = Calendar.getInstance()
    val msgDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    return when {
        msgDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        msgDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "今天"
        msgDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
        msgDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) - 1 -> "昨天"
        else -> dateHeaderFormat.format(Date(timestamp))
    }
}

/** Wrapper for a chat item that can be either a message or a date separator. */
private sealed class ChatListItem {
    data class Message(val msg: ChatMessage) : ChatListItem()
    data class DateSeparator(val label: String) : ChatListItem()
}

@Composable
fun ChatView(
    messages: List<ChatMessage>,
    showLinkThumbnails: Boolean = false,
    autoLoadImages: Boolean = true,
    onDownload: ((FileAttachment) -> StateFlow<DownloadState>)? = null,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var visibleCount by remember { mutableIntStateOf(INITIAL_PAGE) }
    val shape = MaterialTheme.shapes.small

    // Build display list with date separators inserted
    val displayItems = remember(messages, visibleCount) {
        val sliced = messages.takeLast(visibleCount.coerceAtMost(messages.size))
        val items = mutableListOf<ChatListItem>()
        var lastDay = -1L
        // We iterate in chronological order but display reversed
        for (msg in sliced) {
            val day = msg.timestamp / 86400000L // day boundary
            if (day != lastDay) {
                lastDay = day
                items.add(ChatListItem.DateSeparator(formatDateHeader(msg.timestamp)))
            }
            items.add(ChatListItem.Message(msg))
        }
        // Reverse for display (newest at bottom / index 0)
        items.asReversed()
    }

    // Auto-scroll to bottom on new messages (only if already near bottom)
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && listState.firstVisibleItemIndex <= 3) {
            listState.animateScrollToItem(0)
        }
    }

    // Load more when scrolling near the top (high indices in reversed layout)
    val totalMessages by rememberUpdatedState(messages.size)
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to info.totalItemsCount
        }.collect { (lastVisible, totalItems) ->
            if (totalItems > 0 && lastVisible >= totalItems - 3 && visibleCount < totalMessages) {
                visibleCount = (visibleCount + PAGE_SIZE).coerceAtMost(totalMessages)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        reverseLayout = true,
    ) {
        items(displayItems, key = { item ->
            when (item) {
                is ChatListItem.Message -> "msg_${item.msg.timestamp}_${item.msg.senderId}_${item.msg.sender}"
                is ChatListItem.DateSeparator -> "sep_${item.label}"
            }
        }) { item ->
            when (item) {
                is ChatListItem.Message -> {
                    MessageBubble(
                        message = item.msg,
                        showLinkThumbnails = showLinkThumbnails,
                        autoLoadImages = autoLoadImages,
                        onDownload = onDownload,
                    )
                }
                is ChatListItem.DateSeparator -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(shape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
