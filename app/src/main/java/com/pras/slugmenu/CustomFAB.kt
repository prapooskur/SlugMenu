package com.pras.slugmenu

/**
 * A custom implementation of the default Android [FloatingActionButton], with added support for long presses.
 * This is, without a doubt, a terrible implementation.
 * Default FAB available at https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/FloatingActionButton.kt
 */

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content

/**
 * <a href="https://m3.material.io/components/floating-action-button/overview" class="external" target="_blank">Material Design floating action button</a>.
 *
 * The FAB represents the most important action on a screen. It puts key actions within reach.
 *
 * ![FAB image](https://developer.android.com/images/reference/androidx/compose/material3/fab.png)
 *
 * FAB typically contains an icon, for a FAB with text and an icon, see
 * [ExtendedFloatingActionButton].
 *
 * @sample androidx.compose.material3.samples.FloatingActionButtonSample
 *
 * @param onClick called when this FAB is clicked
 * @param modifier the [Modifier] to be applied to this FAB
 * @param shape defines the shape of this FAB's container and shadow (when using [elevation])
 * @param containerColor the color used for the background of this FAB. Use [Color.Transparent] to
 * have no color.
 * @param contentColor the preferred color for content inside this FAB. Defaults to either the
 * matching content color for [containerColor], or to the current [LocalContentColor] if
 * [containerColor] is not a color from the theme.
 * @param elevation [FloatingActionButtonElevation] used to resolve the elevation for this FAB in
 * different states. This controls the size of the shadow below the FAB. Additionally, when the
 * container color is [ColorScheme.surface], this controls the amount of primary color applied as an
 * overlay. See also: [Surface].
 * @param interactionSource the [MutableInteractionSource] representing the stream of [Interaction]s
 * for this FAB. You can create and pass in your own `remember`ed instance to observe [Interaction]s
 * and customize the appearance / behavior of this FAB in different states.
 * @param content the content of this FAB, typically an [Icon]
 */
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
        modifier = modifier
            .semantics { role = Role.Button }
            .combinedClickable (
                onClick = {
                    onClick()
                    Log.d("FAB","clicked!")
                },
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                    Log.d("FAB","Long clicked!")
                },
            ),
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
                    modifier = Modifier
                        .defaultMinSize(
                            minWidth = 56.dp,
                            minHeight = 56.dp,
                        ),
                    contentAlignment = Alignment.Center,
                ) { content() }
            }
        }
    }
}
