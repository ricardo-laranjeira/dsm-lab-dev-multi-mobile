package com.example.orcamentoapp.screens.clientes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import com.example.orcamentoapp.data.repositories.ClienteRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    onVoltar: () -> Unit,
    onGerenciarEnderecos: (Int, String) -> Unit,
    token: String = ""
) {
    val repository = remember { ClienteRepository() }
    val viewModel = remember { ClientesViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var clienteEditando by remember { mutableStateOf<ClienteUi?>(null) }
    var clienteExcluir by remember { mutableStateOf<ClienteUi?>(null) }

    LaunchedEffect(token) {
        viewModel.carregar()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val clientesFiltrados = uiState.clientes.filter {
        it.nome.contains(pesquisa, ignoreCase = true) ||
                it.telefone.contains(pesquisa, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clientes") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    clienteEditando = null
                    mostrarFormulario = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Novo cliente"
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = pesquisa,
                onValueChange = { pesquisa = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Pesquisar cliente") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.padding(top = 8.dp))

            Text(
                text = "${clientesFiltrados.size} cliente(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.erro?.let { erroMsg ->
                Text(
                    text = erroMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextButton(onClick = { viewModel.carregar() }) {
                    Text("Tentar novamente")
                }
            }

            if (uiState.carregando) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else if (clientesFiltrados.isEmpty()) {
                Text(
                    text = "Nenhum cliente encontrado.",
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(clientesFiltrados, key = { it.id }) { cliente ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = cliente.nome,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = cliente.telefone,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = if (cliente.ativo) "Ativo" else "Inativo",
                                            color = if (cliente.ativo) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.error
                                            },
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            clienteEditando = cliente
                                            mostrarFormulario = true
                                        },
                                        enabled = !uiState.salvando
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar cliente"
                                        )
                                    }

                                    IconButton(
                                        onClick = { clienteExcluir = cliente },
                                        enabled = !uiState.salvando
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Excluir cliente",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                OutlinedButton(
                                    onClick = {
                                        onGerenciarEnderecos(
                                            cliente.id,
                                            cliente.nome
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Gerenciar endereços")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioCliente(
            cliente = clienteEditando,
            salvando = uiState.salvando,
            erroApi = uiState.erro,
            onCancelar = { mostrarFormulario = false },
            onGerenciarEnderecos = { id, nome ->
                mostrarFormulario = false
                onGerenciarEnderecos(id, nome)
            },
            onSalvar = { nome, telefone, ativo, irParaEnderecos ->
                viewModel.salvar(clienteEditando?.id, nome, telefone, ativo) { clienteSalvo ->
                    mostrarFormulario = false
                    clienteEditando = null
                    if (irParaEnderecos) {
                        onGerenciarEnderecos(clienteSalvo.id, clienteSalvo.nome)
                    }
                }
            }
        )
    }

    clienteExcluir?.let { cliente ->
        AlertDialog(
            onDismissRequest = { clienteExcluir = null },
            title = { Text("Excluir cliente") },
            text = { Text("Deseja excluir ${cliente.nome}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.excluir(cliente.id) {
                            clienteExcluir = null
                        }
                    },
                    enabled = !uiState.salvando
                ) {
                    Text(if (uiState.salvando) "Excluindo..." else "Excluir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { clienteExcluir = null },
                    enabled = !uiState.salvando
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioCliente(
    cliente: ClienteUi?,
    salvando: Boolean,
    erroApi: String? = null,
    onCancelar: () -> Unit,
    onSalvar: (nome: String, telefone: String, ativo: Boolean, irParaEnderecos: Boolean) -> Unit,
    onGerenciarEnderecos: (Int, String) -> Unit
) {
    var nome by remember(cliente) {
        mutableStateOf(cliente?.nome.orEmpty())
    }
    var telefone by remember(cliente) {
        mutableStateOf(cliente?.telefone.orEmpty())
    }
    var ativo by remember(cliente) {
        mutableStateOf(cliente?.ativo ?: true)
    }
    var erroFormulario by remember(cliente) { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (cliente == null) "Novo cliente" else "Editar cliente")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it; erroFormulario = "" },
                    label = { Text("Nome") },
                    singleLine = true,
                    enabled = !salvando,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it; erroFormulario = "" },
                    label = { Text("Telefone") },
                    singleLine = true,
                    enabled = !salvando,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (ativo) "Cliente ativo" else "Cliente inativo")
                    Switch(
                        checked = ativo,
                        onCheckedChange = { ativo = it },
                        enabled = !salvando
                    )
                }

                if (cliente != null) {
                    OutlinedButton(
                        onClick = {
                            onGerenciarEnderecos(cliente.id, cliente.nome)
                        },
                        enabled = !salvando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Gerenciar endereços")
                    }
                }

                val mensagemErro = erroFormulario.ifBlank { erroApi.orEmpty() }
                if (mensagemErro.isNotBlank()) {
                    Text(
                        text = mensagemErro,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        when {
                            nome.isBlank() ->
                                erroFormulario = "Informe o nome do cliente."
                            telefone.isBlank() ->
                                erroFormulario = "Informe o telefone."
                            nome.trim().length > 45 ->
                                erroFormulario = "O nome deve ter até 45 caracteres."
                            telefone.trim().length > 16 ->
                                erroFormulario = "O telefone deve ter até 16 caracteres."
                            else -> onSalvar(nome.trim(), telefone.trim(), ativo, false)
                        }
                    },
                    enabled = !salvando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (salvando) "Salvando..." else "Salvar")
                }

                if (cliente == null) {
                    OutlinedButton(
                        onClick = {
                            when {
                                nome.isBlank() ->
                                    erroFormulario = "Informe o nome do cliente."
                                telefone.isBlank() ->
                                    erroFormulario = "Informe o telefone."
                                nome.trim().length > 45 ->
                                    erroFormulario = "O nome deve ter até 45 caracteres."
                                telefone.trim().length > 16 ->
                                    erroFormulario = "O telefone deve ter até 16 caracteres."
                                else -> onSalvar(nome.trim(), telefone.trim(), ativo, true)
                            }
                        },
                        enabled = !salvando,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Salvar e Adicionar Endereço")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                enabled = !salvando
            ) {
                Text("Cancelar")
            }
        }
    )
}
