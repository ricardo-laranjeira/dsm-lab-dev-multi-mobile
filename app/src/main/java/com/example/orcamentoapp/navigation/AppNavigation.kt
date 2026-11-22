
package com.example.orcamentoapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.orcamentoapp.data.TokenStorage

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val tokenStorage = remember(context) {
        TokenStorage(context).also {
            com.example.orcamentoapp.data.ApiClient.tokenStorage = it
        }
    }

    var clienteEnderecoId by remember {
        mutableIntStateOf(0)
    }

    var clienteEnderecoNome by remember {
        mutableStateOf("")
    }

    NavHost(
        navController = navController,
        startDestination = Rotas.LOGIN
    ) {
        authNavigation(
            navController = navController,
            tokenStorage = tokenStorage
        )

        mainNavigation(
            navController = navController,
            tokenStorage = tokenStorage,
            clienteEnderecoId = clienteEnderecoId,
            clienteEnderecoNome = clienteEnderecoNome,
            onSelecionarCliente = { id, nome ->
                clienteEnderecoId = id
                clienteEnderecoNome = nome
            }
        )
    }
}
