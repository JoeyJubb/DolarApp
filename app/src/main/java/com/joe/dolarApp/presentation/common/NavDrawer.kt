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

package com.joe.dolarApp.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joe.dolarApp.R
import com.joe.dolarApp.TodoDestinations
import com.joe.dolarApp.TodoNavigationActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppModalDrawer(
  drawerState: DrawerState,
  currentRoute: String,
  navigationActions: TodoNavigationActions,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  content: @Composable () -> Unit
) {
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      AppDrawer(
        currentRoute = currentRoute,
        navigateToTasks = { navigationActions.navigateToCalculator() },
        navigateToAbout = { navigationActions.navigateToAbout() },
        closeDrawer = { coroutineScope.launch { drawerState.close() } }
      )
    }
  ) {
    content()
  }
}

@Composable
private fun AppDrawer(
  currentRoute: String,
  navigateToTasks: () -> Unit,
  navigateToAbout: () -> Unit,
  closeDrawer: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(modifier = modifier.fillMaxSize()) {
      DrawerHeader()
      DrawerButton(
        painter = painterResource(id = R.drawable.ic_list),
        label = stringResource(id = R.string.title_conversion_calculator),
        isSelected = currentRoute == TodoDestinations.CALCULATOR_ROUTE,
        action = {
          navigateToTasks()
          closeDrawer()
        }
      )
      DrawerButton(
        painter = painterResource(id = R.drawable.ic_statistics),
        label = stringResource(id = R.string.title_about),
        isSelected = currentRoute == TodoDestinations.ABOUT_ROUTE,
        action = {
          navigateToAbout()
          closeDrawer()
        }
      )
    }
  }
}

@Composable
private fun DrawerHeader(
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .fillMaxWidth()
      .background(color = MaterialTheme.colorScheme.surfaceDim)
      .height(dimensionResource(id = R.dimen.header_height))
      .padding(dimensionResource(id = R.dimen.header_padding))
  ) {
    

    Image(
      painter = painterResource(id = R.drawable.logo_no_fill),
      contentDescription = null, // decorative element
      modifier = Modifier.width(dimensionResource(id = R.dimen.header_image_width))
    )
    Text(
      text = stringResource(id = R.string.app_name),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
private fun DrawerButton(
  painter: Painter,
  label: String,
  isSelected: Boolean,
  action: () -> Unit,
  modifier: Modifier = Modifier
) {
  val tintColor = if (isSelected) {
    MaterialTheme.colorScheme.secondary
  } else {
    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
  }

  TextButton(
    onClick = action,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
  ) {
    Row(
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        painter = painter,
        contentDescription = null, // decorative
        tint = tintColor
      )
      Spacer(Modifier.width(16.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = tintColor
      )
    }
  }
}

@Preview("Drawer contents")
@Composable
fun PreviewAppDrawer() {
  TodoTheme {
    Surface {
      AppDrawer(
        currentRoute = TodoDestinations.CALCULATOR_ROUTE,
        navigateToTasks = {},
        navigateToAbout = {},
        closeDrawer = {}
      )
    }
  }
}
