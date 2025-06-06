package me.echeung.moemoekyun.client.api.socket

import me.echeung.moemoekyun.di.SerializationModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ResponseModelTest {

    private val json = SerializationModule.json()

    @Test
    fun `deserializes init websocket message`() {
        val response = json.decodeFromString<WebsocketResponse>(
            """{"op":0,"d":{"message":"Welcome to LISTEN.moe! Enjoy your stay!","heartbeat":35000}}""",
        )

        if (response !is WebsocketResponse.Connect) {
            fail("Unexpected type")
        }

        assertEquals(35000, response.d.heartbeat)
    }

    @Test
    fun `serializes heartbeat message`() {
        val request = json.encodeToString(WebsocketRequest.Heartbeat())

        assertEquals("""{"op":9}""", request)
    }
}
