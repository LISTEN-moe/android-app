package me.echeung.moemoekyun.util.ext

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.tfcporciuncula.flow.Preference

@Composable
fun <T> Preference<T>.collectAsState(): State<T> {
    val flow = remember(this) { asFlow() }
    return flow.collectAsState(initial = get())
}
