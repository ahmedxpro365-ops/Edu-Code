package com.educode.app.features.languages

import androidx.compose.ui.platform.LocalConfiguration
import com.educode.app.components.HubShell
import com.educode.app.components.HubTab
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.ui.theme.*

data class LanguageItem(
    val title: String,
    val subtitle: String,
    val brandName: String,
    val accentColor: Color,
    val progress: Float
)

val languages = listOf(
    LanguageItem("HTML 5", "HTML KINGDOM", "الهيكل واللبنة الأساسية", TierHtml, 0f),
    LanguageItem("CSS 3", "CSS CITY", "تنسيق وتصميم صفحات الويب التفاعلية", TierBeginner, 0f),
    LanguageItem("JavaScript", "JAVASCRIPT VALLEY", "لغة الحركة والتفاعل الذكي", TierJs, 0f),
    LanguageItem("Python", "PYTHON EMPIRE", "لغة الذكاء الاصطناعي وبساطة البرمجة", TierPython, 0f),
    LanguageItem("C++", "C++ FORTRESS", "التطوير البرمجي عالي الأداء والأنظمة", TierAdvanced, 0f),
    LanguageItem("C Language", "C ZONE", "لغة الآلة والتحكم المباشر في الذاكرة", TierC, 0f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    onTabClick: (HubTab) -> Unit,
    onBackClick: () -> Unit,
    onLanguageClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600
    val isExpanded = configuration.screenWidthDp >= 840
    val columns = when {
        isExpanded -> GridCells.Fixed(3)
        isTablet -> GridCells.Fixed(2)
        else -> GridCells.Fixed(1)
    }

    HubShell(
        selectedTab = HubTab.LEARN,
        onTabClick = onTabClick,
        showShell = false
    ) { paddingValues ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("المسارات التعليمية", color = NeonCyan, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "اختر لغة البرمجة لبدء التعلّم",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "إمبراطوريات كاملة تحت تصرفك ومستعدة لاختبار تفكيرك المنطقي من الصفر للاحتراف",
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyVerticalGrid(
                    columns = columns,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(languages) { lang ->
                        LanguageCard(lang = lang, onClick = { onLanguageClick(lang.title) })
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageCard(lang: LanguageItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(lang.accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(lang.title.take(2).uppercase(), color = lang.accentColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(lang.brandName, color = lang.accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(lang.title, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(lang.subtitle, color = TextSecondary, fontSize = 12.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("نسبة الإنجاز", color = TextSecondary, fontSize = 12.sp)
                Text("${(lang.progress * 100).toInt()}%", color = lang.accentColor, fontSize = 12.sp)
            }
            LinearProgressIndicator(
                progress = { lang.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = lang.accentColor,
                trackColor = SurfaceVariantDark
            )
        }
    }
}
