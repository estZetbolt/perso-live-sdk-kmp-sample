package com.example.androidapp

import ai.perso.live.AllSetting
import ai.perso.live.PersoLiveSDK
import ai.perso.live.perso.ApiError
import ai.perso.live.perso.Session
import ai.perso.live.perso.Timeout
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper

object SessionManager {

    init {
        setCommunicationDevice(true)
    }

    private var session: Session? = null
    private var unsubscribeSessionStatus: (() -> Unit)? = null

    @Throws(ApiError::class)
    suspend fun getAllSettings(apiServer: String, apiKey: String): AllSetting =
        PersoLiveSDK.getAllSettings(apiServer, apiKey)

    @Throws(ApiError::class, Timeout::class)
    suspend fun createSession(
        apiServer: String,
        apiKey: String,
        llmKey: String,
        ttsKey: String,
        modelStyleKey: String,
        promptKey: String,
        documentKey: String?,
        backgroundImageKey: String?,
        paddingLeft: Float?,
        paddingTop: Float?,
        paddingHeight: Float?,
        width: Int,
        height: Int,
        useIntro: Boolean
    ): Session {
        val session = try {
            val sessionId = PersoLiveSDK.createSessionId(
                apiServer = apiServer,
                apiKey = apiKey,
                llmType = llmKey,
                ttsType = ttsKey,
                modelStyle = modelStyleKey,
                prompt = promptKey,
                document = documentKey,
                backgroundImage = backgroundImageKey,
                paddingLeft = paddingLeft,
                paddingTop = paddingTop,
                paddingHeight = paddingHeight
            )
            val icesServers = PersoLiveSDK.getIceServers(apiServer, sessionId)
            PersoLiveSDK.createSession(apiServer, icesServers, sessionId, width, height, useIntro)
        } catch (e: Throwable) {
            throw e
        }

        unsubscribeSessionStatus = session.subscribeSessionStatus { status ->
            if (!status.live && status.code == 408) {
                Handler(Looper.getMainLooper()).post {
                    releaseSession()
                }
            }
        }

        this.session = session

        return session
    }

    fun hasSession(): Boolean = session != null

    fun getSession(): Session = session!!

    fun stopSession() {
        session?.stopSession()
        releaseSession()
    }

    private fun releaseSession() {
        session = null
        unsubscribeSessionStatus?.invoke()
        unsubscribeSessionStatus = null
    }

    fun isSpeakerphoneOn(): Boolean {
        val audioManager = App.getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        } else {
            audioManager.isSpeakerphoneOn
        }
    }

    fun setSpeakerMode() {
        if (isSpeakerphoneOn()) return

        setCommunicationDevice(true)
    }

    fun setEarpieceMode() {
        if (!isSpeakerphoneOn()) return

        setCommunicationDevice(false)
    }

    private fun setCommunicationDevice(useSpeaker: Boolean) {
        val audioManager = App.getContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.availableCommunicationDevices.firstOrNull {
                it.type == if (useSpeaker) {
                    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                } else {
                    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                }
            }?.let {
                audioManager.setCommunicationDevice(it)
            }
        } else {
            audioManager.isSpeakerphoneOn = useSpeaker
        }
    }

}