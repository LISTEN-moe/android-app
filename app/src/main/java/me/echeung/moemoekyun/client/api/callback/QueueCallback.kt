package me.echeung.moemoekyun.client.api.callback

interface QueueCallback : BaseCallback {
    fun onQueueSuccess(queue: Int)
    fun onUserQueueSuccess(amount: Int, before: Int)
}
