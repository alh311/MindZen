package com.starshines.mindzen.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.starshines.mindzen.ui.theme.MindZenTheme

@Composable
fun MindZenApp() {
    val navigationState = rememberNavigationState(
        startRoute = Tab1Route,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys
    )
    val navigator = remember { Navigator(navigationState) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar {
                TOP_LEVEL_ROUTES.forEach { (key, value) ->
                    val isSelected = key == navigationState.topLevelRoute
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navigator.navigate(key)
                            if (isSelected) {
                                navigator.onReselect(key)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = value.icon,
                                contentDescription = value.label
                            )
                        },
                        label = { Text(value.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        AppNavHost(
            navigator = navigator,
            navigationState = navigationState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MindZenAppPreview() {
    MindZenTheme {
        MindZenApp()
    }
}
