
package com.example.orcamentoapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.example.orcamentoapp.data.TokenStorage

fun NavGraphBuilder.authNavigation(
    navController: NavController,
    tokenStorage: TokenStorage
) {
    loginNavigation(
        navController = navController,
        tokenStorage = tokenStorage
    )

    cadastroNavigation(
        navController = navController
    )
}
