package com.example.orcamentoapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private val AzulPrincipal = Color(0xFF173B67)

private data class OpcaoMenu(
    val titulo: String,
    val descricao: String,
    val icone: ImageVector,
    val acao: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSair: () -> Unit,
    onAbrirPerfil: () -> Unit,
    onAbrirClientes: () -> Unit = {},
    onAbrirOrcamentos: () -> Unit = {},
    onAbrirMateriais: () -> Unit = {},
    onAbrirServicos: () -> Unit = {},
    onAbrirAgendamentos: () -> Unit = {},
    onAbrirCategorias: () -> Unit = {},
    onAbrirEnderecos: () -> Unit = {}
) {
    val opcoes = listOf(
        OpcaoMenu(
            "Clientes",
            "Clientes e endereços",
            Icons.Default.People,
            onAbrirClientes
        ),
        OpcaoMenu(
            "Orçamentos",
            "Criar e consultar",
            Icons.Default.Description,
            onAbrirOrcamentos
        ),
        OpcaoMenu(
            "Materiais",
            "Itens e valores",
            Icons.Default.Inventory2,
            onAbrirMateriais
        ),
        OpcaoMenu(
            "Serviços",
            "Serviços e valores",
            Icons.Default.Build,
            onAbrirServicos
        ),
        OpcaoMenu(
            "Agendamentos",
            "Datas e horários",
            Icons.Default.CalendarMonth,
            onAbrirAgendamentos
        ),
        OpcaoMenu(
            "Categorias",
            "Organizar catálogo",
            Icons.Default.Category,
            onAbrirCategorias
        ),
        OpcaoMenu(
            "Perfil",
            "Seus dados e sua conta",
            Icons.Default.Person,
            onAbrirPerfil
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ORÇAMENTO APP",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Olá, seja bem-vindo!",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    androidx.compose.material3.IconButton(
                        onClick = onAbrirPerfil
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Meu perfil"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrincipal,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSair)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("Sair da conta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .padding(top = 20.dp)
        ) {
            Text(
                text = "Menu principal",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(opcoes) { opcao ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(138.dp)
                            .clickable(onClick = opcao.acao),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = opcao.icone,
                                contentDescription = null,
                                tint = AzulPrincipal,
                                modifier = Modifier.size(30.dp)
                            )

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(
                                text = opcao.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(
                                modifier = Modifier.height(4.dp)
                            )

                            Text(
                                text = opcao.descricao,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
