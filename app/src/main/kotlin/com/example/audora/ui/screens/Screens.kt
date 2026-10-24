// Screens.kt

package com.example.audora.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.example.audora.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    val iconIdInactive: Int,
    val iconIdActive: Int,
    val route: String,
) {
    object Home : Screens(
        titleId = R.string.home,
        iconIdInactive = R.raw.home,
        iconIdActive = R.raw.home_filled,
        route = "home"
    )

    object Search : Screens(
        titleId = R.string.search,
        iconIdInactive = R.raw.search,
        iconIdActive = R.raw.search_filled,
        route = "search_input"
    )

    object Library : Screens(
        titleId = R.string.your_library,
        iconIdInactive = R.raw.lib,
        iconIdActive = R.raw.lib_fill,
        route = "library"
    )

    object SmartMix : Screens(
        titleId = R.string.smart_mix,
        iconIdInactive = R.drawable.equalizer,
        iconIdActive = R.drawable.equalizer,
        route = "smart_mix"
    )

    object Settings : Screens(
        titleId = R.string.settings,
        iconIdInactive = R.drawable.settings_outline,
        iconIdActive = R.drawable.settings_filled,
        route = "settings"
    )

    companion object {
        val MainScreens = listOf(Home, Search, Library, SmartMix)
    }
}

enum class NavigationTab {
    HOME, SEARCH
}

enum class DarkMode {
    AUTO, ON, OFF
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}

enum class OptionStats {
    WEEKS, MONTHS, YEARS, CONTINUOUS
}
