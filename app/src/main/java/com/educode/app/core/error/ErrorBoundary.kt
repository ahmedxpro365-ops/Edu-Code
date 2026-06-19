package com.educode.app.core.error

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.ui.theme.DarkBackground
import com.educode.app.ui.theme.ErrorRed
import com.educode.app.ui.theme.NeonCyan
import com.educode.app.ui.theme.TextPrimary
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun ErrorBoundary(
    onErrorAction: () -> Unit = {},
    content: @Composable () -> Unit
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    if (hasError) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = ErrorRed,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "حدث خطأ غير متوقع في هذه الشاشة",
                color = ErrorRed,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    hasError = false
                    onErrorAction()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
            ) {
                Text(text = "إعادة المحاولة", color = DarkBackground)
            }
        }
    } else {
        // Technically, Compose does not have a direct error boundary catching composition exceptions.
        // The GlobalExceptionHandler will catch uncaught exceptions and direct to ErrorActivity.
        // This composable serves as a logical wrapper for state-based errors handled gracefully.
        content()
    }
}
