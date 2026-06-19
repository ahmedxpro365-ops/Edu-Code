package com.educode.app.core.error

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.MainActivity
import com.educode.app.ui.theme.DarkBackground
import com.educode.app.ui.theme.ErrorRed
import com.educode.app.ui.theme.NeonCyan
import com.educode.app.ui.theme.TextPrimary

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "حدث خطأ غير متوقع."

        setContent {
            MaterialTheme {
                ErrorScreen(
                    errorMessage = errorMessage,
                    onRestartClick = {
                        val restartIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(restartIntent)
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
    }
}

@Composable
fun ErrorScreen(errorMessage: String, onRestartClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "عذراً، حدث خطأ!",
            color = ErrorRed,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "لكن لا تقلق، لقد قمنا بتسجيل الخطأ للعمل على إصلاحه.",
            color = TextPrimary,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = errorMessage,
            color = ErrorRed.copy(alpha = 0.8f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onRestartClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
        ) {
            Text(
                text = "العودة للتطبيق",
                color = DarkBackground,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
