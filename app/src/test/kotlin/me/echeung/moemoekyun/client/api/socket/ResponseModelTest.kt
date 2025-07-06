package me.echeung.moemoekyun.client.api.socket

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import me.echeung.moemoekyun.client.model.Event
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.client.model.SongDescriptor
import me.echeung.moemoekyun.di.SerializationModule
import org.junit.jupiter.api.Test

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

    @Test
    fun `deserializes track update message`() {
        val response = json.decodeFromString<WebsocketResponse>(
            """
            {
              "op": 1,
              "d": {
                "song": {
                  "id": 1611,
                  "title": "Clattanoia",
                  "sources": [],
                  "artists": [
                    {
                      "id": 705,
                      "name": "OxT",
                      "nameRomaji": null,
                      "image": "OxT_image.jpg",
                      "characters": []
                    }
                  ],
                  "characters": [],
                  "albums": [],
                  "duration": 238
                },
                "requester": null,
                "event": {
                  "id": 73,
                  "name": "Throwback to some good stuff",
                  "slug": "throwback",
                  "presence": null,
                  "image": "https://i.imgur.com/G3nzMLQ.png"
                },
                "startTime": "2025-07-06T14:03:17.278Z",
                "lastPlayed": [
                  {
                    "id": 3090,
                    "title": "READY!!",
                    "sources": [
                      {
                        "id": 351,
                        "name": null,
                        "nameRomaji": "THE IDOLM@STER OP",
                        "image": null
                      }
                    ],
                    "artists": [
                      {
                        "id": 1235,
                        "name": "765PRO ALLSTARS",
                        "nameRomaji": null,
                        "image": null,
                        "characters": []
                      }
                    ],
                    "characters": [],
                    "albums": [],
                    "duration": 0
                  },
                  {
                    "id": 3781,
                    "title": "Hear The Universe",
                    "sources": [],
                    "artists": [
                      {
                        "id": 889,
                        "name": "ワルキューレ",
                        "nameRomaji": "Walkure",
                        "image": null,
                        "characters": []
                      }
                    ],
                    "characters": [],
                    "albums": [],
                    "duration": 0
                  }
                ],
                "listeners": 93
              },
              "t": "TRACK_UPDATE"
            }
            """.trimIndent(),
        )

        response.shouldBeInstanceOf<WebsocketResponse.Update>()
        response.d?.song shouldBe Song(
            id = 1611,
            title = "Clattanoia",
            titleRomaji = null,
            artists = listOf(
                SongDescriptor(
                    name = "OxT",
                    nameRomaji = null,
                    image = "OxT_image.jpg",
                ),
            ),
            sources = emptyList(),
            albums = emptyList(),
            duration = 238,
            enabled = false,
            favorite = false,
            favoritedAt = null,
        )
        response.d?.event shouldBe Event(
            name = "Throwback to some good stuff",
            image = "https://i.imgur.com/G3nzMLQ.png",
        )
    }
}
