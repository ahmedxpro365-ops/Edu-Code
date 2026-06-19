package com.educode.app.features.shop

import com.educode.app.R
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educode.app.domain.models.ItemCategory
import com.educode.app.domain.models.ShopItem
import com.educode.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onBackClick: () -> Unit,
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("متجر المطورين 🚀", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Dev Shop Premium", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    CoinDisplay(coins = uiState.user?.coins ?: 0)
                    Spacer(modifier = Modifier.width(8.dp))
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
        ) {
            CategoryTabs(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else {
                val filteredItems = uiState.items.filter { it.category == uiState.selectedCategory }
                
                if (filteredItems.isEmpty()) {
                    EmptyShopMessage()
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredItems) { item ->
                            val isOwned = uiState.user?.purchasedItemIds?.contains(item.id) == true
                            val isEquipped = when (item.category) {
                                ItemCategory.THEME -> uiState.user?.equippedItems?.themeId == item.id
                                ItemCategory.AVATAR -> uiState.user?.equippedItems?.avatarId == item.id
                                ItemCategory.FRAME -> uiState.user?.equippedItems?.frameId == item.id
                                ItemCategory.EFFECT -> uiState.user?.equippedItems?.effectId == item.id
                            }
                            
                            ShopItemCard(
                                item = item,
                                isOwned = isOwned,
                                isEquipped = isEquipped,
                                onBuyClick = { viewModel.purchaseItem(item) },
                                onEquipClick = { viewModel.equipItem(item) },
                                onUnequipClick = { viewModel.unequipItem(item.category) }
                            )
                        }
                    }
                }
            }
        }

        // Error Dialog
        uiState.error?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("خطأ في العملية", color = Color.White) },
                text = { Text(error, color = TextSecondary) },
                confirmButton = {
                    Button(onClick = { viewModel.clearError() }) {
                        Text("حسناً")
                    }
                },
                containerColor = SurfaceDark
            )
        }

        // Success Dialog
        if (uiState.purchaseSuccess) {
            AlertDialog(
                onDismissRequest = { viewModel.clearSuccess() },
                title = { Text("مبروك! 🎉", color = NeonCyan) },
                text = { Text("لقد حصلت على العنصر الجديد بنجاح. هل تريد تجهيزه الآن؟", color = Color.White) },
                confirmButton = {
                    Button(onClick = { viewModel.clearSuccess() }) {
                        Text("تم")
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}

@Composable
fun CoinDisplay(coins: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceVariantDark)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_bit_coin_final_1781762648541),
            contentDescription = null,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(0.5.dp, NeonYellow.copy(alpha = 0.5f), CircleShape),
            contentScale = ContentScale.Crop
        )
        Text("$coins BIT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun CategoryTabs(
    selectedCategory: ItemCategory,
    onCategorySelected: (ItemCategory) -> Unit
) {
    val categories = listOf(
        CategoryData(ItemCategory.THEME, "الثيمات", Icons.Default.Palette),
        CategoryData(ItemCategory.AVATAR, "الأفاتار", Icons.Default.Person),
        CategoryData(ItemCategory.FRAME, "الإطارات", Icons.Default.Wallpaper),
        CategoryData(ItemCategory.EFFECT, "التأثيرات", Icons.Default.AutoAwesome)
    )

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.category == selectedCategory },
        containerColor = SurfaceDark,
        contentColor = NeonCyan,
        edgePadding = 16.dp,
        divider = {},
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOfFirst { it.category == selectedCategory }]),
                color = NeonCyan,
                height = 3.dp
            )
        }
    ) {
        categories.forEach { categoryData ->
            Tab(
                selected = selectedCategory == categoryData.category,
                onClick = { onCategorySelected(categoryData.category) },
                text = {
                    Text(
                        text = categoryData.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selectedCategory == categoryData.category) NeonCyan else TextSecondary
                    )
                },
                icon = {
                    Icon(
                        categoryData.icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (selectedCategory == categoryData.category) NeonCyan else TextSecondary
                    )
                }
            )
        }
    }
}

data class CategoryData(val category: ItemCategory, val label: String, val icon: ImageVector)

@Composable
fun ShopItemCard(
    item: ShopItem,
    isOwned: Boolean,
    isEquipped: Boolean,
    onBuyClick: () -> Unit,
    onEquipClick: () -> Unit,
    onUnequipClick: () -> Unit
) {
    val rarityColor = when (item.rarity) {
        "Legendary" -> NeonPurple
        "Epic" -> NeonCyan
        "Rare" -> SuccessGreen
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (isEquipped) NeonCyan else Color.Transparent, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    when (item.category) {
                        ItemCategory.THEME -> Icons.Default.Palette
                        ItemCategory.AVATAR -> Icons.Default.Person
                        ItemCategory.FRAME -> Icons.Default.Wallpaper
                        ItemCategory.EFFECT -> Icons.Default.AutoAwesome
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = rarityColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(item.rarity, color = rarityColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(12.dp))

            if (isOwned) {
                Button(
                    onClick = { if (isEquipped) onUnequipClick() else onEquipClick() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEquipped) Color.Gray else NeonCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(if (isEquipped) "إلغاء التجهيز" else "تجهيز", color = if (isEquipped) Color.White else Color.Black, fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onBuyClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonYellow),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_bit_coin_final_1781762648541), 
                        contentDescription = null, 
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${item.price} BIT", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyShopMessage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("المتجر فارغ حالياً", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("ابحث عن تحديات جديدة لجمع العملات!", color = TextSecondary, textAlign = TextAlign.Center)
    }
}
