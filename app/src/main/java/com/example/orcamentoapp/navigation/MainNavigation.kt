
package com.example.orcamentoapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.screens.HomeScreen

fun NavGraphBuilder.mainNavigation(
    navController: NavController,
    tokenStorage: TokenStorage,
    clienteEnderecoId: Int,
    clienteEnderecoNome: String,
    onSelecionarCliente: (Int, String) -> Unit
) {
    composable(Rotas.HOME) {
        HomeScreen(
            onSair = {
                tokenStorage.limparToken()

                navController.navigate(Rotas.LOGIN) {
                    popUpTo(Rotas.HOME) {
                        inclusive = true
                    }
                }
            },
            onAbrirPerfil = {
                navController.navigate(Rotas.PERFIL)
            },
            onAbrirClientes = {
                navController.navigate(Rotas.CLIENTES)
            },
            onAbrirCategorias = {
                navController.navigate(Rotas.CATEGORIAS)
            },
            onAbrirMateriais = {
                navController.navigate(Rotas.MATERIAIS)
            },
            onAbrirServicos = {
                navController.navigate(Rotas.SERVICOS)
            },
            onAbrirEnderecos = {
                navController.navigate(Rotas.ENDERECOS)
            },
            onAbrirOrcamentos = {
                navController.navigate(Rotas.ORCAMENTOS)
            },
            onAbrirAgendamentos = {
                navController.navigate(Rotas.AGENDAMENTOS)
            }
        )
    }

    profileNavigation(
        navController = navController,
        tokenStorage = tokenStorage
    )

    clienteNavigation(
        navController = navController,
        tokenStorage = tokenStorage,
        clienteEnderecoId = clienteEnderecoId,
        clienteEnderecoNome = clienteEnderecoNome,
        onSelecionarCliente = onSelecionarCliente
    )

    catalogNavigation(
        navController = navController,
        tokenStorage = tokenStorage
    )

    orcamentoNavigation(
        navController = navController
    )
}
