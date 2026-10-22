package com.messmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.data.Meal
import com.messmate.app.data.IndiaTime
import com.messmate.app.ui.theme.MessMatePalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ScreenHeading(eyebrow: String, title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Eyebrow(eyebrow)
        Text(title, style = MaterialTheme.typography.headlineLarge, color = MessMatePalette.Forest)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MessMatePalette.Muted)
        }
    }
}

@Composable
fun SectionHeading(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = MessMatePalette.Ink)
        if (actionLabel != null && onAction != null) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onAction)
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(actionLabel, color = MessMatePalette.Forest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(2.dp))
                Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = MessMatePalette.Forest, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
fun WeekDayPicker(selectedOffset: Int, onDaySelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        (0..6).forEach { offset ->
            val date = IndiaTime.today().plusDays(offset.toLong())
            val selected = selectedOffset == offset
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (selected) MessMatePalette.Forest else MessMatePalette.Paper)
                    .clickable { onDaySelected(offset) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    date.format(DateTimeFormatter.ofPattern("EE", Locale.ENGLISH)).uppercase(),
                    color = if (selected) MessMatePalette.White.copy(alpha = 0.72f) else MessMatePalette.Muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    date.dayOfMonth.toString(),
                    color = if (selected) MessMatePalette.White else MessMatePalette.Ink,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (offset == 0) {
                    Box(Modifier.size(4.dp).clip(CircleShape).background(if (selected) MessMatePalette.Saffron else MessMatePalette.SageDark))
                } else {
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun NoticeCard(message: String, publishedAt: String? = null, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MessMatePalette.SaffronSoft)
            .clickable(onClick = onClick)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.Campaign, contentDescription = null, tint = MessMatePalette.Saffron, modifier = Modifier.size(19.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("A note from your dining team", color = MessMatePalette.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(message, color = MessMatePalette.Ink, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            publishedAt?.let { Text("Published $it", color = MessMatePalette.SageDark, fontSize = 10.sp) }
        }
        Icon(Icons.Outlined.ArrowForward, contentDescription = "Read dining notice", tint = MessMatePalette.Forest, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun MealItemLines(items: List<String>, textColor: Color, markerColor: Color) {
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(markerColor),
                )
                Text(item, color = textColor, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
fun MealSummaryCard(
    meal: Meal,
    label: String,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onOpen),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(containerColor = MessMatePalette.ForestDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(MessMatePalette.SageDark.copy(alpha = 0.40f), Color.Transparent),
                        ),
                    ),
            )
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = MessMatePalette.White.copy(alpha = 0.12f)) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(MessMatePalette.Saffron))
                            Text(label.uppercase(), color = MessMatePalette.White.copy(alpha = 0.88f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = MessMatePalette.White.copy(alpha = 0.74f), modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(meal.time, color = MessMatePalette.White.copy(alpha = 0.82f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(meal.name, color = MessMatePalette.White, style = MaterialTheme.typography.headlineMedium)
                        MealItemLines(
                            items = meal.items.drop(1),
                            textColor = MessMatePalette.White.copy(alpha = 0.76f),
                            markerColor = MessMatePalette.Saffron,
                        )
                    }
                    Box(Modifier.size(60.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.10f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MessMatePalette.Saffron, modifier = Modifier.size(29.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.ArrowForward, contentDescription = null, tint = MessMatePalette.Saffron, modifier = Modifier.size(15.dp))
                    Text("View meal details", color = MessMatePalette.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun MealListCard(
    meal: Meal,
    subtitle: String,
    favorite: Boolean,
    onFavorite: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MessMatePalette.Paper)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(43.dp).clip(RoundedCornerShape(15.dp)).background(MessMatePalette.Sage), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = MessMatePalette.Forest, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(meal.name, color = MessMatePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MessMatePalette.Muted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            MealItemLines(
                items = meal.items.drop(1),
                textColor = MessMatePalette.Muted,
                markerColor = MessMatePalette.SageDark,
            )
        }
        if (onEdit != null) {
            IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Outlined.Edit, contentDescription = "Edit ${meal.name}", tint = MessMatePalette.Forest, modifier = Modifier.size(17.dp))
            }
        } else {
            IconButton(onClick = onFavorite, modifier = Modifier.size(34.dp)) {
                Icon(
                    if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (favorite) "Remove ${meal.name} from saved meals" else "Save ${meal.name}",
                    tint = if (favorite) MessMatePalette.Saffron else MessMatePalette.Muted,
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
fun InfoPill(text: String, modifier: Modifier = Modifier, filled: Boolean = false) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = if (filled) MessMatePalette.Forest else MessMatePalette.Sage,
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            if (filled) Icon(Icons.Filled.Check, contentDescription = null, tint = MessMatePalette.White, modifier = Modifier.size(12.dp))
            Text(text, color = if (filled) MessMatePalette.White else MessMatePalette.SageDark, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

fun prettyDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH))
