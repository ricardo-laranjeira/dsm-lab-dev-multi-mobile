package com.example.orcamentoapp.screens.categorias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.orcamentoapp.data.repositories.CategoriaRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    token: String = "",
    onVoltar: () -> Unit
) {
    val repository = remember { CategoriaRepository() }
    val viewModel = remember { CategoriasViewModel(repository) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    var pesquisa by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaEditando by remember { mutableStateOf<CategoriaUi?>(null) }
    var categoriaExcluir by remember { mutableStateOf<CategoriaUi?>(null) }

    LaunchedEffect(token) {
        viewModel.carregar()
    }

    LaunchedEffect(uiState.mensagem) {
        uiState.mensagem?.let { msg ->
            snackbar.showSnackbar(msg)
            viewModel.limparMensagem()
        }
    }

    val categoriasFiltradas = uiState.categorias.filter {
        it.nome.contains(pesquisa, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorias") },
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
                    categoriaEditando = null
                    mostrarFormulario = true
                }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nova categoria"
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
                label = { Text("Pesquisar categoria") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                text = "${categoriasFiltradas.size} categoria(s)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            uiState.erro?.let { erroMsg ->
                Text(
                    text = erroMsg,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )

                TextButton(
                    onClick = { viewModel.carregar() }
                ) {
                    Text("Tentar novamente")
                }
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            if (uiState.carregando) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 24.dp)
                )
            } else if (categoriasFiltradas.isEmpty()) {
                Text(
                    text = if (pesquisa.isBlank()) {
                        "Nenhuma categoria cadastrada."
                    } else {
                        "Nenhuma categoria encontrada."
                    },
                    modifier = Modifier.padding(top = 24.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = categoriasFiltradas,
                        key = { it.id }
                    ) { categoria ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text(
                                        text = categoria.nome,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Text(
                                        text = "Categoria de materiais e serviços",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        categoriaEditando = categoria
                                        mostrarFormulario = true
                                    },
                                    enabled = !uiState.salvando
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar categoria"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        categoriaExcluir = categoria
                                    },
                                    enabled = !uiState.salvando
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Excluir categoria",
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

    if (mostrarFormulario) {
        FormularioCategoria(
            categoria = categoriaEditando,
            nomesExistentes = uiState.categorias
                .filter { it.id != categoriaEditando?.id }
                .map { it.nome },
            salvando = uiState.salvando,
            onCancelar = {
                if (!uiState.salvando) {
                    mostrarFormulario = false
                }
            },
            onSalvar = { nome ->
                viewModel.salvar(categoriaEditando?.id, nome) {
                    mostrarFormulario = false
                    categoriaEditando = null
                }
            }
        )
    }

    categoriaExcluir?.let { categoria ->
        AlertDialog(
            onDismissRequest = {
                categoriaExcluir = null
            },
            title = {
                Text("Excluir categoria")
            },
            text = {
                Text(
                    "Deseja excluir a categoria \"${categoria.nome}\"?"
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = {
                        viewModel.excluir(categoria.id) {
                            categoriaExcluir = null
                        }
                    }
                ) {
                    Text(
                        if (uiState.salvando) "Excluindo..." else "Excluir",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !uiState.salvando,
                    onClick = { categoriaExcluir = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FormularioCategoria(
    categoria: CategoriaUi?,
    nomesExistentes: List<String>,
    salvando: Boolean,
    onCancelar: () -> Unit,
    onSalvar: (String) -> Unit
) {
    var nome by remember(categoria) {
        mutableStateOf(categoria?.nome.orEmpty())
    }
    var erro by remember(categoria) {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                if (categoria == null) {
                    "Nova categoria"
                } else {
                    "Editar categoria"
                }
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        erro = ""
                    },
                    label = {
                        Text("Nome da categoria")
                    },
                    singleLine = true,
                    enabled = !salvando,
                    modifier = Modifier.fillMaxWidth()
                )

                if (erro.isNotBlank()) {
                    Text(
                        text = erro,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (salvando) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !salvando,
                onClick = {
                    val nomeValidado = nome.trim()

                    when {
                        nomeValidado.isBlank() -> {
                            erro = "Informe o nome da categoria."
                        }

                        nomeValidado.length > 45 -> {
                            erro = "O nome deve ter até 45 caracteres."
                        }

                        nomesExistentes.any {
                            it.equals(nomeValidado, ignoreCase = true)
                        } -> {
                            erro = "Já existe uma categoria com esse nome."
                        }

                        else -> onSalvar(nomeValidado)
                    }
                }
            ) {
                Text(if (salvando) "Salvando..." else "Salvar")
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
