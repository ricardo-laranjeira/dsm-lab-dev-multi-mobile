package com.example.orcamentoapp.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.orcamentoapp.screens.agendamentos.AgendamentosScreen
import com.example.orcamentoapp.screens.orcamentos.OrcamentosScreen

fun NavGraphBuilder.orcamentoNavigation(
    navController: NavController
) {
    composable(Rotas.ORCAMENTOS) {
        OrcamentosScreen(
            onVoltar = {
                navController.popBackStack()
            }
        )
    }

    composable(Rotas.AGENDAMENTOS) {
        AgendamentosScreen(
            onVoltar = {
                navController.popBackStack()
            }
        )
    }
}
