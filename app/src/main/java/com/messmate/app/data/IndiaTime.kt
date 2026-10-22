package com.messmate.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** India Standard Time helpers shared by the board, menu calendar, and notice timestamps. */
object IndiaTime {
    val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    private val noticeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a 'IST'", Locale.ENGLISH)
    private val clockFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

    fun now(): ZonedDateTime = ZonedDateTime.now(zone)
    fun today(): LocalDate = LocalDate.now(zone)
    fun time(): LocalTime = LocalTime.now(zone)
    fun formatNoticeTimestamp(epochMillis: Long): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).format(noticeFormatter)

    fun formatClock(time: LocalTime): String = time.format(clockFormatter)
}

