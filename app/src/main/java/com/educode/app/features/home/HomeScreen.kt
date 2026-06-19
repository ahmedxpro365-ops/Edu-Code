package com.educode.app.features.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.educode.app.R
import com.educode.app.components.CyberpunkParticlesBackground
import com.educode.app.ui.theme.*
import com.educode.app.domain.models.ProgressSystem
import com.educode.app.domain.models.User

import com.educode.app.components.HubShell
import com.educode.app.components.HubTab
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTabClick: (HubTab) -> Unit,
    onNavigateToLanguages: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChallenges: () -> Unit,
    onNavigateToCodeEditor: () -> Unit,
    onNavigateToCertificates: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showLevelUpDialog by remember { mutableStateOf(false) }
    var levelUpRankName by remember { mutableStateOf("") }
    var levelUpArabicTitle by remember { mutableStateOf("") }
    var showNoHeartsAlert by remember { mutableStateOf(false) }
    
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.LevelUp -> {
                    levelUpRankName = event.newRank
                    levelUpArabicTitle = event.arabicTitle
                    showLevelUpDialog = true
                }
                is HomeEvent.HeartRestored -> {
                    snackbarHostState.showSnackbar("تم استعادة قلب جديد بنجاح! 💖 (القلوب الآن: ${event.currentHearts}/5)")
                }
            }
        }
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    HubShell(
        selectedTab = HubTab.HOME,
        onTabClick = onTabClick
    ) { paddingValues ->
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_logo_final_1781762635637),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edu-Code", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    ),
                    actions = {
                        Row(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .background(SurfaceVariantDark.copy(alpha=0.8f), RoundedCornerShape(20.dp))
                                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(uiState.user.coins.toString(), color = NeonYellow, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Image(
                                painter = painterResource(id = R.drawable.img_bit_coin_final_1781762648541),
                                contentDescription = "BIT",
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, NeonYellow.copy(alpha = 0.5f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent // Let HubShell handle or keep transparent for particle BG
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CyberpunkParticlesBackground()
                
                if (isTablet) {
                    // TABLET/WIDE GRID LAYOUT
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Left Column: User dashboard stats & auxiliary widgets (taking 40%)
                        Column(
                            modifier = Modifier
                                .weight(0.4f)
                                .fillMaxHeight()
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            HomeTopBanner()
                            
                            UserStatsRow(user = uiState.user, nextHeartCountdown = uiState.nextHeartCountdown)
                            
                            XPProgressBarCard(user = uiState.user)
                            
                            // Extra Widgets for tablet to enrich screen
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = NeonYellow)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("لوحة الصدارة الأسبوعية", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("1. أحمد العكاي", color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("2450 XP", color = NeonYellow, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("2. برموج الرهيب", color = TextPrimary, fontSize = 14.sp)
                                        Text("1920 XP", color = TextSecondary, fontSize = 14.sp)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = NeonCyan)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إنجازاتك البرمجية", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Badge(containerColor = NeonPurple.copy(alpha = 0.2f), contentColor = NeonPurple) {
                                            Text("سيد كوتلن", modifier = Modifier.padding(6.dp))
                                        }
                                        Badge(containerColor = NeonCyan.copy(alpha = 0.2f), contentColor = NeonCyan) {
                                            Text("بطل HTML", modifier = Modifier.padding(6.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Right Column: Journey Action Cards (taking 60%)
                        Column(
                            modifier = Modifier
                                .weight(0.6f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("رحلتك البرمجية", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)

                            MainActionCard(
                                title = "الطور التعليمي الشامل",
                                subtitle = "تعلم لغات برمجة مختلفة وبناء الأساس بالتفصيل والتمرير الذكي",
                                gradient = Brush.linearGradient(listOf(NeonCyan, Color(0xFF00B0FF))),
                                iconRes = null,
                                onClick = onNavigateToLanguages
                            )

                            MainActionCard(
                                title = "الطور البرمجي (المغامرات والتحديات)",
                                subtitle = "اختبر مهاراتك الفائقة من خلال التحديات الـ 50 المتدرجة بالصعوبة",
                                gradient = Brush.linearGradient(listOf(NeonPurple, Color(0xFF4A148C))),
                                iconRes = null,
                                onClick = {
                                    if (uiState.user.hearts <= 0) {
                                        showNoHeartsAlert = true
                                    } else {
                                        onNavigateToChallenges()
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SecondaryActionCard(
                                    title = "متجر المبرمج",
                                    icon = Icons.Default.Storefront,
                                    onClick = onNavigateToShop,
                                    modifier = Modifier.weight(1f)
                                )
                                SecondaryActionCard(
                                    title = "المخزن",
                                    icon = Icons.Default.Inventory2,
                                    onClick = onNavigateToInventory,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SecondaryActionCard(
                                    title = "الشهادات",
                                    icon = Icons.Default.WorkspacePremium,
                                    onClick = onNavigateToCertificates,
                                    modifier = Modifier.weight(1f)
                                )
                                SecondaryActionCard(
                                    title = "الإعدادات",
                                    icon = Icons.Default.Settings,
                                    onClick = onNavigateToSettings,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                } else {
                    // MOBILE SCROLLABLE VIEW (Single column)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                    ) {
                        item {
                            HomeTopBanner()
                        }
                        
                        item {
                            UserStatsRow(user = uiState.user, nextHeartCountdown = uiState.nextHeartCountdown)
                        }
                        
                        item {
                            XPProgressBarCard(user = uiState.user)
                        }
                        
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatPill(
                                    icon = Icons.Default.Storefront,
                                    text = "المتجر",
                                    color = NeonYellow,
                                    modifier = Modifier.weight(1f).height(40.dp).clickable { onNavigateToShop() }
                                )
                                StatPill(
                                    icon = Icons.Default.Inventory2,
                                    text = "المخزن",
                                    color = NeonCyan,
                                    modifier = Modifier.weight(1f).height(40.dp).clickable { onNavigateToInventory() }
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatPill(
                                    icon = Icons.Default.WorkspacePremium,
                                    text = "الشهادات",
                                    color = NeonYellow,
                                    modifier = Modifier.weight(1f).height(40.dp).clickable { onNavigateToCertificates() }
                                )
                                StatPill(
                                    icon = Icons.Default.Settings,
                                    text = "الإعدادات",
                                    color = NeonCyan,
                                    modifier = Modifier.weight(1f).height(40.dp).clickable { onNavigateToSettings() }
                                )
                            }
                        }
                        
                        item {
                            Text("رحلتك", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        
                        item {
                            MainActionCard(
                                title = "الطور التعليمي الشامل",
                                subtitle = "تعلم لغات برمجة مختلفة وبناء الأساس",
                                gradient = Brush.linearGradient(listOf(NeonCyan, Color(0xFF00B0FF))),
                                iconRes = null,
                                onClick = onNavigateToLanguages
                            )
                        }
                        item {
                            MainActionCard(
                                title = "الطور البرمجي (المغامرات)",
                                subtitle = "اختبر مهاراتك من خلال التحديات",
                                gradient = Brush.linearGradient(listOf(NeonPurple, Color(0xFF4A148C))),
                                iconRes = null,
                                onClick = {
                                    if (uiState.user.hearts <= 0) {
                                        showNoHeartsAlert = true
                                    } else {
                                        onNavigateToChallenges()
                                    }
                                }
                            )
                        }
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                SecondaryActionCard(
                                    title = "المتجر",
                                    icon = Icons.Default.ShoppingCart,
                                    onClick = onNavigateToShop,
                                    modifier = Modifier.weight(1f)
                                )
                                SecondaryActionCard(
                                    title = "ملفي",
                                    icon = Icons.Default.Person,
                                    onClick = onNavigateToProfile,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (showLevelUpDialog) {
                        AlertDialog(
                            onDismissRequest = { showLevelUpDialog = false },
                            title = null,
                            text = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val scale by infiniteTransition.animateFloat(
                                        initialValue = 0.9f,
                                        targetValue = 1.2f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = FastOutSlowInEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )

                                    Icon(
                                        Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = NeonYellow,
                                        modifier = Modifier
                                            .size(90.dp)
                                            .scale(scale)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "تباركت الأكواد البرمجية! 🎉",
                                        color = NeonCyan,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "ترقيت بنجاح إلى رتبة برمجية جديدة:",
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = levelUpArabicTitle,
                                        color = try { Color(android.graphics.Color.parseColor(ProgressSystem.getHexColor(levelUpRankName))) } catch (e: Exception) { NeonCyan },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    val coinsBonus = com.educode.app.domain.models.ProgressSystem.RANKS.firstOrNull { it.rank == levelUpRankName }?.bonusCoinsReward ?: 100
                                    Text(
                                        text = "🎁 لقد حصلت على جائزة الترقية:\n+$coinsBonus عملة BIT",
                                        color = NeonYellow,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 22.sp
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = { showLevelUpDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("استمر في غزو البرمجيات! 🚀", color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                            },
                            containerColor = SurfaceDark,
                            shape = RoundedCornerShape(24.dp)
                        )
                    }

                    if (showNoHeartsAlert) {
                        AlertDialog(
                            onDismissRequest = { showNoHeartsAlert = false },
                            icon = { Icon(Icons.Default.HeartBroken, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp)) },
                            title = { Text("💔 نفدت القلوب!", color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                            text = {
                                Text(
                                    "تحديات وضع Challenge Mode معطلة حالياً لأن طاقتك انتهت. يمكنك الانتظار ساعتين لاستعادة قلب تلقائياً، أو اذهب فوراً إلى وضع التعلم (Learn Mode) وحل الدروس لتواصل صعودك البرمجي!",
                                    color = Color.LightGray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = { 
                                        showNoHeartsAlert = false
                                        onNavigateToLanguages()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                                ) {
                                    Text("الذهاب للتعلم 🎓", color = Color.White)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showNoHeartsAlert = false }) {
                                    Text("حسناً", color = Color.Gray)
                                }
                            },
                            containerColor = SurfaceDark,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeTopBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = NeonCyan)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(DarkPurple, SurfaceDark)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_app_logo_final_1781762635637), 
                contentDescription = "Edu Code Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun UserStatsRow(user: com.educode.app.domain.models.User, nextHeartCountdown: String) {
    val rankInfo = com.educode.app.domain.models.ProgressSystem.getRankInfoForXp(user.xp)
    val rankColor = Color(android.graphics.Color.parseColor(com.educode.app.domain.models.ProgressSystem.getHexColor(rankInfo.rank)))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatPill(
            icon = Icons.Default.Star,
            text = com.educode.app.domain.models.ProgressSystem.getArabicTitle(user.rank),
            color = rankColor,
            modifier = Modifier.weight(1f)
        )
        StatPill(
            icon = Icons.Default.Favorite,
            text = if (nextHeartCountdown.isNotEmpty()) "${user.hearts}/5 ⏳ $nextHeartCountdown" else "${user.hearts}/5",
            color = if (user.hearts == 0) ErrorRed else NeonPurple,
            modifier = Modifier.weight(1.5f)
        )
        StatPill(
            icon = Icons.Default.Info,
            text = "${user.xp} XP",
            color = NeonCyan,
            modifier = Modifier.weight(0.9f)
        )
    }
}

@Composable
fun StatPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(SurfaceVariantDark, CircleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun XPProgressBarCard(user: com.educode.app.domain.models.User) {
    val rankInfo = com.educode.app.domain.models.ProgressSystem.getRankInfoForXp(user.xp)
    val progress = com.educode.app.domain.models.ProgressSystem.getProgressPercent(user.xp)
    val xpNeeded = com.educode.app.domain.models.ProgressSystem.getXpNeededForNextRank(user.xp)
    val rankColor = Color(android.graphics.Color.parseColor(com.educode.app.domain.models.ProgressSystem.getHexColor(rankInfo.rank)))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = null,
                        tint = rankColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "الرتبة البرمجية: ",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = rankInfo.arabicTitle,
                        color = rankColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "${user.xp} XP",
                    color = rankColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0.01f, 1f))
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(rankColor.copy(alpha = 0.6f), rankColor)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (xpNeeded > 0) {
                    "باقي $xpNeeded XP للرتبة التالية 🚀"
                } else {
                    "لقد وصلت لقمة الخوارزميات! 👑 أسطورة الشفرة المظلمة."
                },
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    gradient: Brush,
    iconRes: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, animationSpec = spring())

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SecondaryActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, spring())

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = LocalIndication.current, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, tint = NeonCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
