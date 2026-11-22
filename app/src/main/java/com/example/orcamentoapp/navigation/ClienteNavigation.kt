package com.example.orcamentoapp.navigation


import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
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
                navController.navigate(Rotas.ENDERECOS)
            },
            token = tokenStorage.obterToken().orEmpty()
        )
    }

    composable(Rotas.ENDERECOS) {
        EnderecosScreen(
            clienteId = clienteEnderecoId,
            clienteNome = clienteEnderecoNome,
            token = tokenStorage.obterToken().orEmpty(),
            onVoltar = {
                navController.popBackStack()
            }
        )
    }
}
