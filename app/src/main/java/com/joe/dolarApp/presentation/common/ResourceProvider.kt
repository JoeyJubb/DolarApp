package com.joe.dolarApp.presentation.common

import androidx.annotation.StringRes

interface ResourceProvider {
  fun getString(@StringRes stringRes: Int, vararg formatArgs: Any): String

}
