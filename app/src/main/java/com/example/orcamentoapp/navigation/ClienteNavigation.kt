package com.example.orcamentoapp.navigation

import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.screens.clientes.ClientesScreen
import com.example.orcamentoapp.screens.enderecos.EnderecosScreen

fun NavGraphBuilder.clienteNavigation(
    navController: NavController,
    tokenStorage: TokenStorage,
    clienteEnderecoId: Int,
    clienteEnderecoNome: String,
    onSelecionarCliente: (Int, String) -> Unit
) {
    composable(Rotas.CLIENTES) {
        ClientesScreen(
            onVoltar = {
                navController.popBackStack()
            },
            onGerenciarEnderecos = { idCliente, nomeCliente ->
                onSelecionarCliente(idCliente, nomeCliente)
                val nomeCodificado = Uri.encode(nomeCliente.ifBlank { "Cliente $idCliente" })
                navController.navigate("enderecos/$idCliente/$nomeCodificado")
            },
            token = tokenStorage.obterToken().orEmpty()
        )
    }

    composable(Rotas.ENDERECOS) {
        if (clienteEnderecoId > 0) {
            EnderecosScreen(
                clienteId = clienteEnderecoId,
                clienteNome = clienteEnderecoNome,
                token = tokenStorage.obterToken().orEmpty(),
                onVoltar = {
                    navController.popBackStack()
                }
            )
        } else {
            ClientesScreen(
                onVoltar = {
                    navController.popBackStack()
                },
                onGerenciarEnderecos = { idCliente, nomeCliente ->
                    onSelecionarCliente(idCliente, nomeCliente)
                    val nomeCodificado = Uri.encode(nomeCliente.ifBlank { "Cliente $idCliente" })
                    navController.navigate("enderecos/$idCliente/$nomeCodificado")
                },
                token = tokenStorage.obterToken().orEmpty()
            )
        }
    }

    composable(
        route = "enderecos/{clienteId}/{clienteNome}",
        arguments = listOf(
            navArgument("clienteId") { type = NavType.IntType },
            navArgument("clienteNome") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val idCliente = backStackEntry.arguments?.getInt("clienteId") ?: clienteEnderecoId
        val nomeClienteRaw = backStackEntry.arguments?.getString("clienteNome").orEmpty()
        val nomeCliente = Uri.decode(nomeClienteRaw).ifBlank { clienteEnderecoNome }

        EnderecosScreen(
            clienteId = idCliente,
            clienteNome = nomeCliente,
            token = tokenStorage.obterToken().orEmpty(),
            onVoltar = {
                navController.popBackStack()
            }
        )
    }
}
