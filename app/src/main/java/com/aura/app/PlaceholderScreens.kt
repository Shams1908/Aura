package com.aura.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aura.core.designsystem.components.AuraEmptyState
import com.aura.core.designsystem.components.AuraTopBar

@Composable
fun AnalysisPlaceholderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AuraTopBar(
                title = "AI Outfit Analysis",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        AuraEmptyState(
            title = "AI Outfit Analysis",
            description = "AI analytics will evaluate colors, alignment, and styling profiles. Coming in the next milestone.",
            icon = Icons.Default.Star,
            actionText = "Go Back",
            onActionClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun TryOnPlaceholderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AuraTopBar(
                title = "AI Virtual Try-On",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        AuraEmptyState(
            title = "Virtual Try-On",
            description = "Dress your 3D digital model with selected outfits to preview look and size. Coming in the next milestone.",
            icon = Icons.Default.Person,
            actionText = "Go Back",
            onActionClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun SimilarProductsPlaceholderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AuraTopBar(
                title = "Similar Products Finder",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        AuraEmptyState(
            title = "Similar Products Finder",
            description = "Visual search will retrieve matches for selected garments from leading catalog brands. Coming in the next milestone.",
            icon = Icons.Default.Search,
            actionText = "Go Back",
            onActionClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun SettingsPlaceholderScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AuraTopBar(
                title = "Settings",
                onNavigationClick = onNavigateBack
            )
        },
        modifier = modifier
    ) { paddingValues ->
        AuraEmptyState(
            title = "Settings",
            description = "Manage accounts, security details, and preferences here. Coming in the next milestone.",
            icon = Icons.Default.Info,
            actionText = "Go Back",
            onActionClick = onNavigateBack,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
