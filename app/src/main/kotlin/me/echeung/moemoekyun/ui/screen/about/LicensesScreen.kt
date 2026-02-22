package me.echeung.moemoekyun.ui.screen.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar

@Composable
fun LicensesScreen(onBack: () -> Unit) {
    val libs by produceLibraries()

    Scaffold(
        topBar = { Toolbar(titleResId = R.string.licenses, onBack = onBack) },
    ) { contentPadding ->
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = contentPadding,
            libraries = libs,
        )
    }
}
