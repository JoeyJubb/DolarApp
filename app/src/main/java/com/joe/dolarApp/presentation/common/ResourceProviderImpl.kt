package com.joe.dolarApp.presentation.common

import android.content.Context
import android.content.res.Resources
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ResourceProviderImpl @Inject constructor(
  @ApplicationContext appContext: Context,
) : ResourceProvider {

  private val resources: Resources = appContext.resources

  override fun getString(stringRes: Int, vararg formatArgs: Any): String {
    return resources.getString(stringRes, *formatArgs)
  }


}