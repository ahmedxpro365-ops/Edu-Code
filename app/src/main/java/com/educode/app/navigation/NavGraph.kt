package com.educode.app.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.educode.app.features.splash.SplashScreen
import com.educode.app.features.home.HomeScreen
import com.educode.app.features.languages.LanguagesScreen
import com.educode.app.features.learnmode.CourseRoadmapScreen
import com.educode.app.features.learnmode.LessonScreen
import com.educode.app.features.learnmode.LearnViewModel
import com.educode.app.features.learnmode.LearnViewModelFactory
import com.educode.app.core.error.ErrorBoundary
import com.educode.app.di.AppModule
import com.educode.app.features.auth.LoginScreen
import com.educode.app.features.profile.CertificatesScreen
import com.educode.app.features.profile.SettingsScreen
import com.educode.app.features.profile.BugReportScreen
import com.educode.app.features.profile.ProfileScreen
import com.educode.app.features.notifications.NotificationsScreen


import com.educode.app.features.shop.ShopScreen
import com.educode.app.features.shop.InventoryScreen

object Routes {
    const val SPLASH = "splash"
// ... (rest of the object)
    const val LOGIN = "login"
    const val HOME = "home"
    const val LANGUAGES = "languages"
    // Using simple routes for navigation builder convenience:
    const val CHALLENGE_MODE = "challenge_mode"
    const val PROFILE = "profile"
    const val SHOP = "shop"
    const val INVENTORY = "inventory"
    const val CERTIFICATES = "certificates"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val CODE_EDITOR = "code_editor"
    const val BUG_REPORT = "bug_report"
    
    // Dynamic Routes
    fun learnMode(language: String) = "learn_mode/${Uri.encode(language)}"
    fun lesson(lessonId: String) = "lesson/${Uri.encode(lessonId)}"
}

@Composable
fun EduCodeNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.SPLASH
) {
    val onTabClick: (com.educode.app.components.HubTab) -> Unit = { tab ->
        val route = when (tab) {
            com.educode.app.components.HubTab.HOME -> Routes.HOME
            com.educode.app.components.HubTab.LEARN -> Routes.LANGUAGES
            com.educode.app.components.HubTab.EDITOR -> Routes.CODE_EDITOR
            com.educode.app.components.HubTab.CHALLENGES -> Routes.CHALLENGE_MODE
            com.educode.app.components.HubTab.SHOP -> Routes.SHOP
        }
        navController.navigate(route) {
            popUpTo(Routes.HOME) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SPLASH) {
            ErrorBoundary {
                SplashScreen(onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                })
            }
        }
        composable(Routes.HOME) {
            ErrorBoundary {
                HomeScreen(
                    onTabClick = onTabClick,
                    onNavigateToLanguages = { navController.navigate(Routes.LANGUAGES) },
                    onNavigateToShop = { navController.navigate(Routes.SHOP) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToChallenges = { navController.navigate(Routes.CHALLENGE_MODE) },
                    onNavigateToCodeEditor = { navController.navigate(Routes.CODE_EDITOR) },
                    onNavigateToCertificates = { navController.navigate(Routes.CERTIFICATES) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                    onNavigateToInventory = { navController.navigate(Routes.INVENTORY) }
                )
            }
        }
        composable(Routes.LANGUAGES) {
            ErrorBoundary {
                LanguagesScreen(
                    onTabClick = onTabClick,
                    onBackClick = { navController.popBackStack() },
                    onLanguageClick = { lang ->
                        navController.navigate(Routes.learnMode(lang))
                    }
                )
            }
        }
        composable(
            route = "learn_mode/{language}",
            arguments = listOf(navArgument("language") { type = NavType.StringType })
        ) { backStackEntry ->
            val language = backStackEntry.arguments?.getString("language") ?: "HTML"
            val factory = remember { LearnViewModelFactory(AppModule.learnRepository) }
            val viewModel: LearnViewModel = viewModel(factory = factory)
            
            ErrorBoundary {
                CourseRoadmapScreen(
                    language = language,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLessonClick = { lessonId ->
                        navController.navigate(Routes.lesson(lessonId))
                    }
                )
            }
        }
        composable(
            route = "lesson/{lessonId}",
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            ErrorBoundary {
                LessonScreen(
                    lessonId = lessonId,
                    repository = AppModule.learnRepository,
                    onBackClick = { navController.popBackStack() },
                    onLessonCompleted = {
                        navController.popBackStack()
                    }
                )
            }
        }
        composable(Routes.CHALLENGE_MODE) {
            ErrorBoundary {
                com.educode.app.features.challenge.ChallengeScreen(
                    onTabClick = onTabClick,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.SHOP) {
            ErrorBoundary {
                com.educode.app.components.HubShell(
                    selectedTab = com.educode.app.components.HubTab.SHOP,
                    onTabClick = onTabClick
                ) { paddingValues ->
                    ShopScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
        composable(Routes.INVENTORY) {
            ErrorBoundary {
                InventoryScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.PROFILE) {
            ErrorBoundary {
                ProfileScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.CERTIFICATES) {
            ErrorBoundary {
                CertificatesScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.SETTINGS) {
            ErrorBoundary {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Routes.SPLASH) {
                            popUpTo(0)
                        }
                    },
                    onNavigateToBugReport = { navController.navigate(Routes.BUG_REPORT) }
                )
            }
        }
        composable(Routes.BUG_REPORT) {
            ErrorBoundary {
                BugReportScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.NOTIFICATIONS) {
            ErrorBoundary {
                NotificationsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.LOGIN) {
            ErrorBoundary {
                LoginScreen(
                    onBackClick = { navController.popBackStack() },
                    onLoginSuccess = {                
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Routes.CODE_EDITOR) {
            ErrorBoundary {
                com.educode.app.features.codeeditor.CodeEditorScreen(
                    onTabClick = onTabClick,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
