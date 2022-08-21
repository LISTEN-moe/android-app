package me.echeung.moemoekyun.util.ext

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val scope = CoroutineScope(SupervisorJob())

fun launchUI(block: suspend CoroutineScope.() -> Unit): Job =
    scope.launch(Dispatchers.Main, CoroutineStart.DEFAULT, block)

fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    scope.launch(Dispatchers.IO, CoroutineStart.DEFAULT, block)

fun launchNow(block: suspend CoroutineScope.() -> Unit): Job =
    scope.launch(Dispatchers.Main, CoroutineStart.UNDISPATCHED, block)
