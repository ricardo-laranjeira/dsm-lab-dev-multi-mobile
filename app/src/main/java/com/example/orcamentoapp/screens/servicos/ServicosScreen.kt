package com.example.orcamentoapp.screens.servicos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.ServicoResponse
import com.example.orcamentoapp.data.repositories.ServicoRepository
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicosScreen(onVoltar: () -> Unit) {
    val repository = remember { ServicoRepository() }
    val viewModel = remember { ServicosViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var formulario by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<ServicoResponse?>(null) }
    var excluir by remember { mutableStateOf<ServicoResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val filtrados = uiState.servicos.filter { servico ->
        val categoriaNome = uiState.categorias.find {
            it.idcategorias == servico.categoriasId
        }?.descricao.orEmpty()

        servico.descricao.contains(pesquisa, ignoreCase = true) ||
                categoriaNome.contains(pesquisa, ignoreCase = true) ||
                servico.categoriasId.toString().contains(pesquisa)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serviços") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editando = null
                    formulario = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Novo serviço"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                label = { Text("Pesquisar serviço") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Pesquisar"
                    )
                },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))
            Text("${filtrados.size} serviço(s)")

            uiState.erro?.let { erroMsg ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = erroMsg,
                    color = MaterialTheme.colorScheme.error
                )
                Button(
                    onClick = { viewModel.carregar() }
                ) {
                    Text("Tentar novamente")
                }
            }

            if (uiState.carregando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (filtrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (pesquisa.isBlank()) {
                            "Nenhum serviço cadastrado."
                        } else {
                            "Nenhum serviço encontrado."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    items(
                        items = filtrados,
                        key = { it.idservicos }
                    ) { servico ->
                        val categoria = uiState.categorias.find {
                            it.idcategorias == servico.categoriasId
                        }

                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        servico.descricao,
                                        fontWeight = FontWeight.SemiBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("Valor: ${formatarValor(servico.valor)}")
                                    Text(
                                        "Categoria: ${
                                            categoria?.descricao
                                                ?: "ID ${servico.categoriasId}"
                                        }",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        editando = servico
                                        formulario = true
                                    },
                                    enabled = !uiState.salvando
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar"
                                    )
                                }

                                IconButton(
                                    onClick = { excluir = servico },
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
    }

    if (formulario) {
        FormularioServicoApi(
            inicial = editando,
            categorias = uiState.categorias,
            salvando = uiState.salvando,
            onCancelar = {
                formulario = false
                editando = null
            },
            onSalvar = { descricao, valor, categoriaId ->
                viewModel.salvar(
                    editando?.idservicos,
                    descricao,
                    valor,
                    categoriaId
                ) {
                    formulario = false
                    editando = null
                }
            }
        )
    }

    excluir?.let { servico ->
        AlertDialog(
            onDismissRequest = {
                if (!uiState.salvando) excluir = null
            },
            title = { Text("Excluir serviço") },
            text = {
                Text("Deseja excluir ${servico.descricao}?")
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluir(servico.idservicos) {
                            excluir = null
                        }
                    }
                ) {
                    Text("Excluir")
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
private fun FormularioServicoApi(
    inicial: ServicoResponse?,
    categorias: List<CategoriaResponse>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (String, String, Int) -> Unit
) {
    var descricao by remember(inicial) {
        mutableStateOf(inicial?.descricao.orEmpty())
    }
    var valor by remember(inicial) {
        mutableStateOf(inicial?.valor.orEmpty())
    }
    var categoriaSelecionada by remember(inicial, categorias) {
        mutableStateOf(
            categorias.find {
                it.idcategorias == inicial?.categoriasId
            }
        )
    }
    var menuAberto by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = {
            if (!salvando) onCancelar()
        },
        title = {
            Text(if (inicial == null) "Novo serviço" else "Editar serviço")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !salvando
                )

                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it },
                    label = { Text("Valor (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    enabled = !salvando
                )

                Box {
                    OutlinedTextField(
                        value = categoriaSelecionada?.descricao.orEmpty(),
                        onValueChange = {},
                        label = { Text("Categoria") },
                        placeholder = { Text("Selecione uma categoria") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        enabled = !salvando
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !salvando) {
                                menuAberto = true
                            }
                    )

                    DropdownMenu(
                        expanded = menuAberto,
                        onDismissRequest = { menuAberto = false }
                    ) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.descricao) },
                                onClick = {
                                    categoriaSelecionada = categoria
                                    menuAberto = false
                                }
                            )
                        }
                    }
                }

                if (categorias.isEmpty()) {
                    Text(
                        "Nenhuma categoria disponível. Cadastre uma categoria primeiro.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (erro.isNotBlank()) {
                    Text(
                        erro,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando && categorias.isNotEmpty(),
                onClick = {
                    val preco = valor.trim()
                        .replace(",", ".")
                        .toBigDecimalOrNull()

                    erro = when {
                        descricao.isBlank() ->
                            "Informe a descrição."

                        descricao.trim().length > 150 ->
                            "A descrição deve ter até 150 caracteres."

                        preco == null ->
                            "Informe um valor válido."

                        preco < BigDecimal.ZERO ->
                            "O valor não pode ser negativo."

                        preco.scale().coerceAtLeast(0) > 2 ->
                            "O valor deve ter no máximo duas casas decimais."

                        preco.precision() > 10 ->
                            "O valor informado é muito grande."

                        categoriaSelecionada == null ->
                            "Selecione uma categoria."

                        else -> ""
                    }

                    if (erro.isBlank() && preco != null) {
                        onSalvar(
                            descricao.trim(),
                            preco.setScale(2).toPlainString(),
                            categoriaSelecionada!!.idcategorias
                        )
                    }
                }
            ) {
                if (salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !salvando,
                onClick = onCancelar
            ) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatarValor(valor: String): String {
    val decimal = valor.toBigDecimalOrNull() ?: BigDecimal.ZERO
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(decimal)
}
