package com.messmate.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import com.messmate.app.data.MessMateStore
import com.messmate.app.data.DiningNotice
import com.messmate.app.data.ThemePreference
import com.messmate.app.data.UserIdentity
import com.messmate.app.data.UserRole
import com.messmate.app.notifications.MessMateNotifications
import com.messmate.app.ui.components.Eyebrow
import com.messmate.app.ui.components.MessMateLogo
import com.messmate.app.ui.components.MessMateWordmark
import com.messmate.app.ui.screens.AboutScreen
import com.messmate.app.ui.screens.AdminShell
import com.messmate.app.ui.screens.AdminTab
import com.messmate.app.ui.screens.LandingScreen
import com.messmate.app.ui.screens.LoginScreen
import com.messmate.app.ui.screens.IdentitySetupScreen
import com.messmate.app.ui.screens.MessMateLaunchSplash
import com.messmate.app.ui.screens.StudentShell
import com.messmate.app.ui.screens.StudentTab
import com.messmate.app.ui.theme.MessMatePalette
import com.messmate.app.ui.theme.MessMateTheme

class MainActivity : ComponentActivity() {
    private lateinit var notificationService: MessMateNotifications

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) notificationService.showDiningUpdate("You're all set for updates from MessMate.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationService = MessMateNotifications(applicationContext)
        val store = MessMateStore(applicationContext)

        setContent {
            var themeName by rememberSaveable { mutableStateOf(store.themePreference().name) }
            val themePreference = ThemePreference.valueOf(themeName)
            MessMateTheme(darkTheme = themePreference == ThemePreference.DARK) {
                MessMateApp(
                    store = store,
                    themePreference = themePreference,
                    onThemeChanged = { selected ->
                        store.setThemePreference(selected)
                        themeName = selected.name
                    },
                    onEnableNotifications = ::enableNotifications,
                    onSystemNotice = notificationService::showDiningUpdate,
                )
            }
        }
    }

    private fun enableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationService.enabled()) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            notificationService.showDiningUpdate("You're all set for updates from MessMate.")
        }
    }
}

private enum class AppRoute { SPLASH, LANDING, SIGN_IN, STUDENT_SETUP, ADMIN_SETUP, MAIN, ABOUT }

@Composable
private fun MessMateApp(
    store: MessMateStore,
    themePreference: ThemePreference,
    onThemeChanged: (ThemePreference) -> Unit,
    onEnableNotifications: () -> Unit,
    onSystemNotice: (String) -> Unit,
) {
    val savedRole = store.savedRole()
    var routeName by rememberSaveable { mutableStateOf(AppRoute.SPLASH.name) }
    var roleName by rememberSaveable { mutableStateOf((savedRole ?: UserRole.STUDENT).name) }
    var studentTabName by rememberSaveable { mutableStateOf(StudentTab.TODAY.name) }
    var adminTabName by rememberSaveable { mutableStateOf(AdminTab.OVERVIEW.name) }
    var dayOffset by rememberSaveable { mutableIntStateOf(0) }
    var showNotice by rememberSaveable { mutableStateOf(false) }
    var showNoticeEditor by rememberSaveable { mutableStateOf(false) }
    var refreshVersion by rememberSaveable { mutableIntStateOf(0) }
    val role = UserRole.valueOf(roleName)
    val currentNotice = remember(refreshVersion) { store.currentNotice() }
    val previousNotices = remember(refreshVersion) { store.previousNotices() }

    fun savedDestination(): AppRoute = when (store.savedRole()) {
        null -> AppRoute.LANDING
        UserRole.STUDENT -> if (store.studentIdentity() == null) AppRoute.STUDENT_SETUP else AppRoute.MAIN
        UserRole.ADMIN -> if (store.currentAdminId()?.let(store::adminName) == null) AppRoute.ADMIN_SETUP else AppRoute.MAIN
    }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(650)
        routeName = savedDestination().name
    }

    fun enterApp(selectedRole: UserRole, adminId: String? = null) {
        roleName = selectedRole.name
        if (selectedRole == UserRole.ADMIN && adminId != null) store.saveAdminSession(adminId)
        store.saveRole(selectedRole)
        if (selectedRole == UserRole.STUDENT) {
            studentTabName = StudentTab.TODAY.name
            routeName = if (store.studentIdentity() == null) AppRoute.STUDENT_SETUP.name else AppRoute.MAIN.name
        } else {
            adminTabName = AdminTab.OVERVIEW.name
            val signedInAdminId = adminId ?: store.currentAdminId()
            routeName = if (signedInAdminId?.let(store::adminName) == null) AppRoute.ADMIN_SETUP.name else AppRoute.MAIN.name
        }
    }

    when (AppRoute.valueOf(routeName)) {
        AppRoute.SPLASH -> MessMateLaunchSplash()
        AppRoute.LANDING -> LandingScreen(
            onStudent = { enterApp(UserRole.STUDENT) },
            onAdmin = { roleName = UserRole.ADMIN.name; routeName = AppRoute.SIGN_IN.name },
        )

        AppRoute.SIGN_IN -> LoginScreen(
            onBack = { routeName = AppRoute.LANDING.name },
            onLogin = { id, password ->
                if (store.authenticateAdmin(id, password)) {
                    enterApp(UserRole.ADMIN, id)
                    true
                } else {
                    false
                }
            },
        )

        AppRoute.STUDENT_SETUP -> IdentitySetupScreen(
            role = UserRole.STUDENT,
            onBack = { routeName = AppRoute.LANDING.name },
            onSave = { name, id ->
                val saved = id?.let { store.saveStudentIdentity(name, it) } ?: false
                if (saved) enterApp(UserRole.STUDENT)
                saved
            },
        )

        AppRoute.ADMIN_SETUP -> {
            val adminId = store.currentAdminId().orEmpty()
            IdentitySetupScreen(
                role = UserRole.ADMIN,
                adminId = adminId,
                onBack = { store.clearRole(); routeName = AppRoute.LANDING.name },
                onSave = { name, _ ->
                    val saved = store.saveAdminName(adminId, name)
                    if (saved) enterApp(UserRole.ADMIN, adminId)
                    saved
                },
            )
        }

        AppRoute.ABOUT -> AboutScreen(onBack = { routeName = AppRoute.MAIN.name })

        AppRoute.MAIN -> {
            if (role == UserRole.ADMIN && !store.hasValidAdminSession()) {
                LandingScreen(
                    onStudent = { enterApp(UserRole.STUDENT) },
                    onAdmin = { roleName = UserRole.ADMIN.name; routeName = AppRoute.SIGN_IN.name },
                )
            } else if (role == UserRole.STUDENT) {
                val identity = remember(refreshVersion) { store.studentIdentity() } ?: UserIdentity("Student", "")
                StudentShell(
                    store = store,
                    identity = identity,
                    themePreference = themePreference,
                    onThemeChanged = { selected -> onThemeChanged(selected); refreshVersion++ },
                    tab = StudentTab.valueOf(studentTabName),
                    onTabChanged = { studentTabName = it.name },
                    dayOffset = dayOffset,
                    onDayChanged = { dayOffset = it.coerceIn(0, 6) },
                    currentNotice = currentNotice,
                    onNotice = { showNotice = true },
                    onAbout = { routeName = AppRoute.ABOUT.name },
                    onEnableNotifications = onEnableNotifications,
                    onSwitchToAdmin = { roleName = UserRole.ADMIN.name; routeName = AppRoute.SIGN_IN.name },
                    onSignOut = { store.clearRole(); routeName = AppRoute.LANDING.name },
                    onDataChanged = { refreshVersion++ },
                )
            } else {
                AdminShell(
                    store = store,
                    currentAdminId = store.currentAdminId().orEmpty(),
                    themePreference = themePreference,
                    onThemeChanged = { selected -> onThemeChanged(selected); refreshVersion++ },
                    tab = AdminTab.valueOf(adminTabName),
                    onTabChanged = { adminTabName = it.name },
                    dayOffset = dayOffset,
                    onDayChanged = { dayOffset = it.coerceIn(0, 6) },
                    currentNotice = currentNotice,
                    previousNotices = previousNotices,
                    onOpenNoticeEditor = { showNoticeEditor = true },
                    showNoticeEditor = showNoticeEditor,
                    onDismissNoticeEditor = { showNoticeEditor = false },
                    onPublishNotice = { message ->
                        val published = store.publishNotice(message)
                        showNoticeEditor = false
                        refreshVersion++
                        onSystemNotice("${published.message}\nPublished ${published.publishedAt}")
                    },
                    onDeleteCurrentNotice = {
                        store.deleteCurrentNotice()
                        refreshVersion++
                    },
                    onAbout = { routeName = AppRoute.ABOUT.name },
                    onSwitchToStudent = { enterApp(UserRole.STUDENT) },
                    onSignOut = { store.clearRole(); routeName = AppRoute.LANDING.name },
                    onDataChanged = { refreshVersion++ },
                )
            }
            if (showNotice && role == UserRole.STUDENT) NoticeDialog(notice = currentNotice, onDismiss = { showNotice = false })
        }
    }
}

@Composable
private fun NoticeDialog(notice: DiningNotice?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { MessMateLogo(size = 36.dp) },
        title = { Text("MessMate dining notice", color = MessMatePalette.Forest) },
        text = {
            androidx.compose.foundation.layout.Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                Text(notice?.message ?: "There is no current dining notice.", color = MessMatePalette.Ink, style = MaterialTheme.typography.bodyLarge)
                notice?.let {
                    Text("Published ${it.publishedAt}", color = MessMatePalette.Muted, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it", color = MessMatePalette.Forest, fontWeight = FontWeight.Bold) } },
        containerColor = MessMatePalette.Paper,
    )
}
