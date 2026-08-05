package com.aura.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aura.core.designsystem.theme.AuraTheme
import com.aura.core.security.TokenStorage
import com.aura.feature.auth.presentation.AuthViewModel
import com.aura.feature.auth.presentation.LoginScreen
import com.aura.feature.auth.presentation.SignUpScreen
import com.aura.feature.detail.presentation.OutfitDetailScreen
import com.aura.feature.detail.presentation.OutfitDetailViewModel
import com.aura.feature.home.presentation.HomeScreen
import com.aura.feature.home.presentation.HomeViewModel
import com.aura.feature.home.presentation.SearchScreen
import com.aura.feature.profile.presentation.ProfileScreen
import com.aura.feature.profile.presentation.ProfileViewModel
import com.aura.feature.profile.presentation.SavedScreen
import com.aura.feature.profile.presentation.UserPhotoScreen
import com.aura.feature.profile.presentation.UserPhotoViewModel
import com.aura.feature.ai.presentation.OutfitWorkspaceScreen
import com.aura.feature.ai.presentation.OutfitWorkspaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AuraTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Screens where bottom navigation should be visible
                val showBottomNav = currentRoute in listOf("home", "search", "saved", "profile")

                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            AuraBottomNavigationBar(
                                navController = navController,
                                currentRoute = currentRoute
                            )
                        }
                    }
                ) { innerPadding ->
                    AuraNavHost(
                        navController = navController,
                        tokenStorage = tokenStorage,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AuraBottomNavigationBar(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, "Home"),
            Triple("search", Icons.Default.Search, "Search"),
            Triple("saved", Icons.Default.Favorite, "Saved"),
            Triple("profile", Icons.Default.Person, "Profile")
        )

        items.forEach { (route, icon, label) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) }
            )
        }
    }
}

object AuraDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val HOME = "home"
    const val SEARCH = "search"
    const val DETAIL = "detail/{outfitId}"
    const val SAVED = "saved"
    const val PROFILE = "profile"
    const val WORKSPACE = "workspace"
    const val USER_PHOTOS = "user_photos"
    const val ANALYSIS = "analysis"
    const val TRY_ON = "try_on"
    const val SIMILAR_PRODUCTS = "similar_products"
    const val SETTINGS = "settings"
}

@Composable
fun AuraNavHost(
    navController: NavHostController,
    tokenStorage: TokenStorage,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AuraDestinations.SPLASH,
        modifier = modifier
    ) {
        composable(AuraDestinations.SPLASH) {
            SplashScreen(
                onSplashComplete = {
                    val hasToken = tokenStorage.getAccessToken() != null
                    if (hasToken) {
                        navController.navigate(AuraDestinations.HOME) {
                            popUpTo(AuraDestinations.SPLASH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(AuraDestinations.LOGIN) {
                            popUpTo(AuraDestinations.SPLASH) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(AuraDestinations.LOGIN) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AuraDestinations.HOME) {
                        popUpTo(AuraDestinations.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(AuraDestinations.SIGNUP)
                },
                viewModel = authViewModel
            )
        }

        composable(AuraDestinations.SIGNUP) {
            val authViewModel = hiltViewModel<AuthViewModel>()
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(AuraDestinations.HOME) {
                        popUpTo(AuraDestinations.SIGNUP) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(AuraDestinations.LOGIN) {
                        popUpTo(AuraDestinations.SIGNUP) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        composable(AuraDestinations.HOME) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(
                onNavigateToSearch = { navController.navigate(AuraDestinations.SEARCH) },
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                onNavigateToProfile = { navController.navigate(AuraDestinations.PROFILE) },
                onNavigateToWorkspace = { imageUri ->
                    val route = if (imageUri != null) {
                        "workspace?imageUri=$imageUri"
                    } else {
                        AuraDestinations.WORKSPACE
                    }
                    navController.navigate(route)
                },
                onNavigateToUserPhotos = { navController.navigate(AuraDestinations.USER_PHOTOS) },
                onNavigateToAnalysis = { navController.navigate(AuraDestinations.ANALYSIS) },
                onNavigateToTryOn = { navController.navigate(AuraDestinations.TRY_ON) },
                onNavigateToSaved = { navController.navigate(AuraDestinations.SAVED) },
                viewModel = homeViewModel
            )
        }

        composable(AuraDestinations.SEARCH) {
            val homeViewModel = hiltViewModel<HomeViewModel>()
            SearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                viewModel = homeViewModel
            )
        }

        composable(AuraDestinations.DETAIL) { backStackEntry ->
            val outfitId = backStackEntry.arguments?.getString("outfitId") ?: ""
            val detailViewModel = hiltViewModel<OutfitDetailViewModel>()
            OutfitDetailScreen(
                outfitId = outfitId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                viewModel = detailViewModel
            )
        }

        composable(AuraDestinations.SAVED) {
            val profileViewModel = hiltViewModel<ProfileViewModel>()
            SavedScreen(
                onNavigateToDetail = { id -> navController.navigate("detail/$id") },
                viewModel = profileViewModel
            )
        }

        composable(AuraDestinations.PROFILE) {
            val profileViewModel = hiltViewModel<ProfileViewModel>()
            val authViewModel = hiltViewModel<AuthViewModel>()
            ProfileScreen(
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(AuraDestinations.LOGIN) {
                        popUpTo(AuraDestinations.HOME) { inclusive = true }
                    }
                },
                onNavigateToUserPhotos = {
                    navController.navigate(AuraDestinations.USER_PHOTOS)
                },
                viewModel = profileViewModel
            )
        }

        composable(
            route = "workspace?imageUri={imageUri}",
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val imageUri = backStackEntry.arguments?.getString("imageUri")
            val workspaceViewModel = hiltViewModel<OutfitWorkspaceViewModel>()
            
            LaunchedEffect(imageUri) {
                if (imageUri != null) {
                    workspaceViewModel.selectImage(imageUri)
                }
            }
            
            OutfitWorkspaceScreen(
                viewModel = workspaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserPhotos = { navController.navigate(AuraDestinations.USER_PHOTOS) },
                onNavigateToAnalysis = { navController.navigate(AuraDestinations.ANALYSIS) },
                onNavigateToTryOn = { navController.navigate(AuraDestinations.TRY_ON) },
                onNavigateToSimilarProducts = { navController.navigate(AuraDestinations.SIMILAR_PRODUCTS) }
            )
        }

        composable(AuraDestinations.USER_PHOTOS) {
            val userPhotoViewModel = hiltViewModel<UserPhotoViewModel>()
            UserPhotoScreen(
                viewModel = userPhotoViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAnalysis = { navController.navigate(AuraDestinations.ANALYSIS) }
            )
        }

        composable(AuraDestinations.ANALYSIS) {
            AnalysisPlaceholderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AuraDestinations.TRY_ON) {
            TryOnPlaceholderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AuraDestinations.SIMILAR_PRODUCTS) {
            SimilarProductsPlaceholderScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(AuraDestinations.SETTINGS) {
            SettingsPlaceholderScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
        delay(1500) // Aura brand presentation window
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F12)), // Sleek Dark Mode Background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Text(
                text = "AURA",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 8.sp,
                color = Color.White
            )
            Text(
                text = "YOUR AI STUDIO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 4.sp,
                color = Color.Gray
            )
        }
    }
}
