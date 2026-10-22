package com.messmate.app.ui.screens

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.ui.components.MessMateLogo
import com.messmate.app.ui.theme.MessMatePalette

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MessMatePalette.Canvas).statusBarsPadding().navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 15.dp),
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = MessMatePalette.Forest)
        }
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(108.dp).clip(RoundedCornerShape(34.dp)).background(MessMatePalette.Sage), contentAlignment = Alignment.Center) {
                    MessMateLogo(size = 72.dp)
                }
                Text("MessMate", style = MaterialTheme.typography.headlineLarge, color = MessMatePalette.Forest)
                Text("Know what's on the menu.", color = MessMatePalette.Muted, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(30.dp))
        Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.Paper), elevation = CardDefaults.cardElevation(0.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Made for hostel dining.", color = MessMatePalette.Forest, style = MaterialTheme.typography.titleLarge)
                Text(
                    "MessMate brings today's meals, the week ahead, and the little updates from your dining team into one thoughtful place.",
                    color = MessMatePalette.Muted,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text("Version 1.0.0 · Local demo", color = MessMatePalette.SageDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(13.dp))
        Card(shape = RoundedCornerShape(23.dp), colors = CardDefaults.cardColors(containerColor = MessMatePalette.SaffronSoft), elevation = CardDefaults.cardElevation(0.dp)) {
            Row(Modifier.fillMaxWidth().padding(17.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                MessMateLogo(size = 39.dp)
                Text("A good meal makes a good day feel closer.", color = MessMatePalette.Forest, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        Text("Made with care for the people around the table.", modifier = Modifier.fillMaxWidth().padding(top = 30.dp, bottom = 18.dp), color = MessMatePalette.Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
      }
    }
}
