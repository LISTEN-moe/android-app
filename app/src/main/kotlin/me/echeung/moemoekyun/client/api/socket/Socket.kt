package me.echeung.moemoekyun.client.api.socket

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.api.socket.response.BaseResponse
import me.echeung.moemoekyun.client.api.socket.response.ConnectResponse
import me.echeung.moemoekyun.client.api.socket.response.EventNotificationResponse
import me.echeung.moemoekyun.client.api.socket.response.NotificationResponse
import me.echeung.moemoekyun.client.api.socket.response.UpdateResponse
import me.echeung.moemoekyun.client.network.NetworkClient
import me.echeung.moemoekyun.service.notification.EventNotification
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchNow
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException

class Socket(
    private val context: Context,
    private val networkClient: NetworkClient,
    private val json: Json,
) : WebSocketListener() {

    val state = MutableSharedFlow<State>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val scope = MainScope()

    private var retryTime = RETRY_TIME_MIN
    private var attemptingReconnect = false

    @Volatile
    private var socket: WebSocket? = null
    private val socketLock = Any()

    private var heartbeatJob: Job? = null

    fun connect() {
        synchronized(socketLock) {
            logcat { "Connecting to socket..." }

            if (socket != null) {
                disconnect()
            }

            val request = Request.Builder().url(RadioClient.library.socketUrl).build()
            socket = networkClient.client.newWebSocket(request, this)
        }
    }

    fun disconnect() {
        synchronized(socketLock) {
            clearHeartbeat()

            socket?.cancel()
            socket = null

            logcat { "Disconnected from socket" }
        }
    }

    fun reconnect() {
        if (attemptingReconnect) return

        logcat { "Reconnecting to socket in $retryTime ms" }

        disconnect()

        attemptingReconnect = true

        launchNow {
            delay(retryTime.toLong())

            // Exponential backoff
            if (retryTime < RETRY_TIME_MAX) {
                retryTime *= 2
            }

            connect()
        }
    }

    fun update() {
        synchronized(socketLock) {
            logcat { "Requesting update from socket" }

            if (socket == null) {
                connect()
                return
            }

            socket!!.send("{ \"op\": 2 }")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logcat { "Socket connection opened" }

        retryTime = RETRY_TIME_MIN
        attemptingReconnect = false
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logcat { "Received message from socket: $text" }

        parseResponse(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        logcat { "Socket connection closed: $reason" }

        reconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logcat(LogPriority.ERROR) { "Socket failure: ${t.asLog()}" }

        reconnect()
    }

    private fun initHeartbeat(delayMillis: Long) {
        clearHeartbeat()
        heartbeatJob = scope.launch {
            logcat { "Created heartbeat job for $delayMillis ms" }
            sendHeartbeat(delayMillis)
        }
    }

    private suspend fun sendHeartbeat(delayMillis: Long) {
        delay(delayMillis)

        if (heartbeatJob?.isActive != true || socket == null) {
            return
        }

        logcat { "Sending heartbeat to socket" }
        socket!!.send("{ \"op\": 9 }")

        // Repeat
        sendHeartbeat(delayMillis)
    }

    private fun clearHeartbeat() {
        logcat { "Cancelling heartbeat job" }
        heartbeatJob?.cancel()
    }

    private fun parseResponse(jsonString: String?) {
        if (jsonString == null) {
            launchIO {
                state.emit(State.Error)
            }
            return
        }

        try {
            val baseResponse = json.decodeFromString<BaseResponse>(jsonString)
            when (baseResponse.op) {
                // Heartbeat init
                0 -> {
                    val connectResponse = json.decodeFromString<ConnectResponse>(jsonString)
                    initHeartbeat(connectResponse.d!!.heartbeat.toLong())
                }

                // Update
                1 -> {
                    val updateResponse = json.decodeFromString<UpdateResponse>(jsonString)
                    if (!updateResponse.isValidUpdate()) {
                        return
                    }
                    if (updateResponse.isNotification()) {
                        parseNotification(jsonString)
                        return
                    }

                    launchIO {
                        state.emit(State.Update(updateResponse.d))
                    }
                }

                // Heartbeat ACK
                10 -> {}

                else -> logcat { "Received invalid socket data: $jsonString" }
            }
        } catch (e: IOException) {
            logcat(LogPriority.ERROR) { "Failed to parse socket data: $jsonString ${e.asLog()}" }
        }
    }

    private fun parseNotification(jsonString: String) {
        try {
            val notificationResponse = json.decodeFromString<NotificationResponse>(jsonString)
            when (notificationResponse.t) {
                EventNotificationResponse.TYPE -> {
                    val eventResponse = json.decodeFromString<EventNotificationResponse>(jsonString)
                    EventNotification.notify(context, eventResponse.d!!.event.name)
                }
            }
        } catch (e: IOException) {
            logcat(LogPriority.ERROR) { "Failed to parse notification data: $jsonString ${e.asLog()}" }
        }
    }

    sealed class State {
        class Update(val info: UpdateResponse.Details?) : State()
        object Error : State()
    }
}

private const val RETRY_TIME_MIN = 250
private const val RETRY_TIME_MAX = 4000
