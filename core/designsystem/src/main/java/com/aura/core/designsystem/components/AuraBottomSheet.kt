package com.aura.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.aura.core.designsystem.theme.AuraShapes

/**
 * Reusable Bottom Sheet component matching modern Material 3 specifications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = AuraShapes.extraLarge,
        modifier = modifier,
        content = content
    )
}
