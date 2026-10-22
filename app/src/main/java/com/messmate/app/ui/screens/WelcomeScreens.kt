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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messmate.app.data.UserRole
import com.messmate.app.ui.components.Eyebrow
import com.messmate.app.ui.components.MessMateLogo
import com.messmate.app.ui.components.MessMateWordmark
import com.messmate.app.ui.theme.MessMatePalette

@Composable
fun LandingScreen(
    onStudent: () -> Unit,
    onAdmin: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().background(MessMatePalette.Canvas).statusBarsPadding().navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 18.dp),
      ) {
        MessMateWordmark(markSize = 40.dp)
        Spacer(Modifier.height(31.dp))
        Eyebrow("Your hostel table, made simple")
        Text(
            "Good food.\nBetter days.",
            modifier = Modifier.padding(top = 10.dp),
            style = MaterialTheme.typography.displaySmall,
            color = MessMatePalette.Forest,
        )
        Text(
            "Know what's on the menu.",
            modifier = Modifier.padding(top = 9.dp),
            color = MessMatePalette.Muted,
            fontSize = 15.sp,
        )
        Spacer(Modifier.height(25.dp))
        WelcomePreviewCard()
        Spacer(Modifier.height(27.dp))
        Button(
            onClick = onStudent,
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text("Continue as a student", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = onAdmin,
            modifier = Modifier.fillMaxWidth().height(53.dp),
            shape = RoundedCornerShape(17.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MessMatePalette.Forest),
        ) {
            Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(9.dp))
            Text("I'm on the dining team", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "A little more joy around the table.",
            modifier = Modifier.fillMaxWidth(),
            color = MessMatePalette.Muted,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
      }
    }
}

@Composable
fun MessMateLaunchSplash() {
    Box(
        modifier = Modifier.fillMaxSize().background(MessMatePalette.Canvas),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .clip(RoundedCornerShape(36.dp))
                    .background(Brush.radialGradient(listOf(MessMatePalette.White, MessMatePalette.Sage))),
                contentAlignment = Alignment.Center,
            ) {
                MessMateLogo(size = 76.dp)
            }
            Text("MessMate", color = MessMatePalette.Forest, fontSize = 29.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
            Text("Know what's on the menu.", color = MessMatePalette.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun WelcomePreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MessMatePalette.Sage),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 22.dp, end = 20.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(MessMatePalette.White.copy(alpha = 0.75f), Color.Transparent))),
            )
            Column(Modifier.fillMaxWidth().padding(19.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("Today at North Mess", color = MessMatePalette.SageDark)
                    Spacer(Modifier.weight(1f))
                    MessMateLogo(size = 52.dp)
                }
                Spacer(Modifier.height(9.dp))
                Text("A table worth\nlooking forward to.", color = MessMatePalette.Forest, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(15.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    PreviewDishChip("7:30 AM", "Breakfast")
                    PreviewDishChip("12:30 PM", "Lunch")
                }
            }
        }
    }
}

@Composable
private fun PreviewDishChip(time: String, name: String) {
    Surface(shape = RoundedCornerShape(13.dp), color = MessMatePalette.White.copy(alpha = 0.8f)) {
        Column(Modifier.padding(horizontal = 11.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(time, color = MessMatePalette.SageDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            Text(name, color = MessMatePalette.Forest, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLogin: (id: String, password: String) -> Boolean,
) {
    var id by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val valid = id.length == 10 && password.isNotBlank()

    Box(
        modifier = Modifier.fillMaxSize().background(MessMatePalette.Canvas).statusBarsPadding().navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
      ) {
        IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = MessMatePalette.Forest)
        }
        Spacer(Modifier.height(19.dp))
        MessMateLogo(size = 53.dp)
        Spacer(Modifier.height(17.dp))
        Eyebrow("DINING TEAM ACCESS")
        Text("Welcome to\nMessMate.", modifier = Modifier.padding(top = 5.dp), style = MaterialTheme.typography.headlineLarge, color = MessMatePalette.Forest)
        Text(
            "Keep the menu fresh and your students in the know.",
            modifier = Modifier.padding(top = 7.dp, bottom = 22.dp),
            color = MessMatePalette.Muted,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text("Use the 10-digit ID from ID.xlsx. Students can continue without signing in.", color = MessMatePalette.Muted, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = id,
            onValueChange = { value -> id = value.filter { it in '0'..'9' }.take(10); error = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("10-digit admin ID") },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        Spacer(Modifier.height(11.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            shape = RoundedCornerShape(15.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Hide" else "Show", color = MessMatePalette.SageDark, fontSize = 11.sp)
                }
            },
        )
        if (error != null) {
            Text(error.orEmpty(), modifier = Modifier.padding(top = 10.dp), color = MessMatePalette.Saffron, fontSize = 12.sp)
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { if (!onLogin(id, password)) error = "ID or password is incorrect, or this ID has not been added as an admin." },
            enabled = valid,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
        ) {
            Text("Sign in to MessMate", fontWeight = FontWeight.SemiBold)
        }
        Text(
            "First-time admins can change their password in Account settings after signing in.",
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            color = MessMatePalette.Muted,
            fontSize = 11.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
      }
    }
}

/** First-use profile setup. The app only offers this screen until required identity is saved. */
@Composable
fun IdentitySetupScreen(
    role: UserRole,
    adminId: String? = null,
    onBack: () -> Unit,
    onSave: (name: String, studentId: String?) -> Boolean,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var studentId by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val isStudent = role == UserRole.STUDENT
    val canContinue = name.isNotBlank() && (!isStudent || studentId.isNotBlank())

    Box(
        modifier = Modifier.fillMaxSize().background(MessMatePalette.Canvas).statusBarsPadding()
            .navigationBarsPadding().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = MessMatePalette.Forest)
            }
            MessMateLogo(size = 52.dp)
            Eyebrow(if (isStudent) "YOUR STUDENT PROFILE" else "ADMIN PROFILE")
            Text(
                if (isStudent) "Let's get to know you." else "One last detail.",
                style = MaterialTheme.typography.headlineLarge,
                color = MessMatePalette.Forest,
            )
            Text(
                if (isStudent) "Add your name and student ID to personalize your MessMate profile. No password is needed."
                else "Your admin ID is verified. Add your name to finish setting up your profile.",
                color = MessMatePalette.Muted,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; error = null },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Your name") },
                singleLine = true,
                shape = RoundedCornerShape(15.dp),
            )
            if (isStudent) {
                OutlinedTextField(
                    value = studentId,
                    onValueChange = { studentId = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Student ID") },
                    singleLine = true,
                    shape = RoundedCornerShape(15.dp),
                )
                Text("These details are saved to this device and cannot be edited later.", color = MessMatePalette.Muted, fontSize = 11.sp)
            } else {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MessMatePalette.Sage),
                    elevation = CardDefaults.cardElevation(0.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Verified admin ID", color = MessMatePalette.SageDark, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text(adminId.orEmpty(), color = MessMatePalette.Forest, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text("Your name and ID will be read-only after setup. You can still change your password in Account settings.", color = MessMatePalette.Muted, fontSize = 11.sp)
            }
            error?.let { Text(it, color = MessMatePalette.Saffron, fontSize = 12.sp) }
            Button(
                onClick = {
                    if (!onSave(name, if (isStudent) studentId else null)) error = "Couldn't save these details. Please check the fields and try again."
                },
                enabled = canContinue,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MessMatePalette.ForestDark, contentColor = MessMatePalette.White),
            ) {
                Text("Continue to MessMate", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
