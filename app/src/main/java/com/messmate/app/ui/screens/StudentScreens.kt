package com.messmate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.data.DiningNotice
import com.messmate.app.data.IndiaTime
import com.messmate.app.data.MealSlot
import com.messmate.app.data.MessMateStore
import com.messmate.app.data.ThemePreference
import com.messmate.app.data.UserIdentity
import com.messmate.app.ui.components.Eyebrow
import com.messmate.app.ui.components.AdaptiveAppShell
import com.messmate.app.ui.components.AdaptiveDestination
import com.messmate.app.ui.components.ResponsiveContentArea
import com.messmate.app.ui.components.InfoPill
import com.messmate.app.ui.components.MealListCard
import com.messmate.app.ui.components.MealSummaryCard
import com.messmate.app.ui.components.MessMateLogo
import com.messmate.app.ui.components.MessMateWordmark
import com.messmate.app.ui.components.NoticeCard
import com.messmate.app.ui.components.rememberIndiaNow
import com.messmate.app.ui.components.ScreenHeading
import com.messmate.app.ui.components.SectionHeading
import com.messmate.app.ui.components.WeekDayPicker
import com.messmate.app.ui.components.prettyDate
import com.messmate.app.ui.components.ThemePreferencePicker
import com.messmate.app.ui.components.responsiveRows
import com.messmate.app.ui.theme.MessMatePalette

enum class StudentTab(val title: String) {
    TODAY("Today"), WEEK("Week"), MENU("Menu"), PROFILE("Profile"),
}

@Composable
fun StudentShell(
    store: MessMateStore,
    identity: UserIdentity,
    themePreference: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    tab: StudentTab,
    onTabChanged: (StudentTab) -> Unit,
    dayOffset: Int,
    onDayChanged: (Int) -> Unit,
    currentNotice: DiningNotice?,
    onNotice: () -> Unit,
    onAbout: () -> Unit,
    onEnableNotifications: () -> Unit,
    onSwitchToAdmin: () -> Unit,
    onSignOut: () -> Unit,
    onDataChanged: () -> Unit,
) {
    AdaptiveAppShell(
        selectedIndex = StudentTab.entries.indexOf(tab),
        destinations = StudentTab.entries.map { item ->
            val icon = when (item) {
                StudentTab.TODAY -> Icons.Filled.Home
                StudentTab.WEEK -> Icons.Filled.CalendarMonth
                StudentTab.MENU -> Icons.AutoMirrored.Filled.MenuBook
                StudentTab.PROFILE -> Icons.Filled.Person
            }
            AdaptiveDestination(item.title, icon)
        },
        onDestinationSelected = { index -> onTabChanged(StudentTab.entries[index]) },
    ) { padding ->
        when (tab) {
            StudentTab.TODAY -> StudentHome(
                store, identity, currentNotice, padding, onNotice,
                onOpenMenu = { onDayChanged(0); onTabChanged(StudentTab.MENU) },
                onFavorite = { store.toggleFavorite(it); onDataChanged() },
            )
            StudentTab.WEEK -> StudentWeek(store, dayOffset, onDayChanged, padding, onOpenMenu = { onTabChanged(StudentTab.MENU) })
            StudentTab.MENU -> StudentMenu(
                store, dayOffset, onDayChanged, padding,
                onFavorite = { store.toggleFavorite(it); onDataChanged() },
            )
            StudentTab.PROFILE -> StudentProfile(
                identity, themePreference, onThemeChanged, currentNotice, padding, onAbout, onNotice,
                onEnableNotifications, onSwitchToAdmin, onSignOut,
            )
        }
    }
}

@Composable
private fun StudentHome(
    store: MessMateStore,
    identity: UserIdentity,
    notice: DiningNotice?,
    padding: PaddingValues,
    onNotice: () -> Unit,
    onOpenMenu: () -> Unit,
    onFavorite: (String) -> Unit,
) {
    val zonedNow = rememberIndiaNow()
    val now = zonedNow.toLocalTime()
    val date = zonedNow.toLocalDate()
    val todayMeals = store.mealsFor(date.dayOfWeek)
    val orderedSlots = MealSlot.entries.sortedBy { store.mealSchedule(it).start }
    val servingSlot = orderedSlots.firstOrNull { slot ->
        val schedule = store.mealSchedule(slot)
        !now.isBefore(schedule.start) && now.isBefore(schedule.end)
    }
    val nextScheduledSlot = orderedSlots.firstOrNull { store.mealSchedule(it).start.isAfter(now) }
    val isTomorrow = servingSlot == null && nextScheduledSlot == null
    val nextSlot = servingSlot ?: nextScheduledSlot ?: orderedSlots.first()
    val nextMealDay = if (isTomorrow) date.plusDays(1).dayOfWeek else date.dayOfWeek
    val nextMeal = store.mealFor(nextMealDay, nextSlot)
    val nextLabel = when {
        servingSlot != null -> "Serving now · ${servingSlot.label}"
        isTomorrow -> "Tomorrow · up next"
        else -> "Up next · ${nextSlot.label}"
    }
    val greeting = when (now.hour) {
        in 5..11 -> "Good morning, ${identity.name}"
        in 12..16 -> "Good afternoon, ${identity.name}"
        else -> "Good evening, ${identity.name}"
    }

    ResponsiveContentArea(Modifier.fillMaxSize().padding(padding)) { columns ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 15.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                MessMateWordmark(markSize = 34.dp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onNotice, modifier = Modifier.size(42.dp).clip(CircleShape).background(MessMatePalette.Paper)) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = "MessMate dining notices", tint = MessMatePalette.Forest, modifier = Modifier.size(21.dp))
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Eyebrow(prettyDate(date))
                Text(greeting, color = MessMatePalette.Forest, style = MaterialTheme.typography.headlineLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = MessMatePalette.SageDark, modifier = Modifier.size(14.dp))
                    Text("Maple House · North Mess", color = MessMatePalette.Muted, fontSize = 12.sp)
                }
            }
        }
        if (notice != null) item { NoticeCard(message = notice.message, publishedAt = notice.publishedAt, onClick = onNotice) }
        item { MealSummaryCard(meal = nextMeal, label = nextLabel, onOpen = onOpenMenu) }
        item {
            SectionHeading("Today at the table", "Full menu", onOpenMenu)
        }
        responsiveRows(MealSlot.entries.size, columns) { index ->
            val slot = MealSlot.entries[index]
            val meal = todayMeals[index]
            MealListCard(
                meal = meal,
                subtitle = "${slot.label} · ${meal.time}",
                favorite = store.favorites().contains(favoriteId(date.dayOfWeek.name, slot.name)),
                onFavorite = { onFavorite(favoriteId(date.dayOfWeek.name, slot.name)) },
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                InfoPill("Freshly prepared each day")
            }
        }
      }
    }
}

@Composable
private fun StudentWeek(
    store: MessMateStore,
    dayOffset: Int,
    onDayChanged: (Int) -> Unit,
    padding: PaddingValues,
    onOpenMenu: () -> Unit,
) {
    val date = IndiaTime.today().plusDays(dayOffset.toLong())
    val meals = store.mealsFor(date.dayOfWeek)
    ResponsiveContentArea(Modifier.fillMaxSize().padding(padding)) { columns ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        item { ScreenHeading("THE WEEK, WELL PLANNED", "A seat at every table.", "Pick a day and see what's coming up.") }
        item { WeekDayPicker(dayOffset, onDayChanged) }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Sage), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(17.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(MessMatePalette.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                        MessMateLogo(size = 28.dp)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(prettyDate(date), color = MessMatePalette.Forest, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Four meals · one good day", color = MessMatePalette.SageDark, fontSize = 11.sp)
                    }
                    InfoPill(if (dayOffset == 0) "Today" else "Menu", filled = dayOffset == 0)
                }
            }
        }
        item { SectionHeading("Serving on this day", "Open menu", onOpenMenu) }
        responsiveRows(MealSlot.entries.size, columns) { index ->
            val slot = MealSlot.entries[index]
            val meal = meals[index]
            MealListCard(meal, "${slot.label} · ${meal.time}", favorite = false, onFavorite = {}, onClick = onOpenMenu)
        }
        item {
            Text("Menus may change slightly based on seasonal availability.", color = MessMatePalette.Muted, fontSize = 11.sp)
        }
      }
    }
}

@Composable
private fun StudentMenu(
    store: MessMateStore,
    dayOffset: Int,
    onDayChanged: (Int) -> Unit,
    padding: PaddingValues,
    onFavorite: (String) -> Unit,
) {
    var selectedSlot by rememberSaveable { mutableStateOf<String?>(null) }
    val date = IndiaTime.today().plusDays(dayOffset.toLong())
    val allMeals = store.mealsFor(date.dayOfWeek)
    val visibleMeals = allMeals.filterIndexed { index, _ ->
        selectedSlot == null || MealSlot.entries[index].name == selectedSlot
    }

    ResponsiveContentArea(Modifier.fillMaxSize().padding(padding)) { columns ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        item { ScreenHeading("FRESH FROM THE KITCHEN", "The menu.", "Every meal, thoughtfully laid out.") }
        item { WeekDayPicker(dayOffset, onDayChanged) }
        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                FilterChip(selected = selectedSlot == null, onClick = { selectedSlot = null }, label = { Text("All meals") })
                MealSlot.entries.forEach { slot ->
                    FilterChip(selected = selectedSlot == slot.name, onClick = { selectedSlot = slot.name }, label = { Text(slot.label) })
                }
            }
        }
        item { SectionHeading("${visibleMeals.size} meals on ${date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}") }
        if (visibleMeals.isEmpty()) {
            item {
                Text("No meals match those filters. Try another meal or day.", color = MessMatePalette.Muted, fontSize = 13.sp)
            }
        } else {
            responsiveRows(visibleMeals.size, columns) { mealIndex ->
                val meal = visibleMeals[mealIndex]
                val slot = MealSlot.entries[allMeals.indexOf(meal)]
                MealListCard(
                    meal = meal,
                    subtitle = "${slot.label} · ${meal.time}",
                    favorite = store.favorites().contains(favoriteId(date.dayOfWeek.name, slot.name)),
                    onFavorite = { onFavorite(favoriteId(date.dayOfWeek.name, slot.name)) },
                )
            }
        }
        item {
            Text("Please let the dining team know about allergies before service.", color = MessMatePalette.Muted, fontSize = 11.sp)
        }
      }
    }
}

@Composable
private fun StudentProfile(
    identity: UserIdentity,
    themePreference: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    notice: DiningNotice?,
    padding: PaddingValues,
    onAbout: () -> Unit,
    onNotice: () -> Unit,
    onEnableNotifications: () -> Unit,
    onSwitchToAdmin: () -> Unit,
    onSignOut: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeading("YOUR MESSMATE", "A little about you.", "Make your dining experience feel like yours.") }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.ForestDark), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Text(identity.name.firstOrNull()?.uppercase() ?: "S", color = MessMatePalette.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(identity.name, color = MessMatePalette.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Student ID · ${identity.id}", color = MessMatePalette.White.copy(alpha = 0.72f), fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            ThemePreferencePicker(selected = themePreference, onSelect = onThemeChanged)
        }
        item {
            SectionHeading("Stay in the loop")
            Spacer(Modifier.height(9.dp))
            SettingRow(
                title = "Dining notifications",
                description = "Menu changes and dining notices",
                actionLabel = "Enable",
                onAction = onEnableNotifications,
            )
            Spacer(Modifier.height(9.dp))
            SettingRow(
                title = "Latest notice",
                description = notice?.let { "${it.message.take(42)} · ${it.publishedAt}" } ?: "No current notice",
                actionLabel = "Read",
                onAction = onNotice,
            )
        }
        item {
            SectionHeading("About")
            Spacer(Modifier.height(9.dp))
            SettingRow(title = "About MessMate", description = "Know what's on the menu.", actionLabel = "View", onAction = onAbout)
        }
        item {
            Button(
                onClick = onSwitchToAdmin,
                modifier = Modifier.fillMaxWidth().height(51.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.Sage),
            ) {
                Text("Preview dining team tools", color = MessMatePalette.Forest, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            androidx.compose.material3.TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = MessMatePalette.Muted, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(MessMatePalette.Paper).padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, color = MessMatePalette.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = MessMatePalette.Muted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        if (trailing != null) trailing()
        if (actionLabel != null && onAction != null) {
            androidx.compose.material3.TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 5.dp, vertical = 0.dp)) {
                Text(actionLabel, color = MessMatePalette.Forest, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private fun favoriteId(dayName: String, slotName: String) = "$dayName:$slotName"
