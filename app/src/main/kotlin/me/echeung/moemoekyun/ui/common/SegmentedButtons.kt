package me.echeung.moemoekyun.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val StartItemShape = RoundedCornerShape(topStartPercent = 100, bottomStartPercent = 100)
val MiddleItemShape = RoundedCornerShape(0)
val EndItemShape = RoundedCornerShape(topEndPercent = 100, bottomEndPercent = 100)

@Composable
fun SegmentedButtons(
    modifier: Modifier = Modifier,
    entries: List<String>,
    selectedIndex: Int,
    onClick: (Int) -> Unit,
) {
    val outlineColor = LocalContentColor.current.copy(alpha = SecondaryItemAlpha)

    Row(
        modifier = modifier,
    ) {
        entries.mapIndexed { index, label ->
            val shape = when (index) {
                0 -> StartItemShape
                entries.lastIndex -> EndItemShape
                else -> MiddleItemShape
            }

            if (index == selectedIndex) {
                Button(
                    modifier = Modifier.weight(1f),
                    shape = shape,
                    onClick = { onClick(index) },
                ) {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    shape = shape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = outlineColor,
                    ),
                    border = BorderStroke(width = 1.dp, color = outlineColor),
                    onClick = { onClick(index) },
                ) {
                    Text(
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedButtonsPreview() {
    Column {
        SegmentedButtons(
            entries = listOf(
                "Day",
                "Week",
                "Month",
                "Year",
            ),
            selectedIndex = 1,
            onClick = {},
        )

        SegmentedButtons(
            entries = listOf(
                "Foo",
                "Bar",
            ),
            selectedIndex = 1,
            onClick = {},
        )
    }
}
