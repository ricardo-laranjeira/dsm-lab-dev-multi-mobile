package com.example.orcamentoapp.screens.enderecos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.repositories.EnderecoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnderecosScreen(
    clienteId: Int,
    clienteNome: String,
    token: String = "",
    onVoltar: () -> Unit
) {
    val repository = remember { EnderecoRepository() }
    val viewModel = remember(clienteId) { EnderecosViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var formulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<EnderecoUi?>(null) }
    var excluir by remember { mutableStateOf<EnderecoUi?>(null) }

    LaunchedEffect(clienteId, token) {
        viewModel.carregar(clienteId)
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val filtrados = uiState.enderecos.filter { endereco ->
        endereco.cep.contains(pesquisa, ignoreCase = true) ||
                endereco.endereco.contains(pesquisa, ignoreCase = true) ||
                endereco.bairro.contains(pesquisa, ignoreCase = true) ||
                endereco.cidade.contains(pesquisa, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Endereços de $clienteNome") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editando = null
                    formulario = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo endereço")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pesquisar endereço") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            Text("${filtrados.size} endereço(s)")

            if (uiState.carregando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            uiState.erro?.let { erroMsg ->
                Text(
                    text = erroMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                TextButton(onClick = { viewModel.carregar(clienteId) }) {
                    Text("Tentar novamente")
                }
            }

            if (!uiState.carregando && uiState.erro == null && filtrados.isEmpty()) {
                Text(
                    "Nenhum endereço cadastrado para este cliente.",
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(filtrados, key = { it.id }) { endereco ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${endereco.endereco}, ${endereco.numero}",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    listOfNotNull(
                                        endereco.complemento.takeIf { it.isNotBlank() },
                                        endereco.bairro,
                                        "${endereco.cidade} - ${endereco.estado}"
                                    ).joinToString(" • ")
                                )
                                Text("CEP: ${endereco.cep}")
                            }

                            IconButton(
                                onClick = {
                                    editando = endereco
                                    formulario = true
                                },
                                enabled = !uiState.salvando
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            IconButton(
                                onClick = { excluir = endereco },
                                enabled = !uiState.salvando
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (formulario) {
        FormularioEnderecoApi(
            inicial = editando,
            salvando = uiState.salvando,
            erroApi = uiState.erro,
            onCancelar = { formulario = false },
            onSalvar = { cep, rua, numero, complemento, bairro, cidade, estado ->
                viewModel.salvar(
                    clienteId,
                    editando?.id,
                    cep,
                    rua,
                    numero,
                    complemento,
                    bairro,
                    cidade,
                    estado
                ) {
                    formulario = false
                    editando = null
                }
            }
        )
    }

    excluir?.let { endereco ->
        AlertDialog(
            onDismissRequest = { excluir = null },
            title = { Text("Excluir endereço") },
            text = {
                Text("Deseja excluir ${endereco.endereco}, ${endereco.numero}?")
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluir(clienteId, endereco.id) {
                            excluir = null
                        }
                    }
                ) {
                    Text(if (uiState.salvando) "Excluindo..." else "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { excluir = null },
                    enabled = !uiState.salvando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioEnderecoApi(
    inicial: EnderecoUi?,
    salvando: Boolean,
    erroApi: String? = null,
    onCancelar: () -> Unit,
    onSalvar: (
        String, String, String, String, String, String, String
    ) -> Unit
) {
    var cep by remember(inicial) { mutableStateOf(inicial?.cep.orEmpty()) }
    var rua by remember(inicial) { mutableStateOf(inicial?.endereco.orEmpty()) }
    var numero by remember(inicial) { mutableStateOf(inicial?.numero.orEmpty()) }
    var complemento by remember(inicial) {
        mutableStateOf(inicial?.complemento.orEmpty())
    }
    var bairro by remember(inicial) { mutableStateOf(inicial?.bairro.orEmpty()) }
    var cidade by remember(inicial) { mutableStateOf(inicial?.cidade.orEmpty()) }
    var estado by remember(inicial) { mutableStateOf(inicial?.estado.orEmpty()) }
    var erroFormulario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (inicial == null) "Novo endereço" else "Editar endereço")
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CampoEndereco("CEP", cep) { cep = it }
                }
                item {
                    CampoEndereco("Endereço / Rua", rua) { rua = it }
                }
                item {
                    CampoEndereco("Número", numero) { numero = it }
                }
                item {
                    CampoEndereco("Complemento (opcional)", complemento) { complemento = it }
                }
                item {
                    CampoEndereco("Bairro", bairro) { bairro = it }
                }
                item {
                    CampoEndereco("Cidade", cidade) { cidade = it }
                }
                item {
                    CampoEndereco("Estado", estado) { estado = it }
                }
                val mensagemErroVisivel = erroFormulario.ifBlank { erroApi.orEmpty() }
                if (mensagemErroVisivel.isNotBlank()) {
                    item {
                        Text(
                            mensagemErroVisivel,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando,
                onClick = {
                    erroFormulario = when {
                        cep.isBlank() -> "Informe o CEP."
                        cep.trim().length > 10 -> "CEP deve ter até 10 caracteres."
                        rua.isBlank() -> "Informe o endereço."
                        rua.trim().length > 100 -> "Endereço deve ter até 100 caracteres."
                        numero.isBlank() -> "Informe o número."
                        numero.trim().length > 5 -> "Número deve ter até 5 caracteres."
                        bairro.isBlank() -> "Informe o bairro."
                        bairro.trim().length > 100 -> "Bairro deve ter até 100 caracteres."
                        cidade.isBlank() -> "Informe a cidade."
                        cidade.trim().length > 45 -> "Cidade deve ter até 45 caracteres."
                        estado.isBlank() -> "Informe o estado."
                        estado.trim().length > 45 -> "Estado deve ter até 45 caracteres."
                        complemento.trim().length > 100 ->
                            "Complemento deve ter até 100 caracteres."
                        else -> ""
                    }

                    if (erroFormulario.isBlank()) {
                        onSalvar(
                            cep.trim(),
                            rua.trim(),
                            numero.trim(),
                            complemento.trim(),
                            bairro.trim(),
                            cidade.trim(),
                            estado.trim()
                        )
                    }
                }
            ) {
                Text(if (salvando) "Salvando..." else "Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !salvando) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun CampoEndereco(
    rotulo: String,
    valor: String,
    aoAlterar: (String) -> Unit
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoAlterar,
        label = { Text(rotulo) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
