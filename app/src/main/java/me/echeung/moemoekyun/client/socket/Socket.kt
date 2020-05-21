package me.echeung.moemoekyun.client.socket

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.squareup.moshi.Moshi
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.echeung.moemoekyun.client.RadioClient
import me.echeung.moemoekyun.client.socket.response.BaseResponse
import me.echeung.moemoekyun.client.socket.response.ConnectResponse
import me.echeung.moemoekyun.client.socket.response.EventNotificationResponse
import me.echeung.moemoekyun.client.socket.response.NotificationResponse
import me.echeung.moemoekyun.client.socket.response.UpdateResponse
import me.echeung.moemoekyun.service.notification.EventNotification
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class Socket(
    private val context: Context,
    private val client: OkHttpClient
) : WebSocketListener() {

    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private var retryTime = RETRY_TIME_MIN
    private var attemptingReconnect = false

    @Volatile
    private var socket: WebSocket? = null
    private val socketLock = Any()

    private val listeners = mutableListOf<Listener>()

    private var heartbeatJob: Job? = null

    fun addListener(listener: Listener) {
        this.listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        this.listeners.remove(listener)
    }

    fun connect() {
        synchronized(socketLock) {
            Log.d(TAG, "Connecting to socket...")

            if (socket != null) {
                disconnect()
            }

            val request = Request.Builder().url(RadioClient.library!!.socketUrl).build()
            socket = client.newWebSocket(request, this)
        }
    }

    fun disconnect() {
        synchronized(socketLock) {
            clearHeartbeat()

            socket?.cancel()
            socket = null

            Log.d(TAG, "Disconnected from socket")
        }
    }

    fun reconnect() {
        if (attemptingReconnect) return

        Log.d(TAG, "Reconnecting to socket in $retryTime ms")

        disconnect()

        attemptingReconnect = true

        // Exponential backoff
        SystemClock.sleep(retryTime.toLong())
        if (retryTime < RETRY_TIME_MAX) {
            retryTime *= 2
        }

        connect()
    }

    fun update() {
        synchronized(socketLock) {
            Log.d(TAG, "Requesting update from socket")

            if (socket == null) {
                connect()
                return
            }

            socket!!.send("{ \"op\": 2 }")
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d(TAG, "Socket connection opened")

        retryTime = RETRY_TIME_MIN
        attemptingReconnect = false
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Received message from socket: $text")

        parseResponse(text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.d(TAG, "Socket connection closed: $reason")

        reconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "Socket failure: ${t.message}", t)

        reconnect()
    }

    private fun initHeartbeat(delayMillis: Long) {
        clearHeartbeat()
        heartbeatJob = scope.launch {
            Log.d(TAG, "Created heartbeat job for $delayMillis ms")
            sendHeartbeat(delayMillis)
        }
    }

    private suspend fun sendHeartbeat(delayMillis: Long) {
        delay(delayMillis)

        if (heartbeatJob?.isActive != true || socket == null) {
            return
        }

        Log.d(TAG, "Sending heartbeat to socket")
        socket!!.send("{ \"op\": 9 }")

        // Repeat
        sendHeartbeat(delayMillis)
    }

    private fun clearHeartbeat() {
        Log.d(TAG, "Cancelling heartbeat job")
        heartbeatJob?.cancel()
    }

    private fun parseResponse(jsonString: String?) {
        if (listeners.isEmpty()) {
            Log.d(TAG, "No listeners")
            return
        }

        if (jsonString == null) {
            listeners.forEach { it.onSocketFailure() }
            return
        }

        try {
            val baseResponse = getResponse(BaseResponse::class.java, jsonString)
            when (baseResponse!!.op) {
                // Heartbeat init
                0 -> {
                    val connectResponse = getResponse(ConnectResponse::class.java, jsonString)
                    initHeartbeat(connectResponse!!.d!!.heartbeat.toLong())
                }

                // Update
                1 -> {
                    val updateResponse = getResponse(UpdateResponse::class.java, jsonString)
                    if (!isValidUpdate(updateResponse!!)) {
                        return
                    }
                    if (isNotification(updateResponse)) {
                        parseNotification(jsonString)
                        return
                    }

                    listeners.forEach { it.onSocketReceive(updateResponse.d) }
                }

                // Heartbeat ACK
                10 -> {}

                else -> Log.d(TAG, "Received invalid socket data: $jsonString")
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to parse socket data: $jsonString", e)
        }
    }

    private fun parseNotification(jsonString: String) {
        try {
            val notificationResponse = getResponse(NotificationResponse::class.java, jsonString)
            when (notificationResponse!!.t) {
                EventNotificationResponse.TYPE -> {
                    val eventResponse = getResponse(EventNotificationResponse::class.java, jsonString)
                    EventNotification.notify(context, eventResponse!!.d!!.event!!.name)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to parse notification data: $jsonString", e)
        }
    }

    private fun isValidUpdate(updateResponse: UpdateResponse): Boolean {
        return (
            updateResponse.t == TRACK_UPDATE ||
                updateResponse.t == TRACK_UPDATE_REQUEST ||
                updateResponse.t == QUEUE_UPDATE ||
                isNotification(updateResponse)
            )
    }

    private fun isNotification(updateResponse: UpdateResponse): Boolean {
        return updateResponse.t == NOTIFICATION
    }

    @Throws(IOException::class)
    private fun <T : BaseResponse> getResponse(responseClass: Class<T>, jsonString: String): T? {
        return MOSHI.adapter(responseClass).fromJson(jsonString)
    }

    interface Listener {
        fun onSocketReceive(info: UpdateResponse.Details?)

        fun onSocketFailure()
    }

    companion object {
        private val TAG = Socket::class.java.simpleName

        private val MOSHI = Moshi.Builder().build()

        private const val TRACK_UPDATE = "TRACK_UPDATE"
        private const val TRACK_UPDATE_REQUEST = "TRACK_UPDATE_REQUEST"
        private const val QUEUE_UPDATE = "QUEUE_UPDATE"
        private const val NOTIFICATION = "NOTIFICATION"

        private const val RETRY_TIME_MIN = 250
        private const val RETRY_TIME_MAX = 4000
    }
}
