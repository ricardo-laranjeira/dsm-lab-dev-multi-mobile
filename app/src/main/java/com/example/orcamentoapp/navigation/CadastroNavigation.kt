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
import com.example.orcamentoapp.data.models.UsuarioCadastroRequest
import com.example.orcamentoapp.screens.cadastro.CadastroScreen
import kotlinx.coroutines.launch
import java.io.IOException

fun NavGraphBuilder.cadastroNavigation(
    navController: NavController
) {
    composable(Rotas.CADASTRO) {
        val scope = rememberCoroutineScope()

        var cadastroErro by remember {
            mutableStateOf<String?>(null)
        }

        var carregandoCadastro by remember {
            mutableStateOf(false)
        }

        CadastroScreen(
            onVoltar = {
                if (!carregandoCadastro) {
                    cadastroErro = null
                    navController.popBackStack()
                }
            },
            onCadastro = { nome, email, senha ->
                if (!carregandoCadastro) {
                    cadastroErro = null
                    carregandoCadastro = true

                    scope.launch {
                        try {
                            val resposta =
                                ApiClient.usuarioApi.cadastrarUsuario(
                                    UsuarioCadastroRequest(
                                        nome = nome,
                                        email = email,
                                        senha = senha
                                    )
                                )

                            if (resposta.isSuccessful) {
                                cadastroErro = null

                                navController.popBackStack(
                                    Rotas.LOGIN,
                                    inclusive = false
                                )
                            } else {
                                cadastroErro = when (resposta.code()) {
                                    409 ->
                                        "Este e-mail já está cadastrado."

                                    422 ->
                                        "Os dados informados são inválidos."

                                    else ->
                                        "Não foi possível cadastrar. Código: ${resposta.code()}."
                                }
                            }
                        } catch (_: IOException) {
                            cadastroErro =
                                "Não foi possível conectar à API. Verifique se ela está em execução."
                        } catch (_: Exception) {
                            cadastroErro =
                                "Ocorreu um erro ao realizar o cadastro."
                        } finally {
                            carregandoCadastro = false
                        }
                    }
                }
            },
            erro = cadastroErro,
            carregando = carregandoCadastro
        )
    }
}
