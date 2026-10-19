package com.example.mindzen.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import com.example.mindzen.ui.settings.Tab1SettingsScreen
import com.example.mindzen.ui.settings.Tab2SettingsScreen
import com.example.mindzen.ui.settings.Tab3SettingsScreen
import com.example.mindzen.ui.tabs.Tab1Screen
import com.example.mindzen.ui.tabs.Tab2Screen
import com.example.mindzen.ui.tabs.Tab3Screen
import com.example.mindzen.ui.theme.MindZenTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable

@Serializable
data object Tab1Route : NavKey

@Serializable
data object Tab2Route : NavKey

@Serializable
data object Tab3Route : NavKey

@Serializable
data object Tab1SettingsRoute : NavKey

@Serializable
data object Tab2SettingsRoute : NavKey

@Serializable
data object Tab3SettingsRoute : NavKey

data class NavBarItem(
    val icon: ImageVector,
    val label: String
)

private val TOP_LEVEL_ROUTES = mapOf<NavKey, NavBarItem>(
    Tab1Route to NavBarItem(icon = Icons.Rounded.AutoAwesome, label = "Affirmations"),
    Tab2Route to NavBarItem(icon = Icons.Rounded.SelfImprovement, label = "Breathe"),
    Tab3Route to NavBarItem(icon = Icons.Rounded.Checklist, label = "Lists"),
)

class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelRoute: NavKey by topLevelRoute

    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            )
            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider
            )
        }
        return getTopLevelRoutesInUse()
            .flatMap { decoratedEntries[it] ?: emptyList() }
    }

    private fun getTopLevelRoutesInUse(): List<NavKey> =
        if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }
}

@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): NavigationState {
    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

class Navigator(val state: NavigationState) {
    private val _reselectEvents = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)
    val reselectEvents = _reselectEvents.asSharedFlow()

    fun navigate(route: NavKey) {
        if (route in state.backStacks.keys) {
            state.topLevelRoute = route
        } else {
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun onReselect(route: NavKey) {
        _reselectEvents.tryEmit(route)
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute]
            ?: error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        if (currentRoute == state.startRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}

@Composable
fun MindZenApp() {
    val navigationState = rememberNavigationState(
        startRoute = Tab1Route,
        topLevelRoutes = TOP_LEVEL_ROUTES.keys
    )
    val navigator = remember { Navigator(navigationState) }

    val entryProvider = entryProvider {
        entry<Tab1Route> {
            Tab1Screen(onNavigateToSettings = { navigator.navigate(Tab1SettingsRoute) })
        }
        entry<Tab2Route> {
            Tab2Screen(onNavigateToSettings = { navigator.navigate(Tab2SettingsRoute) })
        }
        entry<Tab3Route> {
            Tab3Screen(onNavigateToSettings = { navigator.navigate(Tab3SettingsRoute) })
        }
        entry<Tab1SettingsRoute> {
            Tab1SettingsScreen(onBack = { navigator.goBack() })
        }
        entry<Tab2SettingsRoute> {
            Tab2SettingsScreen(onBack = { navigator.goBack() })
        }
        entry<Tab3SettingsRoute> {
            Tab3SettingsScreen(onBack = { navigator.goBack() })
        }
    }

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
        NavDisplay(
            entries = navigationState.toDecoratedEntries(entryProvider),
            onBack = { navigator.goBack() },
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
