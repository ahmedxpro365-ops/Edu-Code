package com.educode.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educode.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BugReportScreen(
    onBackClick: () -> Unit,
    viewModel: BugReportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSuccess) {
        AlertDialog(
            onDismissRequest = onBackClick,
            title = { Text("تم الإرسال بنجاح ✅", color = NeonCyan) },
            text = { Text("شكراً لمساهمتك في تحسين التطبيق. سيقوم فريق التطوير بمراجعة بلاغك قريباً.", color = Color.White) },
            confirmButton = {
                Button(onClick = onBackClick) {
                    Text("حسناً")
                }
            },
            containerColor = SurfaceDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إبلاغ عن مشكلة 🐞", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.BugReport,
                contentDescription = null,
                tint = NeonYellow,
                modifier = Modifier.size(64.dp)
            )
            
            Text(
                "ساعدنا في تحسين Edu Code",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            Text(
                "يرجى وصف المشكلة التي واجهتها بالتفصيل.",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
                label = { Text("عنوان المشكلة", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("وصف المشكلة", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonCyan,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Screenshot Placeholder
            OutlinedButton(
                onClick = { /* Implement screenshot logic */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(NeonCyan)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("التقاط لقطة شاشة")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.submitReport() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SurfaceDark)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = SurfaceDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال البلاغ", color = SurfaceDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (uiState.error != null) {
                Text(
                    uiState.error!!,
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
