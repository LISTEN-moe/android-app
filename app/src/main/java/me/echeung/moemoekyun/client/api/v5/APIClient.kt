package me.echeung.moemoekyun.client.api.v5

import android.util.Log
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.exception.ApolloException
import me.echeung.moemoekyun.TestSongsQuery
import me.echeung.moemoekyun.client.auth.AuthUtil
import okhttp3.OkHttpClient

class APIClient(okHttpClient: OkHttpClient, private val authUtil: AuthUtil) {

    private val client: ApolloClient = ApolloClient.builder()
                .serverUrl(Library.API_BASE)
                .okHttpClient(okHttpClient)
                .build()

    fun test() {
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
