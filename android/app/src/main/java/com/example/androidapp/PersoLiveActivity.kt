package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class PersoLiveActivity : AppCompatActivity() {

    private lateinit var inputMethodContainer: ViewGroup
    private lateinit var speakerOn: Button
    private lateinit var message: EditText
    private lateinit var sendMessage: Button
    private lateinit var voiceChat: Button
    private lateinit var stopSpeech: Button
    private lateinit var chatState: TextView
    private lateinit var close: Button
    private lateinit var showChatLog: Button
    private lateinit var chatLog: RecyclerView
    private lateinit var chatLogContainer: ViewGroup
    private lateinit var closeChatLog: Button

    private val viewModel: PersoLiveViewModel by viewModels()

    private lateinit var keyboardHeightProviderImpl: KeyboardHeightProviderImpl

    private val onBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {}
    }

    private val chatLogAdapter = ChatLogAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perso_live)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        keyboardHeightProviderImpl = KeyboardHeightProviderImpl(this)

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        initView()
        initObserver()

        lifecycleScope.launch {
            viewModel.start()
        }
    }

    private fun initView() {
        inputMethodContainer = findViewById(R.id.inputMethodContainer)
        speakerOn = findViewById(R.id.speakerOn)
        message = findViewById(R.id.message)
        sendMessage = findViewById(R.id.sendMessage)
        voiceChat = findViewById(R.id.voiceChat)
        stopSpeech = findViewById(R.id.stopSpeech)
        chatState = findViewById(R.id.chatState)
        close = findViewById(R.id.close)
        showChatLog = findViewById(R.id.showChatLog)
        chatLog = findViewById(R.id.chatLog)
        chatLogContainer = findViewById(R.id.chatLogContainer)
        closeChatLog = findViewById(R.id.closeChatLog)

        speakerOn.setOnClickListener {
            viewModel.onSpeakerOnClicked()
        }
        message.addTextChangedListener {
            viewModel.onMessageInputted(it.toString())
        }
        sendMessage.setOnClickListener {
            viewModel.onSendMessageClicked()
        }
        voiceChat.setOnClickListener {
            viewModel.onVoiceChatClicked()
        }
        stopSpeech.setOnClickListener {
            viewModel.onStopSpeechClicked()
        }
        close.setOnClickListener {
            viewModel.onCloseClicked()
        }
        showChatLog.setOnClickListener {
            chatLogContainer.isVisible = true
        }
        chatLog.adapter = chatLogAdapter
        closeChatLog.setOnClickListener {
            chatLogContainer.isVisible = false
        }
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                keyboardHeightProviderImpl.keyboardHeight.collect { keyboardHeight ->
                    inputMethodContainer.layoutParams = (inputMethodContainer.layoutParams as ConstraintLayout.LayoutParams).apply {
                        bottomMargin = keyboardHeight
                    }
                }
            }
        }

        viewModel.sessionStateLiveData.observe(this) { sessionState ->
            when (sessionState) {
                0,
                1 -> {
                    inputMethodContainer.isVisible = false
                    close.isVisible = false
                }
                2 -> {
                    inputMethodContainer.isVisible = true
                    close.isVisible = true
                }
            }
        }
        viewModel.chatStateLiveData.observe(this) { state ->
            when (state) {
                0 -> {
                    chatState.text = ""
                    voiceChat.isEnabled = true
                    voiceChat.setText(R.string.voice)
                    message.isEnabled = true
                    sendMessage.isEnabled = true
                    sendMessage.setText(R.string.send)
                    stopSpeech.isVisible = false
                }
                1 -> {
                    chatState.text = ""
                    voiceChat.isEnabled = true
                    voiceChat.setText(R.string.stop)
                    message.isEnabled = false
                    sendMessage.isEnabled = false
                    sendMessage.setText(R.string.send)
                    stopSpeech.isVisible = false
                }
                2 -> {
                    chatState.setText(R.string.analyzing)
                    voiceChat.isEnabled = false
                    voiceChat.setText(R.string.voice)
                    message.isEnabled = false
                    sendMessage.isEnabled = false
                    sendMessage.setText(R.string.send)
                    stopSpeech.isVisible = false
                }
                3 -> {
                    chatState.setText(R.string.ai_speaking)
                    voiceChat.isEnabled = false
                    voiceChat.setText(R.string.voice)
                    message.isEnabled = false
                    sendMessage.isEnabled = false
                    sendMessage.setText(R.string.send)
                    stopSpeech.isVisible = true
                }
            }
        }
        viewModel.speakerOnLiveData.observe(this) { enableSpeaker ->
            speakerOn.setText(
                if (enableSpeaker) {
                    R.string.speaker_off
                } else {
                    R.string.speaker_on
                }
            )
        }
        viewModel.messageLiveData.observe(this) { message ->
            if (this.message.text.toString() != message) {
                this.message.setText(message)
            }
        }
        viewModel.sessionEventLiveData.observe(this) { sessionEvent ->
            sessionEvent.getValue()?.let {
                when (it) {
                    SessionEvent.Live -> {
                        ContextCompat.startForegroundService(
                            this,
                            Intent(this, PersoLiveService::class.java)
                        )
                        viewModel.onSessionStarted(findViewById(R.id.fullscreen_video_view))
                    }
                    SessionEvent.Stopped -> {
                        finishPersoLive()
                    }
                    is SessionEvent.Error -> {
                        AlertDialog.Builder(this)
                            .setTitle("Error!")
                            .setMessage(it.message)
                            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                                dialogInterface.dismiss()
                                finishPersoLive()
                            }
                            .show()
                    }
                }
            }
        }
        viewModel.chatLogLiveData.observe(this) { chatLog ->
            chatLogAdapter.replaceChatLog(chatLog)
        }
    }

    private fun finishPersoLive() {
        stopService(Intent(this, PersoLiveService::class.java))
        finish()
    }

}