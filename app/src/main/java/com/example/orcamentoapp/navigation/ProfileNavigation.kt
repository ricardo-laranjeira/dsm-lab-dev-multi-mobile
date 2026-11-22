package com.example.orcamentoapp.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.orcamentoapp.data.ApiClient
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.models.SenhaUpdateRequest
import com.example.orcamentoapp.data.models.UsuarioPerfilUpdateRequest
import com.example.orcamentoapp.screens.perfil.PerfilScreen
import kotlinx.coroutines.launch
import java.io.IOException

fun NavGraphBuilder.profileNavigation(
    navController: NavController,
    tokenStorage: TokenStorage
) {
    composable(Rotas.PERFIL) {
        val scope = rememberCoroutineScope()

        var perfilNome by remember { mutableStateOf("") }
        var perfilEmail by remember { mutableStateOf("") }
        var perfilMensagem by remember {
            mutableStateOf<String?>(null)
        }
        var perfilErro by remember { mutableStateOf(false) }
        var carregandoPerfil by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            carregandoPerfil = true
            perfilMensagem = null
            perfilErro = false

            try {
                val resposta = ApiClient.usuarioApi.obterMeuPerfil()

                if (resposta.isSuccessful) {
                    val usuario = resposta.body()

                    if (usuario != null) {
                        perfilNome = usuario.nome
                        perfilEmail = usuario.email
                    } else {
                        perfilMensagem =
                            "A API não retornou os dados do perfil."
                        perfilErro = true
                    }
                } else {
                    perfilMensagem =
                        "Não foi possível carregar o perfil. Código: ${resposta.code()}."
                    perfilErro = true
                }
            } catch (_: IOException) {
                perfilMensagem = "Não foi possível conectar à API."
                perfilErro = true
            } catch (_: Exception) {
                perfilMensagem = "Ocorreu um erro ao carregar o perfil."
                perfilErro = true
            } finally {
                carregandoPerfil = false
            }
        }

        PerfilScreen(
            onVoltar = {
                if (!carregandoPerfil) {
                    navController.popBackStack()
                }
            },
            nomeInicial = perfilNome,
            emailInicial = perfilEmail,
            carregando = carregandoPerfil,
            mensagem = perfilMensagem,
            erro = perfilErro,
            onSalvar = { nome, email, senhaAtual, novaSenha ->
                if (!carregandoPerfil) {
                    scope.launch {
                        carregandoPerfil = true
                        perfilMensagem = null
                        perfilErro = false

                        try {
                            val respostaPerfil =
                                ApiClient.usuarioApi.atualizarMeuPerfil(
                                    UsuarioPerfilUpdateRequest(
                                        nome = nome,
                                        email = email
                                    )
                                )

                            if (!respostaPerfil.isSuccessful) {
                                perfilMensagem =
                                    when (respostaPerfil.code()) {
                                        401 ->
                                            "Sessão expirada. Faça login novamente."

                                        409 ->
                                            "Este e-mail já está cadastrado."

                                        422 ->
                                            "Confira os dados informados."

                                        else ->
                                            "Não foi possível atualizar o perfil. Código: ${respostaPerfil.code()}."
                                    }
                                perfilErro = true
                            } else {
                                val usuario = respostaPerfil.body()

                                if (usuario != null) {
                                    perfilNome = usuario.nome
                                    perfilEmail = usuario.email
                                }

                                if (novaSenha.isNotBlank()) {
                                    val respostaSenha =
                                        ApiClient.usuarioApi.atualizarMinhaSenha(
                                            SenhaUpdateRequest(
                                                senhaAtual = senhaAtual,
                                                novaSenha = novaSenha
                                            )
                                        )

                                    if (respostaSenha.isSuccessful) {
                                        perfilMensagem =
                                            "Perfil e senha atualizados com sucesso."
                                    } else {
                                        perfilMensagem =
                                            when (respostaSenha.code()) {
                                                400 ->
                                                    "Perfil atualizado, mas a senha atual está incorreta ou a nova senha é inválida."

                                                401 ->
                                                    "Perfil atualizado, mas a sessão expirou."

                                                422 ->
                                                    "Perfil atualizado, mas a nova senha não foi aceita."

                                                else ->
                                                    "Perfil atualizado, mas houve um erro ao alterar a senha. Código: ${respostaSenha.code()}."
                                            }
                                        perfilErro = true
                                    }
                                } else {
                                    perfilMensagem =
                                        "Perfil atualizado com sucesso."
                                }
                            }
                        } catch (_: IOException) {
                            perfilMensagem =
                                "Não foi possível conectar à API."
                            perfilErro = true
                        } catch (_: Exception) {
                            perfilMensagem =
                                "Ocorreu um erro ao atualizar o perfil."
                            perfilErro = true
                        } finally {
                            carregandoPerfil = false
                        }
                    }
                }
            }
        )
    }
}
