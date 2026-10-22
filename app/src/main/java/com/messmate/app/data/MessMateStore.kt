package com.messmate.app.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalTime
import java.security.MessageDigest

/** Local demo repository for MessMate; replace with an authenticated campus API for shared data. */
class MessMateStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = context.applicationContext
        .getSharedPreferences("messmate_demo", Context.MODE_PRIVATE)

    fun savedRole(): UserRole? {
        val role = preferences.getString(KEY_ROLE, null)
            ?.let { value -> UserRole.entries.firstOrNull { it.name == value } }
        if (role == UserRole.ADMIN && !hasValidAdminSession()) return null
        return role
    }

    fun saveRole(role: UserRole) {
        preferences.edit().putString(KEY_ROLE, role.name).apply()
    }

    fun clearRole() {
        preferences.edit().remove(KEY_ROLE).remove(KEY_SESSION_ADMIN_ID).apply()
    }

    fun currentAdminId(): String? = preferences.getString(KEY_SESSION_ADMIN_ID, null)

    fun saveAdminSession(id: String) {
        preferences.edit().putString(KEY_SESSION_ADMIN_ID, id).apply()
    }

    /** A student identity is collected once on this installation and then kept read-only. */
    fun studentIdentity(): UserIdentity? {
        val name = preferences.getString(KEY_STUDENT_NAME, null)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val id = preferences.getString(KEY_STUDENT_ID, null)?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return UserIdentity(name, id)
    }

    fun saveStudentIdentity(name: String, id: String): Boolean {
        val cleanName = name.trim()
        val cleanId = id.trim()
        if (cleanName.isEmpty() || cleanId.isEmpty() || studentIdentity() != null) return false
        preferences.edit().putString(KEY_STUDENT_NAME, cleanName).putString(KEY_STUDENT_ID, cleanId).apply()
        return true
    }

    fun adminName(id: String): String? = preferences.getString(adminNameKey(id), null)
        ?.trim()?.takeIf { it.isNotEmpty() }

    /** Admin names are stored per approved, activated ID and cannot be edited after first entry. */
    fun saveAdminName(id: String, name: String): Boolean {
        val cleanName = name.trim()
        if (cleanName.isEmpty() || id !in activeAdminIds() || !isApprovedAdminId(id) || adminName(id) != null) return false
        preferences.edit().putString(adminNameKey(id), cleanName).apply()
        return true
    }

    fun themePreference(): ThemePreference = preferences.getString(KEY_THEME, null)
        ?.let { saved -> ThemePreference.entries.firstOrNull { it.name == saved } }
        ?: ThemePreference.LIGHT

    fun setThemePreference(theme: ThemePreference) {
        preferences.edit().putString(KEY_THEME, theme.name).apply()
    }

    fun hasValidAdminSession(): Boolean = currentAdminId()?.let { id ->
        id in activeAdminIds() && isApprovedAdminId(id)
    } ?: false

    /**
     * The roster is stored as SHA-256 hashes so the workbook's student/admin identifiers are
     * not embedded as readable values in the installed app. An updated workbook can replace it.
     */
    fun isApprovedAdminId(id: String): Boolean = id.isTenDigitId() && id.sha256() in adminRosterHashes()

    fun activeAdminIds(): List<String> = preferences.getString(KEY_ACTIVE_ADMINS, null)
        ?.split('\n')
        ?.map { it.trim() }
        ?.filter { it.isTenDigitId() }
        .orEmpty()

    /** The first successful roster ID becomes the local owner on a fresh installation. */
    fun authenticateAdmin(id: String, password: String): Boolean {
        if (!id.isTenDigitId() || password.isEmpty() || !isApprovedAdminId(id)) return false

        val activeIds = activeAdminIds()
        if (activeIds.isEmpty()) {
            if (password != INITIAL_ADMIN_PASSWORD) return false
            saveActiveAdminIds(listOf(id))
            return true
        }
        if (id !in activeIds) return false

        val savedHash = preferences.getString(passwordKey(id), null)
        val expectedHash = savedHash ?: INITIAL_ADMIN_PASSWORD.sha256()
        return password.sha256() == expectedHash
    }

    fun addAdminId(id: String): AddAdminResult {
        if (!id.isTenDigitId()) return AddAdminResult.INVALID_ID
        if (!isApprovedAdminId(id)) return AddAdminResult.NOT_IN_ROSTER
        val activeIds = activeAdminIds()
        if (id in activeIds) return AddAdminResult.ALREADY_ACTIVE
        saveActiveAdminIds(activeIds + id)
        return AddAdminResult.ADDED
    }

    fun changeAdminPassword(id: String, currentPassword: String, newPassword: String): Boolean {
        if (id !in activeAdminIds() || !isApprovedAdminId(id) || newPassword.length < MIN_PASSWORD_LENGTH) return false
        val savedHash = preferences.getString(passwordKey(id), null)
        val expectedHash = savedHash ?: INITIAL_ADMIN_PASSWORD.sha256()
        if (currentPassword.sha256() != expectedHash) return false
        preferences.edit().putString(passwordKey(id), newPassword.sha256()).apply()
        return true
    }

    /** Imports the first worksheet's column A as the current approved admin-ID roster. */
    fun importAdminRoster(uri: Uri, currentAdminId: String): Result<Int> = runCatching {
        val importedIds = appContext.contentResolver.openInputStream(uri)?.use { input ->
            XlsxAdminRoster.readIds(input)
        }
            ?: error("Couldn't open the selected workbook.")
        val hashes = importedIds.map { it.sha256() }.toSet()
        require(hashes.isNotEmpty()) { "No valid 10-digit IDs were found in the first sheet." }
        require(currentAdminId.isTenDigitId() && currentAdminId.sha256() in hashes) {
            "The workbook must include your current admin ID."
        }

        val active = activeAdminIds().filter { it.sha256() in hashes }
        preferences.edit()
            .putString(KEY_ADMIN_ROSTER, hashes.sorted().joinToString("\n"))
            .putString(KEY_ACTIVE_ADMINS, active.joinToString("\n"))
            .apply()
        hashes.size
    }

    private fun adminRosterHashes(): Set<String> {
        val savedRoster = preferences.getString(KEY_ADMIN_ROSTER, null)
        if (savedRoster != null) return savedRoster.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        return runCatching {
            appContext.assets.open("admin_id_hashes.txt").bufferedReader().useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            }
        }.getOrDefault(emptySet())
    }

    private fun saveActiveAdminIds(ids: List<String>) {
        preferences.edit().putString(KEY_ACTIVE_ADMINS, ids.distinct().joinToString("\n")).apply()
    }

    fun mealsFor(day: DayOfWeek): List<Meal> = MealSlot.entries.map { slot -> mealFor(day, slot) }

    fun mealSchedule(slot: MealSlot): MealSchedule {
        val default = slot.defaultSchedule
        val startMinutes = preferences.getInt(startTimeKey(slot), default.start.toMinuteOfDay())
        val endMinutes = preferences.getInt(endTimeKey(slot), default.end.toMinuteOfDay())
        return MealSchedule(startMinutes.toLocalTime(), endMinutes.toLocalTime())
    }

    fun updateMealSchedule(slot: MealSlot, schedule: MealSchedule) {
        require(schedule.end.isAfter(schedule.start)) { "Meal service must end after it starts." }
        preferences.edit()
            .putInt(startTimeKey(slot), schedule.start.toMinuteOfDay())
            .putInt(endTimeKey(slot), schedule.end.toMinuteOfDay())
            .apply()
    }

    fun mealFor(day: DayOfWeek, slot: MealSlot): Meal {
        val base = SampleMenus.meal(day, slot)
        val custom = preferences.getString(menuKey(day, slot), null)
        val items = custom?.split('\n')?.filter { it.isNotBlank() } ?: base.items
        return base.copy(
            name = items.firstOrNull() ?: base.name,
            time = mealSchedule(slot).displayTime,
            items = items,
        )
    }

    fun updateMealItems(day: DayOfWeek, slot: MealSlot, items: List<String>) {
        preferences.edit().putString(menuKey(day, slot), items.joinToString("\n")).apply()
    }

    fun favorites(): Set<String> = preferences.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()

    fun toggleFavorite(id: String): Set<String> {
        val updated = favorites().toMutableSet().apply {
            if (!add(id)) remove(id)
        }
        preferences.edit().putStringSet(KEY_FAVORITES, updated).apply()
        return updated
    }

    fun currentNotice(): DiningNotice? {
        val savedMessage = preferences.getString(KEY_NOTICE_MESSAGE, null)
        if (!savedMessage.isNullOrBlank()) {
            val publishedAt = preferences.getLong(KEY_NOTICE_PUBLISHED_AT, 0L).takeIf { it > 0L }
                ?: System.currentTimeMillis().also { timestamp ->
                    preferences.edit().putLong(KEY_NOTICE_PUBLISHED_AT, timestamp).apply()
                }
            return DiningNotice(savedMessage, publishedAt)
        }

        // Migrate notices created by older builds, which stored only the message.
        val legacyMessage = preferences.getString(KEY_LEGACY_NOTICE, null)?.takeIf { it.isNotBlank() } ?: return null
        val publishedAt = System.currentTimeMillis()
        preferences.edit()
            .putString(KEY_NOTICE_MESSAGE, legacyMessage)
            .putLong(KEY_NOTICE_PUBLISHED_AT, publishedAt)
            .remove(KEY_LEGACY_NOTICE)
            .apply()
        return DiningNotice(legacyMessage, publishedAt)
    }

    fun previousNotices(): List<DiningNotice> {
        val json = preferences.getString(KEY_PREVIOUS_NOTICES, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(json)
            List(array.length()) { index ->
                val item = array.getJSONObject(index)
                DiningNotice(item.getString(JSON_MESSAGE), item.getLong(JSON_PUBLISHED_AT))
            }
        }.getOrDefault(emptyList())
    }

    /** Publishing a replacement moves the existing current notice into timestamped history. */
    fun publishNotice(message: String): DiningNotice {
        require(message.isNotBlank()) { "A notice cannot be empty." }
        val previous = currentNotice()
        val published = DiningNotice(message.trim(), System.currentTimeMillis())
        val history = buildList {
            if (previous != null) add(previous)
            addAll(previousNotices())
        }
        val historyJson = JSONArray().apply {
            history.forEach { notice ->
                put(JSONObject().put(JSON_MESSAGE, notice.message).put(JSON_PUBLISHED_AT, notice.publishedAtEpochMillis))
            }
        }
        preferences.edit()
            .putString(KEY_NOTICE_MESSAGE, published.message)
            .putLong(KEY_NOTICE_PUBLISHED_AT, published.publishedAtEpochMillis)
            .putString(KEY_PREVIOUS_NOTICES, historyJson.toString())
            .remove(KEY_LEGACY_NOTICE)
            .apply()
        return published
    }

    /** Deleting the current notice leaves the already-published history intact. */
    fun deleteCurrentNotice() {
        preferences.edit()
            .remove(KEY_NOTICE_MESSAGE)
            .remove(KEY_NOTICE_PUBLISHED_AT)
            .remove(KEY_LEGACY_NOTICE)
            .apply()
    }

    private fun menuKey(day: DayOfWeek, slot: MealSlot) = "menu_${day.name}_${slot.name}"
    private fun startTimeKey(slot: MealSlot) = "meal_start_${slot.name}"
    private fun endTimeKey(slot: MealSlot) = "meal_end_${slot.name}"
    private fun passwordKey(id: String) = "admin_password_${id}"
    private fun adminNameKey(id: String) = "admin_name_${id}"

    private fun String.isTenDigitId(): Boolean = length == 10 && all { it in '0'..'9' }

    private fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
        .digest(toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> (byte.toInt() and 0xff).toString(16).padStart(2, '0') }

    private fun LocalTime.toMinuteOfDay(): Int = hour * 60 + minute

    private fun Int.toLocalTime(): LocalTime = LocalTime.of(this / 60, this % 60)

    private companion object {
        const val KEY_ROLE = "role"
        const val KEY_SESSION_ADMIN_ID = "session_admin_id"
        const val KEY_STUDENT_NAME = "student_name"
        const val KEY_STUDENT_ID = "student_id"
        const val KEY_THEME = "theme_preference"
        const val KEY_ACTIVE_ADMINS = "active_admin_ids"
        const val KEY_ADMIN_ROSTER = "admin_roster_hashes"
        const val KEY_FAVORITES = "favorite_dishes"
        const val KEY_NOTICE_MESSAGE = "dining_notice_message"
        const val KEY_NOTICE_PUBLISHED_AT = "dining_notice_published_at"
        const val KEY_PREVIOUS_NOTICES = "previous_dining_notices"
        const val KEY_LEGACY_NOTICE = "dining_notice"
        const val JSON_MESSAGE = "message"
        const val JSON_PUBLISHED_AT = "published_at"
        const val INITIAL_ADMIN_PASSWORD = "1234567890"
        const val MIN_PASSWORD_LENGTH = 8
    }
}

data class UserIdentity(val name: String, val id: String)

enum class ThemePreference(val label: String) { LIGHT("Light"), DARK("Dark") }

enum class AddAdminResult { ADDED, INVALID_ID, NOT_IN_ROSTER, ALREADY_ACTIVE }

data class DiningNotice(val message: String, val publishedAtEpochMillis: Long) {
    val publishedAt: String get() = IndiaTime.formatNoticeTimestamp(publishedAtEpochMillis)
}
