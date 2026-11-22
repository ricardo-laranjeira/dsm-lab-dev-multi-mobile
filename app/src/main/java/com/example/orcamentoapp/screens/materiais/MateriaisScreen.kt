package com.example.orcamentoapp.screens.materiais

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.orcamentoapp.data.TokenStorage
import com.example.orcamentoapp.data.models.CategoriaResponse
import com.example.orcamentoapp.data.models.MaterialResponse
import com.example.orcamentoapp.data.repositories.MaterialRepository
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MateriaisScreen(
    onVoltar: () -> Unit
) {
    val repository = remember { MaterialRepository() }
    val viewModel = remember { MateriaisViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var materialEditando by remember { mutableStateOf<MaterialResponse?>(null) }
    var materialExcluir by remember { mutableStateOf<MaterialResponse?>(null) }

    LaunchedEffect(Unit) {
        viewModel.carregar()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val materiaisFiltrados = uiState.materiais.filter { material ->
        val nomeCategoria = uiState.categorias
            .find { it.idcategorias == material.categoriasId }
            ?.descricao.orEmpty()

        material.descricao.contains(pesquisa, ignoreCase = true) ||
                nomeCategoria.contains(pesquisa, ignoreCase = true) ||
                material.categoriasId.toString().contains(pesquisa)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Materiais") },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    materialEditando = null
                    mostrarFormulario = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar material"
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
                label = { Text("Pesquisar material") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Pesquisar"
                    )
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.size(12.dp))

            uiState.erro?.let { erroMsg ->
                Text(
                    text = erroMsg,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.size(8.dp))

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
            } else if (materiaisFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (pesquisa.isBlank()) {
                            "Nenhum material cadastrado."
                        } else {
                            "Nenhum material encontrado."
                        }
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = materiaisFiltrados,
                        key = { it.idmateriais }
                    ) { material ->
                        val categoria = uiState.categorias.find {
                            it.idcategorias == material.categoriasId
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = material.descricao,
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.size(4.dp))

                                Text(
                                    text = "${formatarMoeda(material.valor)} / " +
                                            material.unidadeMedida
                                )

                                Text(
                                    text = "Categoria: ${
                                        categoria?.descricao
                                            ?: "ID ${material.categoriasId}"
                                    }"
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            materialEditando = material
                                            mostrarFormulario = true
                                        },
                                        enabled = !uiState.salvando
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar material"
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            materialExcluir = material
                                        },
                                        enabled = !uiState.salvando
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir material"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        FormularioMaterialApi(
            inicial = materialEditando,
            categorias = uiState.categorias,
            salvando = uiState.salvando,
            onCancelar = {
                mostrarFormulario = false
                materialEditando = null
            },
            onSalvar = { descricao, valor, unidade, categoriaId ->
                viewModel.salvar(
                    materialEditando?.idmateriais,
                    descricao,
                    valor,
                    unidade,
                    categoriaId
                ) {
                    mostrarFormulario = false
                    materialEditando = null
                }
            }
        )
    }

    materialExcluir?.let { material ->
        AlertDialog(
            onDismissRequest = {
                if (!uiState.salvando) materialExcluir = null
            },
            title = { Text("Excluir material") },
            text = {
                Text("Deseja realmente excluir \"${material.descricao}\"?")
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluir(material.idmateriais) {
                            materialExcluir = null
                        }
                    }
                ) {
                    Text(if (uiState.salvando) "Excluindo..." else "Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = { materialExcluir = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioMaterialApi(
    inicial: MaterialResponse?,
    categorias: List<CategoriaResponse>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (
        descricao: String,
        valor: String,
        unidade: String,
        categoriaId: Int
    ) -> Unit
) {
    var descricao by remember(inicial) {
        mutableStateOf(inicial?.descricao.orEmpty())
    }
    var valor by remember(inicial) {
        mutableStateOf(inicial?.valor.orEmpty())
    }
    var unidade by remember(inicial) {
        mutableStateOf(inicial?.unidadeMedida.orEmpty())
    }
    var categoriaSelecionada by remember(inicial, categorias) {
        mutableStateOf(
            categorias.find {
                it.idcategorias == inicial?.categoriasId
            }
        )
    }
    var menuAberto by remember { mutableStateOf(false) }
    var erroFormulario by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!salvando) onCancelar()
        },
        title = {
            Text(
                if (inicial == null) {
                    "Cadastrar material"
                } else {
                    "Editar material"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Descrição") },
                    singleLine = true,
                    enabled = !salvando
                )

                OutlinedTextField(
                    value = valor,
                    onValueChange = { valor = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    singleLine = true,
                    enabled = !salvando
                )

                OutlinedTextField(
                    value = unidade,
                    onValueChange = { unidade = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Unidade de medida") },
                    singleLine = true,
                    enabled = !salvando
                )

                Box {
                    OutlinedTextField(
                        value = categoriaSelecionada?.descricao.orEmpty(),
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { menuAberto = true },
                        readOnly = true,
                        label = { Text("Categoria") },
                        placeholder = { Text("Selecione uma categoria") },
                        enabled = !salvando
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
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
                        text = "Nenhuma categoria disponível. Cadastre uma categoria primeiro.",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                erroFormulario?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando && categorias.isNotEmpty(),
                onClick = {
                    val valorNormalizado = valor.trim().replace(",", ".")
                    val valorDecimal = valorNormalizado.toBigDecimalOrNull()

                    erroFormulario = when {
                        descricao.isBlank() ->
                            "Informe a descrição."

                        descricao.length > 150 ->
                            "A descrição deve ter no máximo 150 caracteres."

                        valorDecimal == null ->
                            "Informe um valor válido."

                        valorDecimal < BigDecimal.ZERO ->
                            "O valor não pode ser negativo."

                        valorDecimal.scale().coerceAtLeast(0) > 2 ->
                            "O valor deve ter no máximo duas casas decimais."

                        valorDecimal.precision() > 10 ->
                            "O valor informado é muito grande."

                        unidade.isBlank() ->
                            "Informe a unidade de medida."

                        unidade.length > 45 ->
                            "A unidade deve ter no máximo 45 caracteres."

                        categoriaSelecionada == null ->
                            "Selecione uma categoria."

                        else -> null
                    }

                    if (erroFormulario == null) {
                        onSalvar(
                            descricao.trim(),
                            valorDecimal!!.setScale(2).toPlainString(),
                            unidade.trim(),
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

private fun formatarMoeda(valor: String): String {
    val decimal = valor.toBigDecimalOrNull() ?: BigDecimal.ZERO
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(decimal)
}
