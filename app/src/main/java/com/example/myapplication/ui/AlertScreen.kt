package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.AbilityType
import com.example.myapplication.model.Alert
import com.example.myapplication.utils.AccessibilityUtil

@Composable
fun AlertScreen(alert: Alert, userAbilityType: AbilityType) {
    val context = LocalContext.current
    val accessibilityUtil = AccessibilityUtil(context)

    LaunchedEffect(alert) {
        accessibilityUtil.handleAccessibility(userAbilityType, alert.title, alert.message)
    }

    DisposableEffect(Unit) {
        onDispose {
            accessibilityUtil.shutdown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (userAbilityType == AbilityType.DEAF || userAbilityType == AbilityType.HARD_OF_HEARING) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = alert.title,
                style = MaterialTheme.typography.displayLarge,
                color = if (userAbilityType == AbilityType.DEAF || userAbilityType == AbilityType.HARD_OF_HEARING) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    LocalContentColor.current
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = alert.message,
                style = MaterialTheme.typography.headlineMedium,
                color = if (userAbilityType == AbilityType.DEAF || userAbilityType == AbilityType.HARD_OF_HEARING) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    LocalContentColor.current
                }
            )
        }
    }
}
