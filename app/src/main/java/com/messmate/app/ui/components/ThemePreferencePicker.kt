package com.messmate.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.data.ThemePreference
import com.messmate.app.ui.theme.MessMatePalette

/** A single, consistent appearance setting shared by student and admin profiles. */
@Composable
fun ThemePreferencePicker(
    selected: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text("Appearance", color = MessMatePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Choose the look that feels right. Saved on this device.", color = MessMatePalette.Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemePreference.entries.forEach { preference ->
                    FilterChip(
                        selected = selected == preference,
                        onClick = { onSelect(preference) },
                        label = { Text(preference.label) },
                    )
                }
            }
        }
    }
}
