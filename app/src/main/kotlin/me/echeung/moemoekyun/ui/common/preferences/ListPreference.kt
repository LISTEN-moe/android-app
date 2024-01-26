package me.echeung.moemoekyun.ui.common.preferences

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import me.echeung.moemoekyun.R

@Composable
fun <T> ListPreference(
    value: T,
    title: String,
    subtitle: String?,
    entries: ImmutableMap<out T, String>,
    onValueChange: (T) -> Unit,
) {
    var isDialogShown by remember { mutableStateOf(false) }

    TextPreference(
        title = title,
        subtitle = subtitle,
        onPreferenceClick = { isDialogShown = true },
    )

    if (isDialogShown) {
        AlertDialog(
            onDismissRequest = { isDialogShown = false },
            title = { Text(text = title) },
            text = {
                Box {
                    val state = rememberLazyListState()
                    LazyColumn(state = state) {
                        entries.forEach { current ->
                            val isSelected = value == current.key
                            item {
                                DialogRow(
                                    label = current.value,
                                    isSelected = isSelected,
                                    onSelected = {
                                        onValueChange(current.key!!)
                                        isDialogShown = false
                                    },
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isDialogShown = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun DialogRow(
    label: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .selectable(
                selected = isSelected,
                onClick = { if (!isSelected) onSelected() },
            )
            .fillMaxWidth()
            .heightIn(min = 48.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.merge(),
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}
