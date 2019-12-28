package me.echeung.moemoekyun.client.cache

import android.content.Context
import com.apollographql.apollo.cache.http.ApolloHttpCache
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore
import java.io.File

class ApolloCache(
        context: Context
) {

    val cache: ApolloHttpCache

    init {
        val cacheFile = File(context.filesDir, "apolloCache")
        val cacheSize = 1024 * 1024.toLong()
        val cacheStore = DiskLruHttpCacheStore(cacheFile, cacheSize)

        cache = ApolloHttpCache(cacheStore)
    }

}
