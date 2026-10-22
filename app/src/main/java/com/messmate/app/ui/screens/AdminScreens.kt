package com.messmate.app.ui.screens

import android.app.TimePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.data.AddAdminResult
import com.messmate.app.data.DiningNotice
import com.messmate.app.data.IndiaTime
import com.messmate.app.data.MealSchedule
import com.messmate.app.data.MealSlot
import com.messmate.app.data.MessMateStore
import com.messmate.app.data.ThemePreference
import com.messmate.app.ui.components.Eyebrow
import com.messmate.app.ui.components.AdaptiveAppShell
import com.messmate.app.ui.components.AdaptiveDestination
import com.messmate.app.ui.components.ResponsiveContentArea
import com.messmate.app.ui.components.InfoPill
import com.messmate.app.ui.components.rememberIndiaNow
import com.messmate.app.ui.components.MealListCard
import com.messmate.app.ui.components.MessMateLogo
import com.messmate.app.ui.components.NoticeCard
import com.messmate.app.ui.components.ScreenHeading
import com.messmate.app.ui.components.SectionHeading
import com.messmate.app.ui.components.WeekDayPicker
import com.messmate.app.ui.components.prettyDate
import com.messmate.app.ui.components.responsiveRows
import com.messmate.app.ui.components.ThemePreferencePicker
import com.messmate.app.ui.theme.MessMatePalette
import java.time.LocalTime

enum class AdminTab(val title: String) {
    OVERVIEW("Overview"), MENUS("Menus"), NOTICES("Notices"), PROFILE("Profile"),
}

@Composable
fun AdminShell(
    store: MessMateStore,
    currentAdminId: String,
    themePreference: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    tab: AdminTab,
    onTabChanged: (AdminTab) -> Unit,
    dayOffset: Int,
    onDayChanged: (Int) -> Unit,
    currentNotice: DiningNotice?,
    previousNotices: List<DiningNotice>,
    onOpenNoticeEditor: () -> Unit,
    showNoticeEditor: Boolean,
    onDismissNoticeEditor: () -> Unit,
    onPublishNotice: (String) -> Unit,
    onDeleteCurrentNotice: () -> Unit,
    onAbout: () -> Unit,
    onSwitchToStudent: () -> Unit,
    onSignOut: () -> Unit,
    onDataChanged: () -> Unit,
) {
    AdaptiveAppShell(
        selectedIndex = AdminTab.entries.indexOf(tab),
        destinations = AdminTab.entries.map { item ->
            val icon = when (item) {
                AdminTab.OVERVIEW -> Icons.Filled.Home
                AdminTab.MENUS -> Icons.AutoMirrored.Filled.MenuBook
                AdminTab.NOTICES -> Icons.Filled.Campaign
                AdminTab.PROFILE -> Icons.Filled.Person
            }
            AdaptiveDestination(item.title, icon)
        },
        onDestinationSelected = { index -> onTabChanged(AdminTab.entries[index]) },
    ) { padding ->
        when (tab) {
            AdminTab.OVERVIEW -> AdminOverview(store, currentNotice, padding, onPublishNotice = onOpenNoticeEditor, onOpenMenus = { onTabChanged(AdminTab.MENUS) })
            AdminTab.MENUS -> AdminMenus(store, dayOffset, onDayChanged, padding, onDataChanged)
            AdminTab.NOTICES -> AdminNoticeScreen(currentNotice, previousNotices, padding, onOpenNoticeEditor, onDeleteCurrentNotice)
            AdminTab.PROFILE -> AdminProfile(
                store, currentAdminId, themePreference, onThemeChanged, padding,
                onAbout, onSwitchToStudent, onSignOut, onDataChanged,
            )
        }
        NoticeEditorDialog(
            visible = showNoticeEditor,
            initialValue = currentNotice?.message.orEmpty(),
            onDismiss = onDismissNoticeEditor,
            onPublish = onPublishNotice,
        )
    }
}

@Composable
private fun AdminOverview(
    store: MessMateStore,
    currentNotice: DiningNotice?,
    padding: PaddingValues,
    onPublishNotice: () -> Unit,
    onOpenMenus: () -> Unit,
) {
    val now = rememberIndiaNow()
    val today = now.toLocalDate()
    val timeNow = now.toLocalTime()
    val orderedSlots = MealSlot.entries.sortedBy { store.mealSchedule(it).start }
    val schedules = MealSlot.entries.associateWith(store::mealSchedule)
    val servingSlot = orderedSlots.firstOrNull { slot ->
        val schedule = schedules.getValue(slot)
        !timeNow.isBefore(schedule.start) && timeNow.isBefore(schedule.end)
    }
    val upcomingSlots = orderedSlots.filter { schedules.getValue(it).start.isAfter(timeNow) }
    val nextSlot = upcomingSlots.firstOrNull() ?: if (servingSlot == null) orderedSlots.firstOrNull() else null
    fun serviceStatus(slot: MealSlot): String = when {
        servingSlot == slot -> "Serving now"
        servingSlot != null && slot in upcomingSlots -> if (slot == upcomingSlots.firstOrNull()) "Up next" else "Later today"
        servingSlot != null -> "Finished"
        upcomingSlots.isNotEmpty() && slot == nextSlot -> "Up next"
        upcomingSlots.isNotEmpty() && schedules.getValue(slot).start.isAfter(timeNow) -> "Later today"
        upcomingSlots.isEmpty() && slot == nextSlot -> "Up next tomorrow"
        else -> "Finished"
    }
    ResponsiveContentArea(Modifier.fillMaxSize().padding(padding)) { columns ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Eyebrow("MESSMATE · DINING TEAM")
                    Text("A good service starts here.", color = MessMatePalette.Forest, style = androidx.compose.material3.MaterialTheme.typography.headlineLarge)
                    Text(prettyDate(today), color = MessMatePalette.Muted, fontSize = 12.sp)
                }
                MessMateLogo(size = 48.dp)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard("Today's meals", "04", "All services planned", Modifier.weight(1f))
                AdminMetricCard("Published notice", if (currentNotice == null) "—" else "01", "Visible to students", Modifier.weight(1f))
            }
        }
        item {
            Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.ForestDark), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Service board", color = MessMatePalette.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("North Mess · Maple House · ${IndiaTime.formatClock(timeNow)} IST", color = MessMatePalette.White.copy(alpha = 0.68f), fontSize = 11.sp)
                        }
                        InfoPill("Live · IST", filled = true)
                    }
                    orderedSlots.forEachIndexed { index, slot ->
                        val status = serviceStatus(slot)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            Box(Modifier.size(25.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                                if (status == "Serving now" || status == "Up next") Icon(Icons.Outlined.Check, contentDescription = "${slot.label}: $status", tint = MessMatePalette.Saffron, modifier = Modifier.size(14.dp))
                                else Text("${index + 1}", color = MessMatePalette.White.copy(alpha = 0.78f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(slot.label, color = MessMatePalette.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(status, color = if (status == "Serving now") MessMatePalette.Saffron else MessMatePalette.White.copy(alpha = 0.62f), fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
                                Text(schedules.getValue(slot).displayTime, color = MessMatePalette.White.copy(alpha = 0.78f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        item { NoticeCard(currentNotice?.message ?: "No notice published yet.", currentNotice?.publishedAt, onPublishNotice) }
        item {
            SectionHeading("Today's menu", "Edit menu & times", onOpenMenus)
        }
        responsiveRows(MealSlot.entries.size, columns) { index ->
            val slot = MealSlot.entries[index]
            val meal = store.mealFor(today.dayOfWeek, slot)
            MealListCard(meal, "${slot.label} · ${meal.time}", favorite = false, onFavorite = {}, onClick = onOpenMenus)
        }
        item {
            Button(
                onClick = onPublishNotice,
                modifier = Modifier.fillMaxWidth().height(51.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
            ) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Publish a dining notice", fontWeight = FontWeight.SemiBold)
            }
        }
      }
    }
}

@Composable
private fun AdminMetricCard(title: String, value: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = MessMatePalette.Muted, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text(value, color = MessMatePalette.Forest, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(caption, color = MessMatePalette.SageDark, fontSize = 9.sp)
        }
    }
}

@Composable
private fun AdminMenus(
    store: MessMateStore,
    dayOffset: Int,
    onDayChanged: (Int) -> Unit,
    padding: PaddingValues,
    onDataChanged: () -> Unit,
) {
    val date = IndiaTime.today().plusDays(dayOffset.toLong())
    var editingSlot by remember { mutableStateOf<MealSlot?>(null) }
    val meals = store.mealsFor(date.dayOfWeek)
    ResponsiveContentArea(Modifier.fillMaxSize().padding(padding)) { columns ->
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        item { ScreenHeading("MENU STUDIO", "What's cooking?", "Tap a meal to edit its dishes or service time.") }
        item { WeekDayPicker(dayOffset, onDayChanged) }
        item {
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Sage), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MessMatePalette.Forest, modifier = Modifier.size(18.dp))
                    Text("Editing ${prettyDate(date)}", color = MessMatePalette.Forest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { SectionHeading("Meals & service times") }
        responsiveRows(MealSlot.entries.size, columns) { index ->
            val slot = MealSlot.entries[index]
            val meal = meals[index]
            MealListCard(meal, "${slot.label} · ${meal.time}", favorite = false, onFavorite = {}, onEdit = { editingSlot = slot })
        }
        item {
            Button(
                onClick = { editingSlot = MealSlot.LUNCH },
                modifier = Modifier.fillMaxWidth().height(51.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
            ) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit a menu or meal time", fontWeight = FontWeight.SemiBold)
            }
        }
      }
    }
    editingSlot?.let { slot ->
        EditMealDialog(
            title = slot.label,
            items = store.mealFor(date.dayOfWeek, slot).items,
            initialSchedule = store.mealSchedule(slot),
            onDismiss = { editingSlot = null },
            onSave = { items, schedule ->
                store.updateMealItems(date.dayOfWeek, slot, items)
                store.updateMealSchedule(slot, schedule)
                editingSlot = null
                onDataChanged()
            },
        )
    }
}

@Composable
private fun AdminNoticeScreen(
    currentNotice: DiningNotice?,
    previousNotices: List<DiningNotice>,
    padding: PaddingValues,
    onEditNotice: () -> Unit,
    onDeleteCurrentNotice: () -> Unit,
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { ScreenHeading("KEEP EVERYONE IN THE KNOW", "A note for the table.", "Share the small details that make a difference.") }
        item {
            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.SaffronSoft), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(19.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.size(34.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Campaign, contentDescription = null, tint = MessMatePalette.Saffron, modifier = Modifier.size(18.dp))
                        }
                        Eyebrow("CURRENT NOTICE", color = MessMatePalette.Forest)
                    }
                    Text(currentNotice?.message ?: "No notice published yet.", color = MessMatePalette.Forest, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                    currentNotice?.let { Text("Published ${it.publishedAt}", color = MessMatePalette.SageDark, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    Text("Students can see this message on their Today screen.", color = MessMatePalette.SageDark, fontSize = 11.sp)
                }
            }
        }
        if (currentNotice != null) {
            item {
                OutlinedButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(15.dp),
                ) {
                    Text("Delete current notice", color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item {
            Button(onClick = onEditNotice, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White)) {
                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(8.dp))
                Text("Write a dining notice", fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = MessMatePalette.Forest, modifier = Modifier.size(21.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("MessMate notifications", color = MessMatePalette.Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("A device notification is sent when permission is enabled.", color = MessMatePalette.Muted, fontSize = 10.sp)
                    }
                }
            }
        }
        item { SectionHeading("Previous notices", "${previousNotices.size} saved") }
        if (previousNotices.isEmpty()) {
            item { Text("Replaced notices will appear here with their publish time.", color = MessMatePalette.Muted, fontSize = 12.sp) }
        } else {
            items(previousNotices) { previous ->
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Eyebrow("PREVIOUS NOTICE", color = MessMatePalette.SageDark)
                        Text(previous.message, color = MessMatePalette.Ink, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                        Text("Published ${previous.publishedAt}", color = MessMatePalette.Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete current notice?") },
            text = { Text("Students will no longer see this notice. Previous notices will remain in the archive.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteCurrentNotice() }) {
                    Text("Delete", color = androidx.compose.material3.MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            containerColor = MessMatePalette.Paper,
        )
    }
}

@Composable
private fun AdminProfile(
    store: MessMateStore,
    currentAdminId: String,
    themePreference: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    padding: PaddingValues,
    onAbout: () -> Unit,
    onSwitchToStudent: () -> Unit,
    onSignOut: () -> Unit,
    onDataChanged: () -> Unit,
) {
    var showAddAdmin by rememberSaveable { mutableStateOf(false) }
    var showPasswordSettings by rememberSaveable { mutableStateOf(false) }
    var rosterMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val importRoster = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val result = store.importAdminRoster(uri, currentAdminId)
            rosterMessage = result.fold(
                onSuccess = { "Imported $it approved IDs from the workbook." },
                onFailure = { it.message ?: "Couldn't read that Excel workbook." },
            )
            if (result.isSuccess) onDataChanged()
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(start = 21.dp, end = 21.dp, top = 21.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { ScreenHeading("YOUR MESSMATE", "Behind every good meal.", "Dining team tools for Maple House.") }
        item {
            Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.ForestDark), elevation = CardDefaults.cardElevation(0.dp)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                    Box(Modifier.size(52.dp).clip(CircleShape).background(MessMatePalette.White.copy(alpha = 0.13f)), contentAlignment = Alignment.Center) {
                        MessMateLogo(size = 34.dp)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(store.adminName(currentAdminId) ?: "Dining team", color = MessMatePalette.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Admin ID · $currentAdminId", color = MessMatePalette.White.copy(alpha = 0.78f), fontSize = 11.sp)
                    }
                }
            }
        }
        item { ThemePreferencePicker(selected = themePreference, onSelect = onThemeChanged) }
        item {
            Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Account settings", color = MessMatePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Update your admin password at any time.", color = MessMatePalette.Muted, fontSize = 12.sp)
                    TextButton(onClick = { showPasswordSettings = true }, contentPadding = PaddingValues(0.dp)) {
                        Text("Change password", color = MessMatePalette.Forest)
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Admin access", color = MessMatePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Add an ID from the approved Excel roster. For a new person, add their 10-digit ID to ID.xlsx, import the updated workbook, then add them here.",
                        color = MessMatePalette.Muted,
                        fontSize = 11.sp,
                    )
                    store.activeAdminIds().forEach { id ->
                        Text("Admin ID · $id", color = MessMatePalette.SageDark, fontSize = 12.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showAddAdmin = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Add admin", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { importRoster.launch(arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("Import ID.xlsx", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    rosterMessage?.let { Text(it, color = MessMatePalette.SageDark, fontSize = 11.sp) }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(19.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("About MessMate", color = MessMatePalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Know what's on the menu.", color = MessMatePalette.Muted, fontSize = 12.sp)
                    TextButton(onClick = onAbout, contentPadding = PaddingValues(0.dp)) { Text("App details", color = MessMatePalette.Forest) }
                }
            }
        }
        item {
            Button(onClick = onSwitchToStudent, modifier = Modifier.fillMaxWidth().height(51.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.Sage)) {
                Text("Preview the student experience", color = MessMatePalette.Forest, fontWeight = FontWeight.SemiBold)
            }
        }
        item {
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out", color = MessMatePalette.Muted, fontWeight = FontWeight.SemiBold) }
        }
    }

    if (showAddAdmin) {
        AddAdminDialog(
            onDismiss = { showAddAdmin = false },
            onAdd = { id ->
                val result = store.addAdminId(id)
                if (result == AddAdminResult.ADDED) {
                    showAddAdmin = false
                    rosterMessage = "Admin added. Their first password is 1234567890."
                    onDataChanged()
                }
                result
            },
        )
    }
    if (showPasswordSettings) {
        ChangeAdminPasswordDialog(
            onDismiss = { showPasswordSettings = false },
            onChangePassword = { current, next -> store.changeAdminPassword(currentAdminId, current, next) },
        )
    }
}

@Composable
private fun AddAdminDialog(onDismiss: () -> Unit, onAdd: (String) -> AddAdminResult) {
    var id by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add an admin", color = MessMatePalette.Forest) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enter the 10-digit ID exactly as it appears in the imported ID.xlsx roster.", color = MessMatePalette.Muted, fontSize = 12.sp)
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it.filter { char -> char in '0'..'9' }.take(10); error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Admin ID") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                error?.let { Text(it, color = MessMatePalette.Saffron, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    error = when (onAdd(id)) {
                        AddAdminResult.ADDED -> null
                        AddAdminResult.INVALID_ID -> "Enter a 10-digit ID."
                        AddAdminResult.NOT_IN_ROSTER -> "This ID isn't in the imported Excel roster. Update ID.xlsx and import it first."
                        AddAdminResult.ALREADY_ACTIVE -> "This ID already has admin access."
                    }
                },
                enabled = id.length == 10,
            ) { Text("Add admin", color = MessMatePalette.Forest, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MessMatePalette.Muted) } },
        containerColor = MessMatePalette.Paper,
    )
}

@Composable
private fun ChangeAdminPasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (current: String, next: String) -> Boolean,
) {
    var current by rememberSaveable { mutableStateOf("") }
    var next by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password", color = MessMatePalette.Forest) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                OutlinedTextField(current, { current = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Current password") }, singleLine = true, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(next, { next = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("New password") }, singleLine = true, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation())
                OutlinedTextField(confirm, { confirm = it; error = null }, modifier = Modifier.fillMaxWidth(), label = { Text("Confirm new password") }, singleLine = true, shape = RoundedCornerShape(14.dp), visualTransformation = PasswordVisualTransformation())
                Text("Use at least 8 characters.", color = MessMatePalette.Muted, fontSize = 11.sp)
                error?.let { Text(it, color = MessMatePalette.Saffron, fontSize = 11.sp) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                error = when {
                    next.length < 8 -> "Use at least 8 characters."
                    next != confirm -> "The new passwords don't match."
                    !onChangePassword(current, next) -> "Current password is incorrect."
                    else -> null
                }
                if (error == null) onDismiss()
            }, enabled = current.isNotBlank() && next.isNotBlank() && confirm.isNotBlank()) {
                Text("Save password", color = MessMatePalette.Forest, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MessMatePalette.Muted) } },
        containerColor = MessMatePalette.Paper,
    )
}

@Composable
private fun EditMealDialog(
    title: String,
    items: List<String>,
    initialSchedule: MealSchedule,
    onDismiss: () -> Unit,
    onSave: (List<String>, MealSchedule) -> Unit,
) {
    var text by remember(title, items) { mutableStateOf(items.joinToString("\n")) }
    var schedule by remember(title, initialSchedule) { mutableStateOf(initialSchedule) }
    val validSchedule = schedule.end.isAfter(schedule.start)
    val editedItems = text.lines().map { it.trim() }.filter { it.isNotBlank() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit $title", color = MessMatePalette.Forest) },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Meal service hours (apply every day)", color = MessMatePalette.Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealTimeButton(
                        label = "Starts",
                        time = schedule.start,
                        modifier = Modifier.weight(1f),
                        onTimeSelected = { schedule = schedule.copy(start = it) },
                    )
                    MealTimeButton(
                        label = "Ends",
                        time = schedule.end,
                        modifier = Modifier.weight(1f),
                        onTimeSelected = { schedule = schedule.copy(end = it) },
                    )
                }
                if (!validSchedule) {
                    Text("End time must be later than start time.", color = MessMatePalette.Saffron, fontSize = 11.sp)
                }
                Text("Menu items · one per line", color = MessMatePalette.Muted, fontSize = 12.sp)
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 7,
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("Add a dish") },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(editedItems, schedule) },
                enabled = validSchedule && editedItems.isNotEmpty(),
            ) {
                Text("Save menu & times", color = MessMatePalette.Forest, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MessMatePalette.Muted) } },
        containerColor = MessMatePalette.Paper,
    )
}

@Composable
private fun MealTimeButton(
    label: String,
    time: LocalTime,
    modifier: Modifier = Modifier,
    onTimeSelected: (LocalTime) -> Unit,
) {
    val context = LocalContext.current
    OutlinedButton(
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> onTimeSelected(LocalTime.of(hour, minute)) },
                time.hour,
                time.minute,
                false,
            ).show()
        },
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = MessMatePalette.Muted, fontSize = 10.sp)
            Text(MealSchedule.formatClock(time), color = MessMatePalette.Forest, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun NoticeEditorDialog(visible: Boolean, initialValue: String, onDismiss: () -> Unit, onPublish: (String) -> Unit) {
    if (!visible) return
    var message by remember(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("A note for the table", color = MessMatePalette.Forest) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("Write a short update students will see in MessMate.", color = MessMatePalette.Muted, fontSize = 12.sp)
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(15.dp),
                    placeholder = { Text("e.g. The dining hall closes early tonight…") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onPublish(message) }, enabled = message.isNotBlank()) {
                Text("Publish update", color = MessMatePalette.Forest, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = MessMatePalette.Muted) } },
        containerColor = MessMatePalette.Paper,
    )
}

