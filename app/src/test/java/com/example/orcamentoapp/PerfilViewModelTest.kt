package com.example.orcamentoapp

import com.example.orcamentoapp.data.models.UsuarioResponse
import com.example.orcamentoapp.data.repositories.PerfilRepository
import com.example.orcamentoapp.screens.perfil.PerfilViewModel
import com.example.orcamentoapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerfilViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepository: FakePerfilRepository
    private lateinit var viewModel: PerfilViewModel

    @Before
    fun setup() {
        fakeRepository = FakePerfilRepository()
        viewModel = PerfilViewModel(fakeRepository)
    }

    @Test
    fun carregar_sucesso_atualizaStateComPerfil() = runTest {
        fakeRepository.usuarioPerfil = UsuarioResponse(
            idusuarios = 1,
            nome = "Maria Oliveira",
            email = "maria@exemplo.com",
            ativo = true,
            data_cadastro = "2023-01-01",
            data_atualizacao = "2023-01-02"
        )

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertNull(state.erro)
        assertEquals("Maria Oliveira", state.nome)
        assertEquals("maria@exemplo.com", state.email)
    }

    @Test
    fun carregar_falhaRepositorio_defineErroEmState() = runTest {
        fakeRepository.shouldThrowErrorOnObter = true
        fakeRepository.errorMessage = "Erro ao carregar dados do usuário"

        viewModel.carregar()

        val state = viewModel.uiState.value
        assertFalse(state.carregando)
        assertEquals("Erro ao carregar dados do usuário", state.erro)
    }

    @Test
    fun salvar_somentePerfil_sucesso_atualizaStateEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            nome = "Maria Oliveira Silva",
            email = "maria.silva@exemplo.com",
            senhaAtual = "",
            novaSenha = ""
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Perfil atualizado com sucesso.", state.mensagem)
        assertEquals("Maria Oliveira Silva", state.nome)
        assertEquals("maria.silva@exemplo.com", state.email)
        assertTrue(callbackExecutado)
        assertFalse(fakeRepository.senhaAtualizada)
    }

    @Test
    fun salvar_perfilESenha_sucesso_atualizaStateEMensagemEChamaCallback() = runTest {
        var callbackExecutado = false

        viewModel.salvar(
            nome = "Maria Oliveira Silva",
            email = "maria.silva@exemplo.com",
            senhaAtual = "senha123",
            novaSenha = "novaSenha456"
        ) {
            callbackExecutado = true
        }

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertNull(state.erro)
        assertEquals("Perfil e senha atualizados com sucesso.", state.mensagem)
        assertEquals("Maria Oliveira Silva", state.nome)
        assertEquals("maria.silva@exemplo.com", state.email)
        assertTrue(callbackExecutado)
        assertTrue(fakeRepository.senhaAtualizada)
    }

    @Test
    fun salvar_falhaAoAtualizarPerfil_defineErroEmState() = runTest {
        fakeRepository.shouldThrowErrorOnAtualizarPerfil = true
        fakeRepository.errorMessage = "Email já em uso por outro usuário"

        viewModel.salvar("Nome", "email@exemplo.com", "", "")

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Email já em uso por outro usuário", state.erro)
    }

    @Test
    fun salvar_falhaAoAtualizarSenha_defineErroEmState() = runTest {
        fakeRepository.shouldThrowErrorOnAtualizarSenha = true
        fakeRepository.errorMessage = "Senha atual incorreta"

        viewModel.salvar("Nome", "email@exemplo.com", "senhaIncorreta", "novaSenha")

        val state = viewModel.uiState.value
        assertFalse(state.salvando)
        assertEquals("Senha atual incorreta", state.erro)
    }

    @Test
    fun limparMensagem_removeMensagemDoState() = runTest {
        viewModel.salvar("Nome", "email@exemplo.com", "", "")
        assertEquals("Perfil atualizado com sucesso.", viewModel.uiState.value.mensagem)

        viewModel.limparMensagem()

        assertNull(viewModel.uiState.value.mensagem)
    }

    private class FakePerfilRepository : PerfilRepository() {
        var usuarioPerfil = UsuarioResponse(1, "Maria", "maria@exemplo.com", true, "2023-01-01", "2023-01-01")
        var shouldThrowErrorOnObter = false
        var shouldThrowErrorOnAtualizarPerfil = false
        var shouldThrowErrorOnAtualizarSenha = false
        var errorMessage = "Erro no repositório de perfil"
        var senhaAtualizada = false

        override suspend fun obterPerfil(): UsuarioResponse {
            if (shouldThrowErrorOnObter) throw Exception(errorMessage)
            return usuarioPerfil
        }

        override suspend fun atualizarPerfil(
            nome: String,
            email: String
        ): UsuarioResponse {
            if (shouldThrowErrorOnAtualizarPerfil) throw Exception(errorMessage)
            usuarioPerfil = usuarioPerfil.copy(nome = nome, email = email)
            return usuarioPerfil
        }

        override suspend fun atualizarSenha(
            senhaAtual: String,
            novaSenha: String
        ) {
            if (shouldThrowErrorOnAtualizarSenha) throw Exception(errorMessage)
            senhaAtualizada = true
        }
    }
}
