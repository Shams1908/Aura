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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
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
import com.aura.feature.camera.presentation.AuraStudioScreen
import com.aura.feature.camera.presentation.StudioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.aura.core.common.session.SessionManager
import com.aura.core.common.session.OutfitSessionId

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStorage: TokenStorage

    @Inject
    lateinit var sessionManager: SessionManager

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
                        sessionManager = sessionManager,
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
    
    @Deprecated("Legacy profile photo path. Workflows are migrated to OutfitSession.", ReplaceWith("AuraDestinations.TRY_ON"))
    const val USER_PHOTOS = "user_photos"
    
    const val ANALYSIS = "analysis"
    
    @Deprecated("Legacy camera path. Workflows are migrated to session-driven try_on?sessionId={sessionId}.", ReplaceWith("try_on?sessionId={sessionId}"))
    const val TRY_ON = "try_on"
    
    const val SIMILAR_PRODUCTS = "similar_products"
    const val SETTINGS = "settings"
}

@Composable
fun AuraNavHost(
    navController: NavHostController,
    tokenStorage: TokenStorage,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, arguments ->
            android.util.Log.d("AURA_DEBUG", "Navigation destination changed: ${destination.route} with arguments: $arguments")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

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
                onNavigateToWorkspace = { referenceImage ->
                    coroutineScope.launch {
                        val session = sessionManager.createNewSession()
                        if (referenceImage != null) {
                            sessionManager.updateReferenceImage(referenceImage)
                        }
                        navController.navigate("workspace?sessionId=${session.sessionId.value}")
                    }
                },
                onNavigateToUserPhotos = {
                    coroutineScope.launch {
                        val session = sessionManager.createNewSession()
                        navController.navigate("try_on?sessionId=${session.sessionId.value}")
                    }
                },
                onNavigateToAnalysis = { navController.navigate(AuraDestinations.ANALYSIS) },
                onNavigateToTryOn = {
                    coroutineScope.launch {
                        val session = sessionManager.createNewSession()
                        navController.navigate("try_on?sessionId=${session.sessionId.value}")
                    }
                },
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
                    coroutineScope.launch {
                        val session = sessionManager.createNewSession()
                        navController.navigate("try_on?sessionId=${session.sessionId.value}")
                    }
                },
                viewModel = profileViewModel
            )
        }

        composable(
            route = "workspace?sessionId={sessionId}",
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            val workspaceViewModel = hiltViewModel<OutfitWorkspaceViewModel>()
            
            LaunchedEffect(sessionId) {
                if (sessionId != null) {
                    workspaceViewModel.loadSession(OutfitSessionId(sessionId))
                }
            }
            
            OutfitWorkspaceScreen(
                viewModel = workspaceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUserPhotos = {
                    coroutineScope.launch {
                        val session = sessionManager.createNewSession()
                        navController.navigate("try_on?sessionId=${session.sessionId.value}")
                    }
                },
                onNavigateToAnalysis = { navController.navigate(AuraDestinations.ANALYSIS) },
                onNavigateToTryOn = {
                    val activeSession = sessionManager.activeSession.value
                    if (activeSession != null) {
                        navController.navigate("try_on?sessionId=${activeSession.sessionId.value}")
                    } else {
                        coroutineScope.launch {
                            val session = sessionManager.createNewSession()
                            navController.navigate("try_on?sessionId=${session.sessionId.value}")
                        }
                    }
                },
                onNavigateToSimilarProducts = { navController.navigate(AuraDestinations.SIMILAR_PRODUCTS) }
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
            LaunchedEffect(imageUri) {
                val session = sessionManager.createNewSession()
                if (imageUri != null) {
                    sessionManager.attachOutfit(
                        outfitUri = imageUri,
                        metadata = com.aura.core.common.data.OutfitModel(
                            id = System.currentTimeMillis().toString(),
                            title = "Legacy Outfit",
                            brand = "Legacy",
                            description = "Visual fit analysis item",
                            imageUrl = imageUri,
                            category = "Clothing",
                            color = "Default",
                            tags = listOf("Legacy"),
                            price = 0.0
                        )
                    )
                }
                navController.navigate("workspace?sessionId=${session.sessionId.value}") {
                    popUpTo("workspace?imageUri={imageUri}") { inclusive = true }
                }
            }
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

        composable(
            route = "try_on?sessionId={sessionId}",
            arguments = listOf(
                navArgument("sessionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId")
            val studioViewModel = hiltViewModel<StudioViewModel>()
            
            LaunchedEffect(sessionId) {
                if (sessionId != null) {
                    studioViewModel.loadSession(OutfitSessionId(sessionId))
                }
            }
            
            AuraStudioScreen(
                viewModel = studioViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPhotoSelected = { uri ->
                    navController.popBackStack()
                }
            )
        }

        composable(AuraDestinations.TRY_ON) {
            LaunchedEffect(Unit) {
                val session = sessionManager.createNewSession()
                navController.navigate("try_on?sessionId=${session.sessionId.value}") {
                    popUpTo(AuraDestinations.TRY_ON) { inclusive = true }
                }
            }
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
