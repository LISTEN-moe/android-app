package me.echeung.moemoekyun.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

const val SecondaryItemAlpha = .78f

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SecondaryItemAlpha)
