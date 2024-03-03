package me.echeung.moemoekyun.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

const val SECONDARY_ITEM_ALPHA = .78f

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SECONDARY_ITEM_ALPHA)
