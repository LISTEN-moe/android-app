package me.echeung.moemoekyun.ui.screen.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.ui.common.Toolbar

object LicensesScreen : Screen {

    @Composable
    override fun Content() {
        Scaffold(
            topBar = { Toolbar(titleResId = R.string.licenses, showUpButton = true) },
        ) { contentPadding ->
            LibrariesContainer(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = contentPadding,
            )
        }
    }
}
