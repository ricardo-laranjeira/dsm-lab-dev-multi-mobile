package com.example.orcamentoapp.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.screens.categorias.CategoriasScreen
import com.example.orcamentoapp.screens.materiais.MateriaisScreen
import com.example.orcamentoapp.screens.servicos.ServicosScreen

fun NavGraphBuilder.catalogNavigation(
    navController: NavController,
    tokenStorage: TokenStorage
) {
    composable(Rotas.CATEGORIAS) {
        CategoriasScreen(
            token = tokenStorage.obterToken().orEmpty(),
            onVoltar = {
                navController.popBackStack()
            }
        )
    }

    composable(Rotas.MATERIAIS) {
        MateriaisScreen(
            onVoltar = {
                navController.popBackStack()
            }
        )
    }

    composable(Rotas.SERVICOS) {
        ServicosScreen(
            onVoltar = {
                navController.popBackStack()
            }
        )
    }
}
