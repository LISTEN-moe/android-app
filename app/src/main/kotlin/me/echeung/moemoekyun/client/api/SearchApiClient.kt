package me.echeung.moemoekyun.client.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchApiClient @Inject constructor(private val okHttpClient: OkHttpClient, private val json: Json) {

    suspend fun search(query: String, cursor: String? = null): SearchResponse {
        val body = SearchRequest(
            searchText = query,
            limit = PAGE_SIZE,
            resultTypes = listOf("songs"),
            cursor = cursor,
            cursorDirection = if (cursor != null) "next" else null,
        )

        val requestBody = json.encodeToString(SearchRequest.serializer(), body)
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://listen.moe/api/v1/search")
            .post(requestBody)
            .build()

        val responseBody = okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Search failed: ${response.code}")
            response.body?.string() ?: throw Exception("Empty search response")
        }

        return json.decodeFromString(SearchResponse.serializer(), responseBody)
    }

    companion object {
        const val PAGE_SIZE = 50
    }
}

@Serializable
data class SearchRequest(
    @SerialName("search_text") val searchText: String,
    val limit: Int,
    @SerialName("result_types") val resultTypes: List<String>,
    val cursor: String? = null,
    @SerialName("cursor_direction") val cursorDirection: String? = null,
)

@Serializable
data class SearchResponse(
    val results: List<SearchResult> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class SearchResult(
    val type: String,
    val id: Int,
    val title: String,
    @SerialName("title_romaji") val titleRomaji: String? = null,
    val artists: List<SearchDescriptor> = emptyList(),
    val albums: List<SearchDescriptor> = emptyList(),
    val sources: List<SearchDescriptor> = emptyList(),
    val duration: Int = 0,
)

@Serializable
data class SearchDescriptor(
    val name: String? = null,
    @SerialName("name_romaji") val nameRomaji: String? = null,
    val image: String? = null,
)
