package com.example.orcamentoapp.screens.orcamentos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun SeletorOrcamento(
    label: String,
    selecionado: String,
    opcoes: List<String>,
    aoSelecionar: (String) -> Unit,
    habilitado: Boolean = true
) {
    var expandido by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { expandido = true },
            enabled = habilitado && opcoes.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(selecionado, style = MaterialTheme.typography.bodyLarge)
            }
            Text("▼")
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opcoes.forEach { opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        aoSelecionar(opcao)
                        expandido = false
                    }
                )
            }
        }
    }
}