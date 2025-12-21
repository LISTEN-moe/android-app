package me.echeung.moemoekyun.client.api.socket

import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import logcat.LogPriority
import logcat.asLog
import logcat.logcat
import me.echeung.moemoekyun.util.PreferenceUtil
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Socket @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val preferenceUtil: PreferenceUtil,
    private val json: Json,
) : WebSocketListener() {

    private val _flow = MutableStateFlow<Result>(Result.Loading)
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

            val request = Request.Builder().url(preferenceUtil.station().get().socketUrl).build()
            socket = okHttpClient.newWebSocket(request, this)
        }
    }

    fun disconnect() {
        logcat { "Disconnecting from socket" }
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

        scope.launch {
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

            socket?.send(json.encodeToString(WebsocketRequest.Update()))
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

    private fun initHeartbeat(delayMillis: Int) {
        clearHeartbeat()
        heartbeatJob = scope.launch {
            logcat { "Created heartbeat job for $delayMillis ms" }
            sendHeartbeat(delayMillis.toLong())
        }
    }

    private suspend fun sendHeartbeat(delayMillis: Long) {
        delay(delayMillis)

        if (heartbeatJob?.isActive != true || socket == null) {
            return
        }

        val request = json.encodeToString(WebsocketRequest.Heartbeat())
        logcat { "Sending heartbeat to socket: $request" }
        socket?.send(request)

        // Repeat
        sendHeartbeat(delayMillis)
    }

    private fun clearHeartbeat() {
        heartbeatJob?.let {
            logcat { "Cancelling heartbeat job" }
            it.cancel()
        }
    }

    private fun parseResponse(jsonString: String?) {
        if (jsonString == null) {
            scope.launch {
                _flow.value = Result.Error
            }
            return
        }

        try {
            when (val response = json.decodeFromString<WebsocketResponse>(jsonString)) {
                is WebsocketResponse.Connect -> {
                    initHeartbeat(response.d.heartbeat)
                }
                is WebsocketResponse.Update -> {
                    if (!response.isValidUpdate()) {
                        return
                    }

                    scope.launch {
                        _flow.value = Result.Response(response.d)
                    }
                }
                is WebsocketResponse.HeartbeatAck -> {}
            }
        } catch (e: IOException) {
            logcat(LogPriority.ERROR) { "Failed to parse socket data: $jsonString ${e.asLog()}" }
        }
    }

    sealed interface Result {
        data object Loading : Result
        data class Response(val info: WebsocketResponse.Update.Details?) : Result
        data object Error : Result
    }
}

private const val RETRY_TIME_MIN = 250
private const val RETRY_TIME_MAX = 4000
