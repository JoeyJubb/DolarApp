package com.joe.dolarApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.joe.dolarApp.presentation.common.TodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DolarAppActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TodoTheme {
        TodoNavGraph()
      }
    }
  }
}
