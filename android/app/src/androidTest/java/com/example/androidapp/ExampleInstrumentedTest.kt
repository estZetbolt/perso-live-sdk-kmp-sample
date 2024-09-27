package com.example.androidapp

import ai.perso.live.PersoLiveInitializer
import ai.perso.live.api.IceServer
import ai.perso.live.webrtc.DataChannelInit
import ai.perso.live.webrtc.PeerConnection
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun rtcPeerConnectionTest() = runTest {
        val applicationContext = InstrumentationRegistry.getInstrumentation().targetContext

        PersoLiveInitializer.init(applicationContext)

        val iceServers = listOf(
            IceServer(
                urls = "turn:turn.api.live.perso.ai:17126?transport=udp",
                username = "aa4daa68dabde8195a793dbb14116b2661d3e883d328cb955840aafcb528c089",
                credential = "16a41d9ce114db967b4e44f50c00a889c51d835a51bedd2aacbc8dc6b4d12182"
            ),
            IceServer(
                urls = "turn:turn.api.live.perso.ai:17126?transport=tcp",
                username = "aa4daa68dabde8195a793dbb14116b2661d3e883d328cb955840aafcb528c089",
                credential = "16a41d9ce114db967b4e44f50c00a889c51d835a51bedd2aacbc8dc6b4d12182"
            ),
            IceServer(
                urls = "turns:turn.api.live.perso.ai:443",
                username = "aa4daa68dabde8195a793dbb14116b2661d3e883d328cb955840aafcb528c089",
                credential = "16a41d9ce114db967b4e44f50c00a889c51d835a51bedd2aacbc8dc6b4d12182"
            )
        )
        val peerConnection = PeerConnection(iceServers)
        val dataChannel = peerConnection.createDataChannel(
            "message", DataChannelInit(protocol = "message")
        )
        dataChannel.onMessage {
            // do nothing
        }
        dataChannel.onOpen {
            println("data channel is opened")
        }
        dataChannel.onClose {
            println("data channel is closed")
        }

        val description = peerConnection.createOffer()
        peerConnection.setLocalDescription(description)

        val result = try {
            dataChannel.send("test message")
            true
        } catch (e: Throwable) {
            false
        }
        // 정상적으로 연결된 상태가 아니기에 에러가 나는게 정상
        assertEquals(result, false)

        dataChannel.close()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.androidapp", appContext.packageName)
    }
}