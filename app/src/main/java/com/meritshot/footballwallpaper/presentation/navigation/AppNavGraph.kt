package com.meritshot.footballwallpaper.presentation.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.meritshot.footballwallpaper.presentation.screens.admin.*
import com.meritshot.footballwallpaper.presentation.screens.auth.LoginScreen
import com.meritshot.footballwallpaper.presentation.screens.auth.RegisterScreen
import com.meritshot.footballwallpaper.presentation.screens.user.*
import com.meritshot.footballwallpaper.presentation.viewmodel.AuthViewModel

// ── Route constants ────────────────────────────────────────────
object Routes {
    // Auth
    const val LOGIN    = "login"
    const val REGISTER = "register"

    // User
    const val HOME            = "home"
    const val CATEGORY_DETAIL = "category/{categoryId}/{categoryName}"
    const val SUBCATEGORY     = "subcategory/{subcategoryId}/{subcategoryName}"
    const val WALLPAPER_DETAIL= "wallpaper/{wallpaperId}"
    const val FAVORITES       = "favorites"
    const val PROFILE         = "profile"

    // Admin
    const val ADMIN_DASHBOARD      = "admin_dashboard"
    const val ADMIN_UPLOAD         = "admin_upload"
    const val ADMIN_BULK_UPLOAD    = "admin_bulk_upload"
    const val ADMIN_MANAGE         = "admin_manage"
    const val ADMIN_CATEGORIES     = "admin_categories"

    fun categoryDetail(id: String, name: String) = "category/$id/$name"
    fun subcategory(id: String, name: String)     = "subcategory/$id/$name"
    fun wallpaperDetail(id: String)               = "wallpaper/$id"
}

@Composable
fun AppNavGraph() {
    val navController   = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState(initial = null)

    // Determine start destination
    val startDestination = if (authViewModel.isLoggedIn) Routes.HOME else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { user ->
                    val dest = if (user.isAdmin) Routes.ADMIN_DASHBOARD else Routes.HOME
                    navController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // ── User Panel ────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick   = { id, name -> navController.navigate(Routes.categoryDetail(id, name)) },
                onWallpaperClick  = { id -> navController.navigate(Routes.wallpaperDetail(id)) },
                onFavoritesClick  = { navController.navigate(Routes.FAVORITES) },
                onProfileClick    = { navController.navigate(Routes.PROFILE) },
                onAdminClick      = { navController.navigate(Routes.ADMIN_DASHBOARD) }
            )
        }

        composable(
            Routes.CATEGORY_DETAIL,
            arguments = listOf(
                navArgument("categoryId")   { type = NavType.StringType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { back ->
            val categoryId   = back.arguments?.getString("categoryId") ?: ""
            val categoryName = back.arguments?.getString("categoryName") ?: ""
            CategoryDetailScreen(
                categoryId   = categoryId,
                categoryName = categoryName,
                onBack = { navController.popBackStack() },
                onSubcategoryClick = { id, name -> navController.navigate(Routes.subcategory(id, name)) },
                onWallpaperClick   = { navController.navigate(Routes.wallpaperDetail(it)) }
            )
        }

        composable(
            Routes.SUBCATEGORY,
            arguments = listOf(
                navArgument("subcategoryId")   { type = NavType.StringType },
                navArgument("subcategoryName") { type = NavType.StringType }
            )
        ) { back ->
            val subcategoryId   = back.arguments?.getString("subcategoryId") ?: ""
            val subcategoryName = back.arguments?.getString("subcategoryName") ?: ""
            SubcategoryScreen(
                subcategoryId   = subcategoryId,
                subcategoryName = subcategoryName,
                onBack           = { navController.popBackStack() },
                onWallpaperClick = { navController.navigate(Routes.wallpaperDetail(it)) }
            )
        }

        composable(
            Routes.WALLPAPER_DETAIL,
            arguments = listOf(navArgument("wallpaperId") { type = NavType.StringType })
        ) { back ->
            val wallpaperId = back.arguments?.getString("wallpaperId") ?: ""
            WallpaperDetailScreen(
                wallpaperId = wallpaperId,
                onBack      = { navController.popBackStack() }
            )
        }

        composable(Routes.FAVORITES) {
            FavoritesScreen(
                onBack           = { navController.popBackStack() },
                onWallpaperClick = { navController.navigate(Routes.wallpaperDetail(it)) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack   = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Admin Panel ───────────────────────────────────────
        composable(Routes.ADMIN_DASHBOARD) {
            AdminDashboardScreen(
                onUpload     = { navController.navigate(Routes.ADMIN_UPLOAD) },
                onBulkUpload = { navController.navigate(Routes.ADMIN_BULK_UPLOAD) },
                onManage     = { navController.navigate(Routes.ADMIN_MANAGE) },
                onCategories = { navController.navigate(Routes.ADMIN_CATEGORIES) },
                onBack       = { navController.popBackStack() },
                onLogout     = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_UPLOAD) {
            AdminUploadScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_BULK_UPLOAD) {
            AdminBulkUploadScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE) {
            AdminManageScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_CATEGORIES) {
            AdminCategoriesScreen(onBack = { navController.popBackStack() })
        }
    }
}
