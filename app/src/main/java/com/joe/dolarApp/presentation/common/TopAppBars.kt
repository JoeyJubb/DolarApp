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

@file:OptIn(ExperimentalMaterial3Api::class)

package com.joe.dolarApp.presentation.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.joe.dolarApp.R

@Composable
fun CalculatorTopBar(
  openDrawer: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.title_conversion_calculator)) },
    navigationIcon = {
      IconButton(onClick = openDrawer) {
        Icon(Icons.Filled.Menu, stringResource(id = R.string.title_conversion_calculator))
      }
    },
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
fun AboutTopAppBar(openDrawer: () -> Unit) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.title_about)) },
    navigationIcon = {
      IconButton(onClick = openDrawer) {
        Icon(Icons.Filled.Menu, stringResource(id = R.string.title_conversion_calculator))
      }
    },
    modifier = Modifier.fillMaxWidth()
  )
}

@Preview
@Composable
private fun CalculatorTopBarPreview() {
  TodoTheme {
    Surface {
      CalculatorTopBar {}
    }
  }
}

@Preview
@Composable
private fun AboutTopAppBarPreview() {
  TodoTheme {
    Surface {
      AboutTopAppBar { }
    }
  }
}
