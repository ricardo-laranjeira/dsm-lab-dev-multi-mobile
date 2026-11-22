package com.example.orcamentoapp.screens.orcamentos

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import com.example.orcamentoapp.util.pdf.OrcamentoPdfExporter
import com.example.orcamentoapp.util.pdf.PdfShareHelper
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.repositories.OrcamentoRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrcamentosScreen(
    onVoltar: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { OrcamentoRepository() }
    val viewModel = remember { OrcamentosViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<OrcamentoUi?>(null) }
    var excluir by remember { mutableStateOf<OrcamentoUi?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregarDados()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val listaFiltrada = uiState.orcamentos.filter {
        it.clienteNome.contains(pesquisa, ignoreCase = true) ||
                it.status.contains(pesquisa, ignoreCase = true) ||
                it.id.toString() == pesquisa.trim()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orçamentos") },
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
                    if (!uiState.carregando && !uiState.salvando) {
                        editando = null
                        mostrarFormulario = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Novo orçamento"
                )
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
                label = { Text("Pesquisar orçamento ou cliente") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.carregando || uiState.salvando) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            uiState.erro?.let { mensagem ->
                Text(
                    text = mensagem,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
                TextButton(
                    onClick = { viewModel.carregarDados() }
                ) {
                    Text("Tentar novamente")
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("${listaFiltrada.size} orçamento(s)")
            Spacer(Modifier.height(8.dp))

            if (listaFiltrada.isEmpty() && !uiState.carregando) {
                Text("Nenhum orçamento encontrado.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { orcamento ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            "Orçamento #${orcamento.id}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(orcamento.clienteNome)
                                    }

                                    IconButton(
                                        enabled = !uiState.salvando,
                                        onClick = {
                                            try {
                                                val pdfFile = OrcamentoPdfExporter().exportOrcamento(context, orcamento)
                                                PdfShareHelper.sharePdf(context, pdfFile, title = "Orçamento #${orcamento.id}")
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Compartilhar PDF")
                                    }

                                    IconButton(
                                        enabled = !uiState.salvando,
                                        onClick = {
                                            editando = orcamento
                                            mostrarFormulario = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                                    }

                                    IconButton(
                                        enabled = !uiState.salvando,
                                        onClick = { excluir = orcamento }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir")
                                    }
                                }

                                Text("Validade: ${orcamento.validade}")
                                Text("Status: ${orcamento.status}")
                                Text("Itens: ${orcamento.itens.size}")
                                Text(
                                    "Total: ${moeda(orcamento.total)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioOrcamento(
            inicial = editando,
            clientesDisponiveis = uiState.clientes,
            materiaisDisponiveis = uiState.materiais,
            servicosDisponiveis = uiState.servicos,
            salvando = uiState.salvando,
            onCancelar = { mostrarFormulario = false },
            onSalvar = { novo ->
                viewModel.salvarOrcamento(editando, novo) {
                    mostrarFormulario = false
                    editando = null
                }
            }
        )
    }

    excluir?.let { orcamento ->
        AlertDialog(
            onDismissRequest = { excluir = null },
            title = { Text("Excluir orçamento") },
            text = { Text("Deseja excluir o orçamento #${orcamento.id}?") },
            confirmButton = {
                Button(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluirOrcamento(orcamento.id) {
                            excluir = null
                        }
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { excluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
