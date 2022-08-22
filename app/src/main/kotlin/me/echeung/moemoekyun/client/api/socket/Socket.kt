package me.echeung.moemoekyun.client.api.socket

import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.client.RadioClient
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
) : WebSocketListener() {

    private val _flow = MutableStateFlow<SocketResult>(SocketLoading)
    val flow = _flow.asStateFlow()

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

            socket?.send(json.encodeToString(ResponseModel.Base(2)))
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
        socket?.send(json.encodeToString(ResponseModel.Base(9)))

        // Repeat
        sendHeartbeat(delayMillis)
    }

    private fun clearHeartbeat() {
        logcat { "Cancelling heartbeat job" }
        heartbeatJob?.cancel()
    }

    private fun parseResponse(jsonString: String?) {
        if (jsonString == null) {
            scope.launchIO {
                _flow.value = SocketError
            }
            return
        }

        try {
            val baseResponse = json.decodeFromString<ResponseModel.Base>(jsonString)
            when (baseResponse.op) {
                // Heartbeat init
                0 -> {
                    val connectResponse = json.decodeFromString<ResponseModel.Connect>(jsonString)
                    initHeartbeat(connectResponse.d!!.heartbeat.toLong())
                }

                // Update
                1 -> {
                    val updateResponse = json.decodeFromString<ResponseModel.Update>(jsonString)
                    if (!isValidUpdate(updateResponse)) {
                        return
                    }
                    if (isNotification(updateResponse)) {
                        parseNotification(jsonString)
                        return
                    }

                    scope.launchIO {
                        _flow.value = SocketResponse(updateResponse.d)
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
            val notificationResponse = json.decodeFromString<ResponseModel.Notification>(jsonString)
            when (notificationResponse.t) {
                ResponseModel.EventNotificationResponse.TYPE -> {
                    val eventResponse = json.decodeFromString<ResponseModel.EventNotificationResponse>(jsonString)
                    EventNotification.notify(context, eventResponse.d!!.event!!.name)
                }
            }
        } catch (e: IOException) {
            logcat(LogPriority.ERROR) { "Failed to parse notification data: $jsonString ${e.asLog()}" }
        }
    }

    private fun isValidUpdate(updateResponse: ResponseModel.Update): Boolean {
        return (
            updateResponse.t == TRACK_UPDATE ||
                updateResponse.t == TRACK_UPDATE_REQUEST ||
                updateResponse.t == QUEUE_UPDATE ||
                isNotification(updateResponse)
            )
    }

    private fun isNotification(updateResponse: ResponseModel.Update): Boolean {
        return updateResponse.t == NOTIFICATION
    }

    sealed interface SocketResult
    object SocketLoading : SocketResult
    data class SocketResponse(val info: ResponseModel.Update.Details?) : SocketResult
    object SocketError : SocketResult

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        private const val TRACK_UPDATE = "TRACK_UPDATE"
        private const val TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST"
        private const val QUEUE_UPDATE = "QUEUE_UPDATE"
        private const val NOTIFICATION = "NOTIFICATION"

        private const val RETRY_TIME_MIN = 250
        private const val RETRY_TIME_MAX = 4000
    }
}
