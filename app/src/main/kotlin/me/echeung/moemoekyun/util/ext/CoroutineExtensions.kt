package me.echeung.moemoekyun.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val scope = CoroutineScope(SupervisorJob())

fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    scope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, block)

fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
    scope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, block)

fun CoroutineScope.launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    launch(Dispatchers.IO, block = block)

suspend fun <T> withUIContext(block: suspend CoroutineScope.() -> T) = withContext(Dispatchers.Main, block)
