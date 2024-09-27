package com.example.androidapp

import ai.perso.live.AllSetting
import ai.perso.live.perso.ApiError
import ai.perso.live.perso.Timeout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PersoConfigViewModel : ViewModel() {

    private val apiServer = "https://live-api.perso.ai"
    private val apiKey = "plak-357ac36761a8d9b28d3cf6cb59eae297"

    private var allSetting = AllSetting(
        llms = emptyList(),
        ttss = emptyList(),
        modelStyles = emptyList(),
        prompts = emptyList(),
        documents = emptyList(),
        backgroundImages = emptyList()
    )

    private val _eventLiveData = MutableLiveData<OneTimeEvent<Event>>()
    val eventLiveData: LiveData<OneTimeEvent<Event>> = _eventLiveData

    private val _readyToStartLiveData = MutableLiveData(false)
    val readyToStartLiveData: LiveData<Boolean> = _readyToStartLiveData

    private val _introMessageLiveData = MutableLiveData("")
    val introMessageLiveData: LiveData<String> = _introMessageLiveData

    private var llmIndex: Int = -1
    private var ttsIndex: Int = -1
    private var modelStyleIndex: Int = -1
    private var promptIndex: Int = -1
    private var documentIndex: Int = -1
    private var backgroundImageIndex: Int = -1

    private var useIntro: Boolean = false

    private var chatbotLeft: Int = 0
    private var chatbotTop: Int = 0
    private var chatbotHeight: Int = 100

    fun start() = viewModelScope.launch {
        _eventLiveData.value = OneTimeEvent(Event.ShowLoading)

        try {
            allSetting = SessionManager.getAllSettings(apiServer, apiKey)

            val initialViewState = InitialViewState(
                llms = allSetting.llms.map { it.name },
                ttss = allSetting.ttss.map { it.name },
                modelStyles = allSetting.modelStyles.map { it.name },
                backgroundImages = allSetting.backgroundImages?.let { backgroundImages ->
                    mutableListOf<String>().apply {
                        add("")
                        addAll(backgroundImages.map { it.title })
                    }
                } ?: emptyList(),
                prompts = allSetting.prompts.map { it.name },
                documents = allSetting.documents?.let { documents ->
                    mutableListOf<String>().apply {
                        add("")
                        addAll(documents.map { it.title })
                    }
                } ?: emptyList(),
                chatbotLeft = SeekBarState(-100, 100, 0),
                chatbotTop = SeekBarState(0, 100, 0),
                chatbotHeight = SeekBarState(0, 500, 100)
            )

            _eventLiveData.value = OneTimeEvent(Event.ViewInit(initialViewState))
        } catch (t: Throwable) {
            handleError(t)
        }

        _eventLiveData.value = OneTimeEvent(Event.HideLoading)
    }

    fun onSessionStartClicked() = viewModelScope.launch {
        _eventLiveData.value = OneTimeEvent(Event.ShowLoading)

        try {
            SessionManager.createSession(
                apiServer = apiServer,
                apiKey = apiKey,
                llmKey = allSetting.llms[llmIndex].name,
                ttsKey = allSetting.ttss[ttsIndex].name,
                modelStyleKey = allSetting.modelStyles[modelStyleIndex].name,
                promptKey = allSetting.prompts[promptIndex].promptId,
                documentKey = allSetting.documents?.getOrNull(documentIndex - 1)?.documentId,
                backgroundImageKey = allSetting.backgroundImages?.getOrNull(backgroundImageIndex - 1)?.backgroundImageId,
                paddingLeft = chatbotLeft / 100f,
                paddingTop = chatbotTop / 100f,
                paddingHeight = chatbotHeight / 100f,
                width = 1080,
                height = 1920,
                useIntro = useIntro
            )

            _eventLiveData.value = OneTimeEvent(Event.SessionCreated)
        } catch (t: Throwable) {
            handleError(t)
        }

        _eventLiveData.value = OneTimeEvent(Event.HideLoading)
    }

    fun onLlmSelected(index: Int) {
        llmIndex = index
        refreshReadyToStart()
    }

    fun onTtsSelected(index: Int) {
        ttsIndex = index
        refreshReadyToStart()
    }

    fun onModelStyleSelected(index: Int) {
        modelStyleIndex = index
        refreshReadyToStart()
    }

    fun onBackgroundSelected(index: Int) {
        backgroundImageIndex = index
        refreshReadyToStart()
    }

    fun onPromptSelected(index: Int) {
        promptIndex = index
        _introMessageLiveData.value = allSetting.prompts[promptIndex].introMessage ?: ""
        refreshReadyToStart()
    }

    fun onUseIntroCheckChanged(checked: Boolean) {
        useIntro = checked
    }

    fun onDocumentSelected(index: Int) {
        documentIndex = index
        refreshReadyToStart()
    }

    fun onChatbotLeftChanged(value: Int) {
        chatbotLeft = value
    }

    fun onChatbotTopChanged(value: Int) {
        chatbotTop = value
    }

    fun onChatbotHeightChanged(value: Int) {
        chatbotHeight = value
    }

    private fun refreshReadyToStart() {
        _readyToStartLiveData.value = llmIndex > -1 && ttsIndex > -1 && modelStyleIndex > -1 &&
                backgroundImageIndex > -1 && documentIndex > -1
    }

    private fun handleError(t: Throwable) {
        _eventLiveData.value = OneTimeEvent(
            when (t) {
                is ApiError -> {
                    Event.HandledError(t.message)
                }
                is Timeout -> {
                    Event.HandledError(t.message)
                }
                else -> {
                    Event.UnhandledError(t.message ?: "")
                }
            }
        )
    }

    data class InitialViewState(
        val llms: List<String>,
        val ttss: List<String>,
        val modelStyles: List<String>,
        val backgroundImages: List<String>,
        val prompts: List<String>,
        val documents: List<String>,
        val chatbotLeft: SeekBarState,
        val chatbotTop: SeekBarState,
        val chatbotHeight: SeekBarState
    )

    data class SeekBarState(
        val min: Int,
        val max: Int,
        val progress: Int
    )

    sealed class Event {
        object ShowLoading : Event()
        object HideLoading : Event()
        class ViewInit(val initialViewInit: InitialViewState) : Event()
        object SessionCreated : Event()
        class HandledError(val message: String) : Event()
        class UnhandledError(val message: String) : Event()
    }

}