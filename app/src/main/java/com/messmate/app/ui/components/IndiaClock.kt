package com.messmate.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.messmate.app.data.IndiaTime
import kotlinx.coroutines.delay
import java.time.ZonedDateTime

/** Recompose once per minute so time-sensitive dining labels stay current in India time. */
@Composable
fun rememberIndiaNow(): ZonedDateTime {
    var now by remember { mutableStateOf(IndiaTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = IndiaTime.now()
            val elapsedInMinute = now.second * 1_000L + now.nano / 1_000_000L
            delay((60_000L - elapsedInMinute).coerceAtLeast(250L))
        }
    }
    return now
}
