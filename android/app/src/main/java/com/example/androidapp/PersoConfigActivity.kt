package com.example.androidapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

class PersoConfigActivity : AppCompatActivity() {

    private lateinit var sessionStart: Button
    private lateinit var llm: Spinner
    private lateinit var tts: Spinner
    private lateinit var modelStyle: Spinner
    private lateinit var background: Spinner
    private lateinit var prompt: Spinner
    private lateinit var introMessage: TextView
    private lateinit var useIntro: CheckBox
    private lateinit var document: Spinner
    private lateinit var chatbotLeft: SeekBar
    private lateinit var chatbotLeftValue: TextView
    private lateinit var chatbotTop: SeekBar
    private lateinit var chatbotTopValue: TextView
    private lateinit var chatbotHeight: SeekBar
    private lateinit var chatbotHeightValue: TextView
    private lateinit var loading: ViewGroup

    private lateinit var llmAdapter: ArrayAdapter<String>
    private lateinit var ttsAdapter: ArrayAdapter<String>
    private lateinit var modelStyleAdapter: ArrayAdapter<String>
    private lateinit var backgroundAdapter: ArrayAdapter<String>
    private lateinit var promptAdapter: ArrayAdapter<String>
    private lateinit var documentAdapter: ArrayAdapter<String>

    private val viewModel: PersoConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perso_config)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initView()
        initObserver()

        if (SessionManager.hasSession()) {
            startActivity(Intent(this, PersoLiveActivity::class.java))
        } else {
            viewModel.start()
        }
    }

    private fun initView() {
        sessionStart = findViewById(R.id.sessionStart)
        llm = findViewById(R.id.llm)
        tts = findViewById(R.id.tts)
        modelStyle = findViewById(R.id.modelStyle)
        background = findViewById(R.id.background)
        prompt = findViewById(R.id.prompt)
        introMessage = findViewById(R.id.introMessage)
        useIntro = findViewById(R.id.useIntro)
        document = findViewById(R.id.document)
        chatbotLeft = findViewById(R.id.chatbotLeft)
        chatbotLeftValue = findViewById(R.id.chatbotLeftValue)
        chatbotTop = findViewById(R.id.chatbotTop)
        chatbotTopValue = findViewById(R.id.chatbotTopValue)
        chatbotHeight = findViewById(R.id.chatbotHeight)
        chatbotHeightValue = findViewById(R.id.chatbotHeightValue)
        loading = findViewById(R.id.loading)

        sessionStart.setOnClickListener {
            viewModel.onSessionStartClicked()
        }

        llm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onLlmSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        llmAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        llmAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        llm.adapter = llmAdapter

        tts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onTtsSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        ttsAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        ttsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tts.adapter = ttsAdapter

        modelStyle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onModelStyleSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        modelStyleAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        modelStyleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelStyle.adapter = modelStyleAdapter

        background.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onBackgroundSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        backgroundAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        backgroundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        background.adapter = backgroundAdapter

        prompt.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onPromptSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        promptAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        promptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prompt.adapter = promptAdapter

        useIntro.setOnCheckedChangeListener { _, checked ->
            viewModel.onUseIntroCheckChanged(checked)
        }

        document.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                viewModel.onDocumentSelected(p2)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        documentAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item)
        documentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        document.adapter = documentAdapter

        val onSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                when (p0) {
                    chatbotLeft -> {
                        viewModel.onChatbotLeftChanged(p1)
                    }

                    chatbotTop -> {
                        viewModel.onChatbotTopChanged(p1)
                    }

                    chatbotHeight -> {
                        viewModel.onChatbotHeightChanged(p1)
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        }
        chatbotLeft.setOnSeekBarChangeListener(onSeekBarChangeListener)
        chatbotTop.setOnSeekBarChangeListener(onSeekBarChangeListener)
        chatbotHeight.setOnSeekBarChangeListener(onSeekBarChangeListener)
    }

    private fun initObserver() {
        viewModel.eventLiveData.observe(this) {
            it.getValue()?.let { event ->
                when (event) {
                    is PersoConfigViewModel.Event.ViewInit -> {
                        initViewState(event.initialViewInit)
                    }
                    PersoConfigViewModel.Event.SessionCreated -> {
                        startActivity(Intent(this, PersoLiveActivity::class.java))
                    }
                    PersoConfigViewModel.Event.ShowLoading -> {
                        loading.isVisible = true
                    }
                    PersoConfigViewModel.Event.HideLoading -> {
                        loading.isVisible = false
                    }
                    is PersoConfigViewModel.Event.HandledError -> {
                        showOkDialog(event.message)
                    }
                    is PersoConfigViewModel.Event.UnhandledError -> {
                        showOkDialog(event.message)
                    }
                }
            }
        }
        viewModel.readyToStartLiveData.observe(this) {
            sessionStart.isEnabled = it
        }
        viewModel.introMessageLiveData.observe(this) {
            introMessage.text = it
        }
    }

    private fun initViewState(initialViewState: PersoConfigViewModel.InitialViewState) {
        llmAdapter.clear()
        llmAdapter.addAll(initialViewState.llms)

        ttsAdapter.clear()
        ttsAdapter.addAll(initialViewState.ttss)

        modelStyleAdapter.clear()
        modelStyleAdapter.addAll(initialViewState.modelStyles)

        promptAdapter.clear()
        promptAdapter.addAll(initialViewState.prompts)

        backgroundAdapter.clear()
        backgroundAdapter.addAll(initialViewState.backgroundImages)

        documentAdapter.clear()
        documentAdapter.addAll(initialViewState.documents)

        initialViewState.chatbotLeft.run {
            chatbotLeft.min = min
            chatbotLeft.max = max
            chatbotLeft.progress = progress
        }

        initialViewState.chatbotTop.run {
            chatbotTop.min = min
            chatbotTop.max = max
            chatbotTop.progress = progress
        }

        initialViewState.chatbotHeight.run {
            chatbotHeight.min = min
            chatbotHeight.max = max
            chatbotHeight.progress = progress
        }
    }

    private fun showOkDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error!")
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }

}