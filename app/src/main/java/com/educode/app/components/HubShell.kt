package com.educode.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.educode.app.ui.theme.*

enum class HubTab(val title: String) {
    HOME("الرئيسية"),
    LEARN("المسارات"),
    EDITOR("المحرر"),
    CHALLENGES("التحديات"),
    SHOP("المتجر")
}

@Composable
fun HubShell(
    selectedTab: HubTab,
    onTabClick: (HubTab) -> Unit,
    showShell: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    if (!showShell) {
        Box(modifier = Modifier.fillMaxSize()) {
            content(PaddingValues(0.dp))
        }
        return
    }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val tabs = listOf(
        HubTab.HOME to Icons.Default.Home,
        HubTab.EDITOR to Icons.Default.Code,
        HubTab.SHOP to Icons.Default.Storefront
    )

    if (isTablet) {
        // TABLET: Side Navigation Rail + content
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = SurfaceDark,
                contentColor = TextSecondary,
                modifier = Modifier.width(80.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                tabs.forEach { (tab, icon) ->
                    val isSelected = tab == selectedTab
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = { onTabClick(tab) },
                        icon = { Icon(icon, contentDescription = tab.title, tint = if (isSelected) NeonCyan else TextSecondary) },
                        label = { Text(tab.title, color = if (isSelected) NeonCyan else TextSecondary) },
                        colors = NavigationRailItemDefaults.colors(
                            indicatorColor = NeonCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                content(PaddingValues(0.dp))
            }
        }
    } else {
        // MOBILE: Bottom Navigation Bar + content
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = SurfaceDark,
                    contentColor = TextSecondary
                ) {
                    tabs.forEach { (tab, icon) ->
                        val isSelected = tab == selectedTab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { onTabClick(tab) },
                            icon = { Icon(icon, contentDescription = tab.title, tint = if (isSelected) NeonCyan else TextSecondary) },
                            label = { Text(tab.title, color = if (isSelected) NeonCyan else TextSecondary) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NeonCyan.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
                content(PaddingValues(top = paddingValues.calculateTopPadding()))
            }
        }
    }
}
