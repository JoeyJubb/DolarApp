/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.joe.dolarApp

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.joe.dolarApp.TodoDestinationsArgs.USER_MESSAGE_ARG
import com.joe.dolarApp.TodoScreens.ABOUT_SCREEN
import com.joe.dolarApp.TodoScreens.CALCULATOR_SCREEN

/**
 * Screens used in [TodoDestinations]
 */
private object TodoScreens {
    const val CALCULATOR_SCREEN = "calculator"
    const val ABOUT_SCREEN = "about"
}

/**
 * Arguments used in [TodoDestinations] routes
 */
object TodoDestinationsArgs {
    const val USER_MESSAGE_ARG = "userMessage"
}

/**
 * Destinations used in the [DolarAppActivity]
 */
object TodoDestinations {
    const val CALCULATOR_ROUTE = "$CALCULATOR_SCREEN?$USER_MESSAGE_ARG={$USER_MESSAGE_ARG}"
    const val ABOUT_ROUTE = ABOUT_SCREEN
}

/**
 * Models the navigation actions in the app.
 */
class TodoNavigationActions(private val navController: NavHostController) {

    fun navigateToCalculator(userMessage: Int = 0) {
        val navigatesFromDrawer = userMessage == 0
        navController.navigate(
            CALCULATOR_SCREEN.let {
                if (userMessage != 0) "$it?$USER_MESSAGE_ARG=$userMessage" else it
            }
        ) {
            popUpTo(navController.graph.findStartDestination().id) {
                inclusive = !navigatesFromDrawer
                saveState = navigatesFromDrawer
            }
            launchSingleTop = true
            restoreState = navigatesFromDrawer
        }
    }

    fun navigateToAbout() {
        navController.navigate(TodoDestinations.ABOUT_ROUTE) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
