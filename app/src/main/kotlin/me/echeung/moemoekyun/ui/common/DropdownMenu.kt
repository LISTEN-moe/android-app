package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material3.DropdownMenu as ComposeDropdownMenu

@Composable
fun DropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(8.dp, (-56).dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    ComposeDropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.sizeIn(minWidth = 196.dp, maxWidth = 196.dp),
        offset = offset,
        properties = properties,
        content = content,
    )
}

@Composable
fun RadioIcon(
    checked: Boolean,
) {
    if (checked) {
        Icon(
            Icons.Outlined.RadioButtonChecked,
            contentDescription = null,
        )
    } else {
        Icon(
            Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
        )
    }
}

@Composable
fun CheckboxIcon(
    checked: Boolean,
) {
    if (checked) {
        Icon(
            Icons.Outlined.CheckBox,
            contentDescription = null,
        )
    } else {
        Icon(
            Icons.Outlined.CheckBoxOutlineBlank,
            contentDescription = null,
        )
    }
}
