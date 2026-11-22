package com.example.orcamentoapp.screens.agendamentos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.repositories.AgendamentoRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private data class OrcamentoAgendamentoUi(
    val id: Int,
    val clienteNome: String,
    val descricao: String
)

private data class AgendamentoUi(
    val id: Int,
    val data: String,
    val hora: String,
    val orcamentoId: Int,
    val clienteNome: String,
    val descricaoOrcamento: String
)

private fun tokenAutorizacaoAgendamento(token: String?): String {
    val valor = token?.trim().orEmpty()
    return when {
        valor.isBlank() -> ""
        valor.startsWith("Bearer ", ignoreCase = true) -> valor
        else -> "Bearer $valor"
    }
}

private fun formatarData(data: String): String {
    return runCatching {
        LocalDate.parse(data.take(10)).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }.getOrDefault(data)
}

private fun formatarHora(hora: String): String {
    val partes = hora.trim().split(":")
    if (partes.size >= 2) {
        val h = partes[0].padStart(2, '0')
        val m = partes[1].padStart(2, '0')
        return "$h:$m"
    }
    return hora
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendamentosScreen(
    onVoltar: () -> Unit
) {
    val repository = remember { AgendamentoRepository() }
    val viewModel = remember { AgendamentosViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<AgendamentoUi?>(null) }
    var excluir by remember { mutableStateOf<AgendamentoUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val clientesPorId = remember(uiState.clientes) { uiState.clientes.associateBy { it.idclientes } }
    val orcamentosPorId = remember(uiState.orcamentos) { uiState.orcamentos.associateBy { it.idorcamentos } }

    val orcamentosDisponiveis = remember(uiState.orcamentos, uiState.clientes) {
        uiState.orcamentos.map { orcamento ->
            val cliente = clientesPorId[orcamento.clientesId]
            OrcamentoAgendamentoUi(
                id = orcamento.idorcamentos,
                clienteNome = cliente?.nome ?: "Cliente #${orcamento.clientesId}",
                descricao = "Orçamento #${orcamento.idorcamentos}"
            )
        }
    }

    val agendamentosUi = remember(uiState.agendamentos, uiState.orcamentos, uiState.clientes) {
        uiState.agendamentos.map { item ->
            val orcamento = orcamentosPorId[item.orcamentosId]
            val cliente = orcamento?.let { clientesPorId[it.clientesId] }
            AgendamentoUi(
                id = item.idagendamentos,
                data = item.data.take(10),
                hora = formatarHora(item.hora),
                orcamentoId = item.orcamentosId,
                clienteNome = cliente?.nome ?: "Cliente do orçamento #${item.orcamentosId}",
                descricaoOrcamento = "Orçamento #${item.orcamentosId}"
            )
        }
    }

    val listaFiltrada = agendamentosUi.filter { agendamento ->
        agendamento.clienteNome.contains(pesquisa, ignoreCase = true) ||
                agendamento.data.contains(pesquisa, ignoreCase = true) ||
                agendamento.hora.contains(pesquisa, ignoreCase = true) ||
                agendamento.orcamentoId.toString() == pesquisa.trim() ||
                agendamento.descricaoOrcamento.contains(pesquisa, ignoreCase = true)
    }.sortedWith(compareBy<AgendamentoUi> { it.data }.thenBy { it.hora })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agendamentos") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!uiState.carregando && !uiState.salvando) {
                        editando = null
                        mostrarFormulario = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo agendamento"
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
                label = { Text("Pesquisar agendamento") },
                placeholder = { Text("Cliente, data ou orçamento") },
                singleLine = true,
                enabled = !uiState.salvando,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.carregando || uiState.salvando) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            uiState.erro?.let { erroMsg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = erroMsg, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.carregar() }) {
                    Text("Tentar novamente")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("${listaFiltrada.size} agendamento(s)")
            Spacer(modifier = Modifier.height(8.dp))

            if (listaFiltrada.isEmpty() && !uiState.carregando) {
                Text("Nenhum agendamento encontrado.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items = listaFiltrada, key = { it.id }) { agendamento ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = agendamento.clienteNome,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${agendamento.descricaoOrcamento} • ID #${agendamento.id}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }

                                    IconButton(
                                        enabled = !uiState.salvando,
                                        onClick = {
                                            editando = agendamento
                                            mostrarFormulario = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar agendamento"
                                        )
                                    }

                                    IconButton(
                                        enabled = !uiState.salvando,
                                        onClick = { excluir = agendamento }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir agendamento",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("Data: ${formatarData(agendamento.data)}")
                                    Text("Horário: ${agendamento.hora}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioAgendamentoApi(
            inicial = editando,
            orcamentos = orcamentosDisponiveis,
            salvando = uiState.salvando,
            onCancelar = {
                mostrarFormulario = false
                editando = null
            },
            onSalvar = { data, hora, orcamentoId ->
                viewModel.salvar(editando?.id, data, hora, orcamentoId) {
                    mostrarFormulario = false
                    editando = null
                }
            }
        )
    }

    excluir?.let { agendamento ->
        AlertDialog(
            onDismissRequest = {
                if (!uiState.salvando) excluir = null
            },
            title = { Text("Excluir agendamento") },
            text = { Text("Deseja realmente excluir o agendamento de ${agendamento.clienteNome}?") },
            confirmButton = {
                Button(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluir(agendamento.id) {
                            excluir = null
                        }
                    }
                ) {
                    Text(if (uiState.salvando) "Excluindo..." else "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = { excluir = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioAgendamentoApi(
    inicial: AgendamentoUi?,
    orcamentos: List<OrcamentoAgendamentoUi>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (data: String, hora: String, orcamentoId: Int) -> Unit
) {
    var data by remember(inicial) {
        mutableStateOf(inicial?.data.orEmpty())
    }
    var hora by remember(inicial) {
        mutableStateOf(inicial?.hora.orEmpty())
    }
    var orcamentoSelecionado by remember(inicial, orcamentos) {
        mutableStateOf(
            orcamentos.find { it.id == inicial?.orcamentoId }
                ?: orcamentos.firstOrNull()
        )
    }

    var menuOrcamentoAberto by remember { mutableStateOf(false) }
    var erroFormulario by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!salvando) onCancelar() },
        title = {
            Text(if (inicial == null) "Novo agendamento" else "Editar agendamento")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Data (AAAA-MM-DD)") },
                    placeholder = { Text("Ex: 2026-03-30") },
                    singleLine = true,
                    enabled = !salvando
                )

                OutlinedTextField(
                    value = hora,
                    onValueChange = { hora = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Horário (HH:MM)") },
                    placeholder = { Text("Ex: 14:30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !salvando
                )

                Column {
                    OutlinedButton(
                        onClick = { menuOrcamentoAberto = true },
                        enabled = !salvando && orcamentos.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Orçamento", style = MaterialTheme.typography.labelSmall)
                            Text(
                                orcamentoSelecionado?.let {
                                    "${it.descricao} - ${it.clienteNome}"
                                } ?: "Selecione um orçamento",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Text("▼")
                    }

                    DropdownMenu(
                        expanded = menuOrcamentoAberto,
                        onDismissRequest = { menuOrcamentoAberto = false }
                    ) {
                        orcamentos.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.descricao} - ${item.clienteNome}") },
                                onClick = {
                                    orcamentoSelecionado = item
                                    menuOrcamentoAberto = false
                                }
                            )
                        }
                    }
                }

                if (orcamentos.isEmpty()) {
                    Text(
                        "Nenhum orçamento disponível para agendamento.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                erroFormulario?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando && orcamentos.isNotEmpty(),
                onClick = {
                    val dataTrim = data.trim()
                    val horaTrim = hora.trim()

                    val dataValida = runCatching { LocalDate.parse(dataTrim) }.isSuccess
                    val horaValida = runCatching { LocalTime.parse(horaTrim) }.isSuccess

                    erroFormulario = when {
                        dataTrim.isBlank() -> "Informe a data do agendamento."
                        !dataValida -> "Informe a data no formato AAAA-MM-DD (ex: 2026-03-30)."
                        horaTrim.isBlank() -> "Informe o horário do agendamento."
                        !horaValida -> "Informe o horário no formato HH:MM (ex: 14:30)."
                        orcamentoSelecionado == null -> "Selecione um orçamento."
                        else -> null
                    }

                    if (erroFormulario == null) {
                        onSalvar(
                            dataTrim,
                            "$horaTrim:00",
                            orcamentoSelecionado!!.id
                        )
                    }
                }
            ) {
                if (salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !salvando, onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}
