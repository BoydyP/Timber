package com.android.timberworkoutlogs.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlin.collections.contains

private val mainScreenRoutes = setOf(
    AppDestinations.HOME_ROUTE,
    AppDestinations.STATS_ROUTE,
    AppDestinations.HISTORY_ROUTE,
    AppDestinations.TEMPLATES_ROUTE,
    AppDestinations.SETTINGS_ROUTE,
)

fun NavGraphBuilder.homeComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            if (initialState.destination.route in mainScreenRoutes) {
                EnterTransition.None
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        },
        exitTransition = {
            if (targetState.destination.route in mainScreenRoutes || targetState.destination.route?.startsWith(
                    AppDestinations.WORKOUT_ROUTE
                ) == true
            ) {
                ExitTransition.None
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            }
        },
        popEnterTransition = {
            if (initialState.destination.route in mainScreenRoutes || initialState.destination.route?.startsWith(
                    AppDestinations.WORKOUT_ROUTE
                ) == true
            ) {
                EnterTransition.None
            } else {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        },
        popExitTransition = {
            if (targetState.destination.route in mainScreenRoutes) {
                ExitTransition.None
            } else {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            }
        },
        content = content
    )
}

fun NavGraphBuilder.slideComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(300)
            )
        },
        content = content
    )
}


fun NavGraphBuilder.workoutComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            if (initialState.destination.route?.startsWith(AppDestinations.SELECT_EXERCISE_ROUTE) == true) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            } else {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(300)
                )
            }
        },
        exitTransition = {
            if (targetState.destination.route in mainScreenRoutes) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300)
                )
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(300)
                )
            }
        },
        popEnterTransition = {
            if (initialState.destination.route in mainScreenRoutes) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(300)
                )
            } else if (initialState.destination.route?.startsWith(AppDestinations.SELECT_EXERCISE_ROUTE) == true) {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            } else {
                EnterTransition.None
            }
        },
        popExitTransition = {
            if (targetState.destination.route in mainScreenRoutes) {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(300)
                )
            } else {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(300)
                )
            }
        },
        content = content
    )
}
