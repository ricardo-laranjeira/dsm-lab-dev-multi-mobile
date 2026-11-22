package com.example.orcamentoapp.navigation


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.models.LoginRequest
import com.example.orcamentoapp.screens.login.LoginScreen
import kotlinx.coroutines.launch
import java.io.IOException

fun NavGraphBuilder.loginNavigation(
    navController: NavController,
    tokenStorage: TokenStorage
) {
    composable(Rotas.LOGIN) {
        val scope = rememberCoroutineScope()

        var loginErro by remember {
            mutableStateOf<String?>(null)
        }

        var carregandoLogin by remember {
            mutableStateOf(false)
        }

        LoginScreen(
            onEntrar = { email, senha ->
                if (!carregandoLogin) {
                    loginErro = null
                    carregandoLogin = true

                    scope.launch {
                        try {
                            val resposta = ApiClient.usuarioApi.login(
                                LoginRequest(
                                    email = email,
                                    senha = senha
                                )
                            )

                            if (resposta.isSuccessful) {
                                val token =
                                    resposta.body()?.access_token

                                if (!token.isNullOrBlank()) {
                                    tokenStorage.salvarToken(token)

                                    navController.navigate(Rotas.HOME) {
                                        popUpTo(Rotas.LOGIN) {
                                            inclusive = true
                                        }
                                    }
                                } else {
                                    loginErro =
                                        "A API não retornou um token válido."
                                }
                            } else {
                                loginErro = when (resposta.code()) {
                                    401 ->
                                        "E-mail ou senha inválidos."

                                    403 ->
                                        "Acesso não autorizado. Verifique se a conta está ativa."

                                    else ->
                                        "Não foi possível entrar. Código: ${resposta.code()}."
                                }
                            }
                        } catch (_: IOException) {
                            loginErro =
                                "Não foi possível conectar à API. Verifique se ela está em execução."
                        } catch (_: Exception) {
                            loginErro =
                                "Ocorreu um erro ao realizar o login."
                        } finally {
                            carregandoLogin = false
                        }
                    }
                }
            },
            onCadastrar = {
                loginErro = null
                navController.navigate(Rotas.CADASTRO)
            },
            erro = loginErro,
            carregando = carregandoLogin
        )
    }
}
