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

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.joe.dolarApp.presentation.about.AboutScreen
import com.joe.dolarApp.presentation.calculator.CalculatorScreen
import com.joe.dolarApp.presentation.common.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TodoNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberNavController(),
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
  startDestination: String = TodoDestinations.CALCULATOR_ROUTE,
  navActions: TodoNavigationActions = remember(navController) {
    TodoNavigationActions(navController)
  }
) {
  val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier
  ) {
    composable(
      TodoDestinations.CALCULATOR_ROUTE
    ) { _ ->
      AppModalDrawer(drawerState, currentRoute, navActions) {
        CalculatorScreen(
          openDrawer = { coroutineScope.launch { drawerState.open() } }
        )
      }
    }
    composable(TodoDestinations.ABOUT_ROUTE) {
      AppModalDrawer(drawerState, currentRoute, navActions) {
        AboutScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
      }
    }
  }
}
