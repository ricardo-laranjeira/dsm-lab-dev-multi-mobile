package com.example.orcamentoapp.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.repositories.PerfilRepository

@Composable
fun PerfilScreen(
    onVoltar: () -> Unit,
    nomeInicial: String = "",
    emailInicial: String = "",
    carregando: Boolean = false,
    mensagem: String? = null,
    erro: Boolean = false,
    onSalvar: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    val repository = remember { PerfilRepository() }
    val viewModel = remember { PerfilViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()

    var nome by remember(uiState.nome) { mutableStateOf(uiState.nome.ifEmpty { nomeInicial }) }
    var email by remember(uiState.email) { mutableStateOf(uiState.email.ifEmpty { emailInicial }) }
    var senhaAtual by remember { mutableStateOf("") }
    var novaSenha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var mensagemValidacao by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Meu perfil",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Gerencie seus dados pessoais e sua senha.")

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Dados pessoais",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = nome,
            onValueChange = {
                nome = it
                mensagemValidacao = null
            },
            label = { Text("Nome") },
            singleLine = true,
            enabled = !uiState.carregando && !uiState.salvando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                mensagemValidacao = null
            },
            label = { Text("E-mail") },
            singleLine = true,
            enabled = !uiState.carregando && !uiState.salvando,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Segurança",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = senhaAtual,
            onValueChange = {
                senhaAtual = it
                mensagemValidacao = null
            },
            label = { Text("Senha atual") },
            singleLine = true,
            enabled = !uiState.carregando && !uiState.salvando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = novaSenha,
            onValueChange = {
                novaSenha = it
                mensagemValidacao = null
            },
            label = { Text("Nova senha") },
            singleLine = true,
            enabled = !uiState.carregando && !uiState.salvando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmarSenha,
            onValueChange = {
                confirmarSenha = it
                mensagemValidacao = null
            },
            label = { Text("Confirmar nova senha") },
            singleLine = true,
            enabled = !uiState.carregando && !uiState.salvando,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        val textoMensagem = mensagemValidacao ?: uiState.erro ?: uiState.mensagem ?: mensagem
        if (!textoMensagem.isNullOrBlank()) {
            Text(
                text = textoMensagem,
                color = if (mensagemValidacao != null || uiState.erro != null || erro) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val alterandoSenha =
                    senhaAtual.isNotBlank() ||
                            novaSenha.isNotBlank() ||
                            confirmarSenha.isNotBlank()

                when {
                    nome.isBlank() || email.isBlank() ->
                        mensagemValidacao = "Preencha nome e e-mail."

                    alterandoSenha && (
                            senhaAtual.isBlank() ||
                                    novaSenha.isBlank() ||
                                    confirmarSenha.isBlank()
                            ) ->
                        mensagemValidacao = "Preencha todos os campos de senha."

                    alterandoSenha && novaSenha != confirmarSenha ->
                        mensagemValidacao = "A confirmação da senha não confere."

                    alterandoSenha && novaSenha.length < 8 ->
                        mensagemValidacao = "A nova senha deve ter pelo menos 8 caracteres."

                    else -> {
                        mensagemValidacao = null
                        val pwdNew = if (alterandoSenha) novaSenha else ""
                        viewModel.salvar(nome.trim(), email.trim(), senhaAtual, pwdNew) {
                            senhaAtual = ""
                            novaSenha = ""
                            confirmarSenha = ""
                        }
                        onSalvar(nome.trim(), email.trim(), senhaAtual, pwdNew)
                    }
                }
            },
            enabled = !uiState.carregando && !uiState.salvando,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.carregando || uiState.salvando) {
                CircularProgressIndicator()
            } else {
                Text("Salvar alterações")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onVoltar,
            enabled = !uiState.carregando && !uiState.salvando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}
