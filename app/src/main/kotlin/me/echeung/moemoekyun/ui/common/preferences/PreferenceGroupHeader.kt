package me.echeung.moemoekyun.ui.common.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceGroupHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 14.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = PrefsHorizontalPadding),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
