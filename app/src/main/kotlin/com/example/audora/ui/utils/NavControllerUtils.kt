// NavControllerUtils.kt

package com.example.audora.ui.utils

import androidx.navigation.NavController
import com.example.audora.ui.screens.Screens

fun NavController.backToMain() {
    val mainRoutes = Screens.MainScreens.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in mainRoutes
    ) {
        popBackStack()
    }
}
