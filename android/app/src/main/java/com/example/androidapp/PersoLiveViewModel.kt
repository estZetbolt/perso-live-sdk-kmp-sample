package com.example.androidapp

import ai.perso.live.perso.Chat
import ai.perso.live.perso.Session
import ai.perso.live.webrtc.VideoView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.webrtc.SurfaceViewRenderer

class PersoLiveViewModel : ViewModel() {

    private lateinit var session: Session
    private var unsubscribeSessionStatus: (() -> Unit)? = null
    private var unsubscribeChatStatus: (() -> Unit)? = null
    private var unsubscribeChatLog: (() -> Unit)? = null

    private var sessionState = 0 // 0: Initial state(or closed) 1: starting 2: started
    private val _sessionStateLiveData: MutableLiveData<Int> = MutableLiveData(sessionState)
    val sessionStateLiveData: LiveData<Int> = _sessionStateLiveData

    private var chatState = 0 // 0: available 1: recording 2: analyzing 3: AI speaking
    private val _chatStateLiveData: MutableLiveData<Int> = MutableLiveData(chatState)
    val chatStateLiveData: LiveData<Int> = _chatStateLiveData

    private var speakerOn: Boolean = SessionManager.isSpeakerphoneOn()
    private val _speakerOnLiveData: MutableLiveData<Boolean> = MutableLiveData(speakerOn)
    val speakerOnLiveData: LiveData<Boolean> = _speakerOnLiveData

    private var message: String = ""
    private val _messageLiveData: MutableLiveData<String> = MutableLiveData(message)
    val messageLiveData: LiveData<String> = _messageLiveData

    private val _chatLogLiveData: MutableLiveData<List<Chat>> = MutableLiveData(emptyList())
    val chatLogLiveData: LiveData<List<Chat>> = _chatLogLiveData

    private val _sessionEventLiveData = MutableLiveData<OneTimeEvent<SessionEvent>>()
    val sessionEventLiveData: LiveData<OneTimeEvent<SessionEvent>> = _sessionEventLiveData

    private var recording: Boolean = false

    fun onSpeakerOnClicked() {
        if (speakerOn) {
            SessionManager.setEarpieceMode()
        } else {
            SessionManager.setSpeakerMode()
        }

        speakerOn = SessionManager.isSpeakerphoneOn()
        _speakerOnLiveData.value = speakerOn
    }

    fun onSendMessageClicked() {
        session.processChat(message.trim())
        onMessageInputted("")
    }

    fun onMessageInputted(message: String) {
        this.message = message
        _messageLiveData.value = this.message
    }

    fun onTtstfMessageSubmit() {
        session.processTTSTF(message)
        onMessageInputted("")
    }

    fun onVoiceChatClicked() {
        if (recording) {
            session.stopVoiceChat()
            recording = false
        } else {
            session.startVoiceChat()
            recording = true
        }
    }

    fun onStopSpeechClicked() {
        session.clearBuffer()
    }

    fun onCloseClicked() {
        SessionManager.stopSession()
    }

    fun onSessionStarted(surfaceViewRenderer: SurfaceViewRenderer) {
        session.setSrc(VideoView(surfaceViewRenderer))

        applySessionState(2)
    }

    private fun startSession() {
        refreshChatLog(emptyList())
        this.unsubscribeChatLog = session.subscribeChatLog { chatLog ->
            viewModelScope.launch(Dispatchers.Main) {
                refreshChatLog(chatLog)
            }
        }
        this.unsubscribeChatStatus = session.subscribeMicStatus { status ->
            viewModelScope.launch(Dispatchers.Main) {
                applyChatState(status)
            }
        }
        this.unsubscribeSessionStatus = session.subscribeSessionStatus { status ->
            viewModelScope.launch(Dispatchers.Main) {
                if (status.live) {
                    _sessionEventLiveData.value = OneTimeEvent(SessionEvent.Live)
                } else {
                    if (status.code == 408) {
                        _sessionEventLiveData.value = OneTimeEvent(
                            SessionEvent.Error(
                                status.code,
                                "Timeout.\nSessionID: ${session.getSessionId()}"
                            )
                        )
                        applySessionState(0)
                    } else if (status.code == 200) {
                        _sessionEventLiveData.value = OneTimeEvent(SessionEvent.Stopped)
                    }
                }
            }
        }
    }

    private fun refreshChatLog(chatList: List<Chat>) {
        _chatLogLiveData.value = chatList
    }

    fun start() {
        if (SessionManager.hasSession().not()) {
            return
        }

        session = SessionManager.getSession()
        applySessionState(1)
        startSession()
    }

    private fun applySessionState(sessionState: Int) {
        this.sessionState = sessionState
        _sessionStateLiveData.value = this.sessionState
    }

    private fun applyChatState(status: Int) {
        this.chatState = status
        _chatStateLiveData.value = chatState
    }

    override fun onCleared() {
        super.onCleared()
        unsubscribeSessionStatus?.invoke()
        unsubscribeSessionStatus = null
        unsubscribeChatStatus?.invoke()
        unsubscribeChatStatus = null
        unsubscribeChatLog?.invoke()
        unsubscribeChatLog = null
    }

}

sealed class SessionEvent {
    object Live : SessionEvent()
    object Stopped : SessionEvent()
    class Error(val code: Int, val message: String) : SessionEvent()
}