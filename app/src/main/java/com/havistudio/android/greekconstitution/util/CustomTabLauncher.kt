package com.havistudio.android.greekconstitution.util

import android.content.Context
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

internal fun openCustomTab(context: Context, url: String, toolbarColorArgb: Int) {
    val colorParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(toolbarColorArgb)
        .build()
    val intent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(colorParams)
        .setShowTitle(true)
        .build()
    intent.launchUrl(context, url.toUri())
}
