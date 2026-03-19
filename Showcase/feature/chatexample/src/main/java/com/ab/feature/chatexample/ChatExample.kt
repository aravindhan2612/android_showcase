package com.ab.feature.chatexample

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random


interface MessagingRepository {
    val incomingMessages: Flow<IncomingMessage>
    val messageResults: Flow<SendConfirmation>

    suspend fun sendMessage(message: OutgoingMessage)
}

sealed interface IncomingMessage
data class TextualMessage(val message: String) : IncomingMessage
data class ImageMessage(@ColorInt val color: Int) : IncomingMessage

data class OutgoingMessage(val message: String, val id: UUID)

data class SendConfirmation(val id: UUID = UUID.randomUUID(), val result: Result = Result.Success) {
    sealed interface Result {
        data object Success : Result
        data object Failure : Result
    }
}

class MessagingRepositoryImpl(
    private val networkDelay: Long = 700L,
    private val failureProbability: Double = 0.15
) : MessagingRepository {

    private val _incomingMessages = MutableSharedFlow<IncomingMessage>(replay = 0)
    private val _messageResults = MutableSharedFlow<SendConfirmation>(replay = 0)

    private val mutex = Mutex()

    override val incomingMessages: Flow<IncomingMessage> = _incomingMessages.asSharedFlow()
    override val messageResults: Flow<SendConfirmation> = _messageResults.asSharedFlow()

    override suspend fun sendMessage(message: OutgoingMessage) = withContext(Dispatchers.IO) {
        mutex.withLock {
            delay(networkDelay)
            val failed = Random.nextDouble() < failureProbability
            val confirmation = if (failed) {
                SendConfirmation(message.id, SendConfirmation.Result.Failure)
            } else {
                SendConfirmation(message.id, SendConfirmation.Result.Success)
            }
            _messageResults.emit(confirmation)

            if (!failed) {
                _incomingMessages.emit(TextualMessage("Echo: ${message.message}"))
            }
        }
    }
}

class ObserveIncomingMessagesUseCase(private val messagingRepository: MessagingRepository) {
    operator fun invoke() = messagingRepository.incomingMessages
}

class ObserveSendConfirmationsUseCase(private val messageRepository: MessagingRepository) {
    operator fun invoke() = messageRepository.messageResults
}

class SendMessageUseCase(private val messageRepository: MessagingRepository) {
    suspend operator fun invoke(message: OutgoingMessage) = messageRepository.sendMessage(message)
}

sealed interface UIIntent {
    data class InputChange(val text: String) : UIIntent
    object SendClicked : UIIntent
    data class RetrySend(val id: UUID, val message: String) : UIIntent
    object Load : UIIntent
}

sealed interface UIEffect {
    data class ShowToast(val message: String) : UIEffect
    object ScrollToBottom : UIEffect
}

sealed class UIMessage {
    data class IncomingText(val message: String) : UIMessage()
    data class IncomingImage(@ColorInt val color: Int) : UIMessage()
    data class OutgoingUIMessage(val id: UUID, val message: String, val status: OutgoingStatus) :
        UIMessage()
}

enum class OutgoingStatus { PENDING, SUCCESS, FAILURE }
data class UiState(
    val messages: List<UIMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false
)

class ChatViewModel(
    private val observeIncoming: ObserveIncomingMessagesUseCase,
    private val observeSendConfirmations: ObserveSendConfirmationsUseCase,
    private val sendMessage: SendMessageUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<UIEffect>(extraBufferCapacity = 4)
    val effects = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeIncoming().collect { incoming ->
                val uiMessage = when (incoming) {
                    is ImageMessage -> UIMessage.IncomingImage(incoming.color)
                    is TextualMessage -> UIMessage.IncomingText(incoming.message)
                }
                _state.update { state ->
                    state.copy(messages = state.messages + uiMessage)
                }
                _effects.tryEmit(UIEffect.ScrollToBottom)
            }
        }
        viewModelScope.launch {
            observeSendConfirmations().collect { confirmation ->
                _state.update { s ->
                    val msgs = s.messages.map { m ->
                        if (m is UIMessage.OutgoingUIMessage && m.id == confirmation.id) {
                            val status = when (confirmation.result) {
                                SendConfirmation.Result.Success -> OutgoingStatus.SUCCESS
                                SendConfirmation.Result.Failure -> OutgoingStatus.FAILURE
                            }
                            m.copy(status = status)
                        } else m
                    }
                    s.copy(messages = msgs)
                }
                // optionally emit toast on failure
                if (confirmation.result is SendConfirmation.Result.Failure) {
                    _effects.tryEmit(UIEffect.ShowToast("Failed to send message"))
                }
            }
        }
    }

    fun process(intent: UIIntent) {
        when (intent) {
            is UIIntent.InputChange -> {
                _state.update { it.copy(inputText = intent.text) }
            }

            UIIntent.Load -> Unit
            is UIIntent.RetrySend -> {
                val id = intent.id
                val message = intent.message
                _state.update { s ->
                    s.copy(messages = s.messages.map { m ->
                        if (m is UIMessage.OutgoingUIMessage && m.id == id) m.copy(status = OutgoingStatus.PENDING) else m
                    })
                }
                viewModelScope.launch {
                    try {
                        sendMessage(OutgoingMessage(id = id, message = message))
                    } catch (e: Exception) {
                        _state.update { s ->
                            s.copy(messages = s.messages.map { m ->
                                if (m is UIMessage.OutgoingUIMessage && m.id == id) m.copy(status = OutgoingStatus.FAILURE) else m
                            })
                        }
                        _effects.tryEmit(UIEffect.ShowToast("Retry failed"))
                    }
                }
            }

            UIIntent.SendClicked -> {
                val current = _state.value.inputText.trim()
                if (current.isEmpty()) return
                val id = UUID.randomUUID()
                val outgoingUi = UIMessage.OutgoingUIMessage(
                    id = id,
                    message = current,
                    status = OutgoingStatus.PENDING
                )
                _state.update { s ->
                    s.copy(
                        messages = s.messages + outgoingUi,
                        inputText = "",
                        isSending = true
                    )
                }

                viewModelScope.launch {
                    try {
                        sendMessage(OutgoingMessage(message = current, id = id))
                    } catch (e: Exception) {
                        _state.update { s ->
                            s.copy(
                                messages = s.messages.map { m ->
                                    if (m is UIMessage.OutgoingUIMessage && m.id == id) m.copy(
                                        status = OutgoingStatus.FAILURE
                                    ) else m
                                }
                            )
                        }
                        _effects.tryEmit(UIEffect.ShowToast("Send failed locally: ${e.message}"))
                    } finally {
                        _state.update {
                            it.copy(isSending = false)
                        }
                        _effects.tryEmit(UIEffect.ScrollToBottom)
                    }
                }
            }
        }

    }
}

@Composable
fun ChatScreen() {
    val repository = remember { MessagingRepositoryImpl() }
    val viewModel = remember {
        ChatViewModel(
            ObserveIncomingMessagesUseCase(repository),
            ObserveSendConfirmationsUseCase(repository),
            SendMessageUseCase(repository)
        )
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                UIEffect.ScrollToBottom -> {
                    listState.animateScrollToItem(state.messages.size - 1)
                }

                is UIEffect.ShowToast -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(effect.message)
                    }
                }

            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            )
        },
        bottomBar = {
            BottomAppBar {
                ChatInput(
                    text = state.inputText,
                    onTextChanged = { viewModel.process(UIIntent.InputChange(it)) },
                    onSend = { viewModel.process(UIIntent.SendClicked) },
                    isSending = state.isSending
                )
            }

        }

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MessageList(
                state = listState,
                messages = state.messages,
                onRetry = { id, message -> viewModel.process(UIIntent.RetrySend(id, message)) }
            )
        }
    }
}

@Composable
fun MessageList(
    state: LazyListState,
    messages: List<UIMessage>, onRetry: (UUID, String) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        items(messages) { message ->
            when (message) {
                is UIMessage.IncomingImage -> IncomingImageItem(message.color)
                is UIMessage.IncomingText -> IncomingTextItem(message.message)
                is UIMessage.OutgoingUIMessage -> OutgoingItem(message, onRetry)
            }
            Spacer(modifier = Modifier.height(6.dp))

        }
    }
}

@Composable
fun IncomingTextItem(text: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Card(modifier = Modifier.wrapContentWidth()) {
            Text(text = text, modifier = Modifier.padding(10.dp))
        }
    }
}

@Composable
fun IncomingImageItem(@ColorInt color: Int) {
    // very simple representation of an image message as a colored box
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(Color(color))
    )
}

@Composable
fun OutgoingItem(msg: UIMessage.OutgoingUIMessage, onRetry: (UUID, String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Column(horizontalAlignment = Alignment.End) {
            Card(modifier = Modifier.wrapContentWidth()) {
                Text(
                    text = msg.message,
                    modifier = Modifier.padding(10.dp),
                    textAlign = TextAlign.End
                )
            }
            when (msg.status) {
                OutgoingStatus.PENDING -> Text(
                    "Sending...",
                    style = MaterialTheme.typography.labelSmall
                )

                OutgoingStatus.SUCCESS -> Text("Sent", style = MaterialTheme.typography.labelSmall)
                OutgoingStatus.FAILURE -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Retry",
                            modifier = Modifier
                                .clickable { onRetry(msg.id, msg.message) }
                                .padding(4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ChatInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Write a message") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSend, enabled = text.isNotBlank() && !isSending) {
            Text("Send")
        }
    }
}