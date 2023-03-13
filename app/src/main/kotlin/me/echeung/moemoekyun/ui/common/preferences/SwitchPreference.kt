package me.echeung.moemoekyun.ui.common.preferences

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tfcporciuncula.flow.Preference
import me.echeung.moemoekyun.util.ext.collectAsState

@Composable
fun SwitchPreference(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    preference: Preference<Boolean>,
) {
    val checked by preference.collectAsState()
    TextPreference(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        widget = {
            Switch(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.padding(start = TrailingWidgetBuffer),
            )
        },
        onPreferenceClick = { preference.set(!checked) },
    )
}
