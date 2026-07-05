package me.echeung.moemoekyun.ui.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import me.echeung.moemoekyun.ui.theme.AppTheme

/**
 * Placeholder shown while the authenticated user's data is still loading.
 */
@Composable
fun LoadingHomeContent(modifier: Modifier = Modifier, contentPadding: PaddingValues = PaddingValues(0.dp)) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@PreviewLightDark
@Composable
private fun LoadingHomeContentPreview() {
    AppTheme {
        LoadingHomeContent()
    }
}
