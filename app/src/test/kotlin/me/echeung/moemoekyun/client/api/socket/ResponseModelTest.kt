package me.echeung.moemoekyun.client.api.socket

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import me.echeung.moemoekyun.di.SerializationModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ResponseModelTest {

    private val json = SerializationModule.json()

    @Test
    fun `deserializes init websocket message`() {
        val response = json.decodeFromString<WebsocketResponse>(
            """{"op":0,"d":{"message":"Welcome to LISTEN.moe! Enjoy your stay!","heartbeat":35000}}""",
        )

        response.shouldBeInstanceOf<WebsocketResponse.Connect>()
        response.d.heartbeat shouldBe 35000
    }

    @Test
    fun `serializes heartbeat message`() {
        val request = json.encodeToString(WebsocketRequest.Heartbeat())

        request shouldBe """{"op":9}"""
    }
}
