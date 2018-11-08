package me.echeung.moemoekyun.client.api_v2

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.TestSongsQuery
import okhttp3.OkHttpClient

class GraphQLAPIClient {

    private fun setupApollo(): ApolloClient {
        val okHttp = OkHttpClient
                .Builder()
                .build()
        return ApolloClient.builder()
                .serverUrl("https://listen.moe/graphql")
                .okHttpClient(okHttp)
                .build()
    }

    fun test() {
        val client = setupApollo()
        client.query(TestSongsQuery
                .builder()
                .offset(0)
                .count(1)
                .build())
                .enqueue(object : ApolloCall.Callback<TestSongsQuery.Data>() {
                    override fun onFailure(e: ApolloException) {
                        Log.d("GraphQL test failure", e.message.toString())
                    }

                    override fun onResponse(response: Response<TestSongsQuery.Data>) {
                        Log.d("GraphQL test response", response.data()?.songs().toString())
                    }
                })
    }
}
