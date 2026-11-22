package com.example.orcamentoapp.screens.orcamentos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
internal fun FormularioOrcamento(
    inicial: OrcamentoUi?,
    clientesDisponiveis: List<ClienteOrcamentoUi>,
    materiaisDisponiveis: List<ProdutoOrcamentoUi>,
    servicosDisponiveis: List<ProdutoOrcamentoUi>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (OrcamentoUi) -> Unit
) {
    var clienteId by remember(inicial) {
        mutableIntStateOf(inicial?.clienteId ?: 0)
    }
    var validade by remember(inicial) {
        mutableStateOf(inicial?.validade ?: LocalDate.now().plusDays(15).toString())
    }
    var status by remember(inicial) {
        mutableStateOf(inicial?.status ?: "Não aprovado")
    }
    val itens = remember(inicial) {
        mutableStateListOf<ItemOrcamentoUi>().apply {
            addAll(inicial?.itens.orEmpty())
        }
    }

    var tipoItem by remember { mutableStateOf("Material") }
    var produtoId by remember(tipoItem) { mutableIntStateOf(0) }
    var quantidadeTexto by remember { mutableStateOf("1") }
    var erro by remember { mutableStateOf("") }
    var filtroItens by remember { mutableStateOf("Tudo") }

    val cliente = clientesDisponiveis.firstOrNull { it.id == clienteId }
    val produtos = if (tipoItem == "Material") materiaisDisponiveis else servicosDisponiveis
    val produto = produtos.firstOrNull { it.id == produtoId }
    val quantidade = quantidadeTexto.toIntOrNull() ?: 0
    val total = itens.sumOf { it.subtotal }

    val itensVisiveis = when (filtroItens) {
        "Materiais" -> itens.filter { it.tipo == "Material" }
        "Serviços" -> itens.filter { it.tipo == "Serviço" }
        else -> itens.toList()
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (inicial == null) "Novo orçamento" else "Editar orçamento #${inicial.id}")
        },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val cliente = clientesDisponiveis.firstOrNull { it.id == clienteId }

                SeletorOrcamento(
                    label = "Cliente",
                    selecionado = cliente?.nome ?: "Selecione um cliente",
                    opcoes = clientesDisponiveis.map { it.nome },
                    aoSelecionar = { nome ->
                        clienteId = clientesDisponiveis
                            .firstOrNull { it.nome == nome }
                            ?.id ?: 0
                    },
                    habilitado = clientesDisponiveis.isNotEmpty()
                )

                OutlinedTextField(
                    value = validade,
                    onValueChange = { validade = it },
                    label = { Text("Validade (AAAA-MM-DD)") },
                    placeholder = { Text("2026-11-25") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                SeletorOrcamento(
                    label = "Status",
                    selecionado = status,
                    opcoes = statusOrcamento,
                    aoSelecionar = { status = it }
                )

                HorizontalDivider()
                Text("Adicionar itens", fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Material", "Serviço").forEach { tipo ->
                        val selecionado = tipoItem == tipo
                        if (selecionado) {
                            Button(
                                onClick = { tipoItem = tipo; produtoId = 0 },
                                modifier = Modifier.weight(1f)
                            ) { Text(if (tipo == "Material") "Materiais" else "Serviços") }
                        } else {
                            OutlinedButton(
                                onClick = { tipoItem = tipo; produtoId = 0 },
                                modifier = Modifier.weight(1f)
                            ) { Text(if (tipo == "Material") "Materiais" else "Serviços") }
                        }
                    }
                }

                SeletorOrcamento(
                    label = if (tipoItem == "Material") "Selecione um material" else "Selecione um serviço",
                    selecionado = produto?.nome ?: "Nenhum item selecionado",
                    opcoes = produtos.map { it.nome },
                    aoSelecionar = { nome ->
                        produtoId = produtos.first { it.nome == nome }.id
                    },
                    habilitado = produtos.isNotEmpty()
                )

                Text("Valor unitário: ${produto?.let { moeda(it.valor) } ?: "—"}")

                OutlinedTextField(
                    value = quantidadeTexto,
                    onValueChange = { quantidadeTexto = it },
                    label = { Text("Quantidade") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (produto == null) {
                            erro = "Selecione um item."
                        } else if (quantidade <= 0) {
                            erro = "Informe uma quantidade maior que zero."
                        } else if (itens.any {
                                it.tipo == tipoItem && it.referenciaId == produto.id
                            }
                        ) {
                            erro = "Este item já foi adicionado. Remova-o antes de adicioná-lo novamente."
                        } else {
                            itens.add(
                                ItemOrcamentoUi(
                                    id = (itens.maxOfOrNull { it.id } ?: 0) + 1,
                                    tipo = tipoItem,
                                    referenciaId = produto.id,
                                    descricao = produto.nome,
                                    quantidade = quantidade,
                                    valorUnitario = produto.valor
                                )
                            )
                            erro = ""
                            quantidadeTexto = "1"
                            produtoId = 0
                        }
                    },
                    enabled = produto != null && quantidade > 0,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Adicionar item")
                }

                if (erro.isNotBlank()) {
                    Text(erro, color = MaterialTheme.colorScheme.error)
                }

                HorizontalDivider()
                Text("Itens do orçamento", fontWeight = FontWeight.Bold)

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Tudo", "Materiais", "Serviços").forEach { filtro ->
                        if (filtroItens == filtro) {
                            Button(
                                onClick = { filtroItens = filtro },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text(filtro) }
                        } else {
                            OutlinedButton(
                                onClick = { filtroItens = filtro },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp)
                            ) { Text(filtro) }
                        }
                    }
                }

                Text("${itensVisiveis.size} item(ns) exibido(s) de ${itens.size}")

                itensVisiveis.toList().forEach { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.descricao, fontWeight = FontWeight.SemiBold)
                                Text(item.tipo)
                                Text("${item.quantidade} × ${moeda(item.valorUnitario)}")
                                Text("Subtotal: ${moeda(item.subtotal)}")
                            }
                            IconButton(onClick = {
                                itens.removeAll {
                                    it.tipo == item.tipo &&
                                            it.referenciaId == item.referenciaId
                                }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover item")
                            }
                        }
                    }
                }

                HorizontalDivider()
                Text(
                    "Total do orçamento: ${moeda(total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando,
                onClick = {
                    val dataValida = runCatching { LocalDate.parse(validade) }.isSuccess
                    erro = when {
                        cliente == null -> "Selecione um cliente."
                        !dataValida -> "Informe uma data válida no formato AAAA-MM-DD."
                        itens.isEmpty() -> "Adicione pelo menos um item ao orçamento."
                        else -> ""
                    }

                    if (erro.isBlank() && cliente != null) {
                        onSalvar(
                            OrcamentoUi(
                                id = inicial?.id ?: 0,
                                clienteId = cliente.id,
                                clienteNome = cliente.nome,
                                validade = validade,
                                status = status,
                                itens = itens.toList()
                            )
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
