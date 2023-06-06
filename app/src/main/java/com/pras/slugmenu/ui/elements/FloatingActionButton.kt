package com.pras.slugmenu.ui.elements

/**
 * A custom implementation of the default Android [FloatingActionButton], with added support for long presses.
 * This is, without a doubt, a terrible implementation.
 * Default FAB available at https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/FloatingActionButton.kt
 */

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomFloatingActionButton(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = FloatingActionButtonDefaults.containerColor,
    contentColor: Color = contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current

    Surface(
//        onClick = onClick,
        modifier = modifier.semantics { role = Role.Button },
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 16.dp,
        shadowElevation = 16.dp,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            // Adding the text style from [ExtendedFloatingActionButton] to all FAB variations. In
            // the majority of cases this will have no impact, because icons are expected, but if a
            // developer decides to put some short text to emulate an icon, (like "?") then it will
            // have the correct styling.
            ProvideTextStyle(
                MaterialTheme.typography.labelLarge,
            ) {
                Box(
                    //By using combinedclickable on the box instead of the surface, the ripple effect does not extend past the rounded corners.
                    modifier = Modifier
                        .defaultMinSize(minWidth = 56.dp,minHeight = 56.dp)
                        .combinedClickable (
                            role = Role.Button,
                            onClick = {
                                onClick()
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onLongClick()
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) { content() }
            }
        }
    }
}
