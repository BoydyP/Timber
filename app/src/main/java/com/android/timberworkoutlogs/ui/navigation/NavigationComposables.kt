package com.android.timberworkoutlogs.ui.navigation

import android.util.Log
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

internal val mainScreenRoutes = setOf(
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
    enterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        if (initialState.destination.route in mainScreenRoutes) {
            EnterTransition.None
        } else {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    },
    exitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        val targetRoute = targetState.destination.route
        val isWorkoutScreen =
            targetRoute == AppDestinations.WORKOUT_ROUTE || targetRoute?.startsWith("${AppDestinations.WORKOUT_ROUTE}?") == true
        if (targetRoute in mainScreenRoutes || isWorkoutScreen) {
            ExitTransition.None
        } else {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        }
    },
    popEnterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        val initialRoute = initialState.destination.route
        val isWorkoutScreen =
            initialRoute == AppDestinations.WORKOUT_ROUTE || initialRoute?.startsWith("${AppDestinations.WORKOUT_ROUTE}?") == true
        if (initialRoute in mainScreenRoutes || isWorkoutScreen) {
            EnterTransition.None
        } else {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
        }
    },
    popExitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        if (targetState.destination.route in mainScreenRoutes) {
            ExitTransition.None
        } else {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
        }
    },
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    Log.d("HomeComposable", "Building composable for route: $route")
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}

fun NavGraphBuilder.slideComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(300)
        )
    },
    exitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Left,
            tween(300)
        )
    },
    popEnterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
        slideIntoContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(300)
        )
    },
    popExitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
        slideOutOfContainer(
            AnimatedContentTransitionScope.SlideDirection.Right,
            tween(300)
        )
    },
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    Log.d("SlideComposable", "Building composable for route: $route")
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}


fun NavGraphBuilder.workoutComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    enterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
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
    exitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
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
    popEnterTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
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
    popExitTransition: (@JvmSuppressWildcards
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
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
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        content = content
    )
}
