package com.educode.app.features.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.educode.app.domain.models.ItemCategory
import com.educode.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onBackClick: () -> Unit,
    viewModel: ShopViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المخزن الخاص بك 🛠️", color = NeonCyan, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
                val ownedItems = uiState.items.filter { 
                    it.category == uiState.selectedCategory && 
                    uiState.user?.purchasedItemIds?.contains(it.id) == true 
                }
                
                if (ownedItems.isEmpty()) {
                    EmptyInventoryMessage(categoryName = when(uiState.selectedCategory) {
                        ItemCategory.THEME -> "ثيمات"
                        ItemCategory.AVATAR -> "أفاتار"
                        ItemCategory.FRAME -> "إطارات"
                        ItemCategory.EFFECT -> "تأثيرات"
                    })
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(ownedItems) { item ->
                            val isEquipped = when (item.category) {
                                ItemCategory.THEME -> uiState.user?.equippedItems?.themeId == item.id
                                ItemCategory.AVATAR -> uiState.user?.equippedItems?.avatarId == item.id
                                ItemCategory.FRAME -> uiState.user?.equippedItems?.frameId == item.id
                                ItemCategory.EFFECT -> uiState.user?.equippedItems?.effectId == item.id
                            }
                            
                            ShopItemCard(
                                item = item,
                                isOwned = true,
                                isEquipped = isEquipped,
                                onBuyClick = {},
                                onEquipClick = { viewModel.equipItem(item) },
                                onUnequipClick = { viewModel.unequipItem(item.category) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyInventoryMessage(categoryName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("لا تمتلك أي $categoryName", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("اذهب إلى المتجر للحصول على عناصر جديدة!", color = TextSecondary, textAlign = TextAlign.Center)
    }
}
