package com.example.appfirebasecrud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// Representa um produto armazenado no Firestore
data class Produto(
    val id: String = "",
    val nome: String = "",
    val descricao: String = ""
)

/**
 * Tela que demonstra as operações de Create, Read, Update e Delete (CRUD)
 * utilizando o Firebase Firestore.
 */
@Composable
fun CrudScreen(onBack: () -> Unit) {
    val db = Firebase.firestore // Instância do Firestore

    var nome by remember { mutableStateOf("") } // Campo de nome do produto
    var descricao by remember { mutableStateOf("") } // Campo de descrição
    var editandoId by remember { mutableStateOf<String?>(null) } // ID do produto em edição

    // Lista reativa dos produtos cadastrados
    val produtos = remember { mutableStateListOf<Produto>() }

    // Realiza a leitura dos dados no Firestore em tempo real
    LaunchedEffect(Unit) {
        db.collection("produtos").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                produtos.clear()
                for (doc in snapshot.documents) {
                    produtos.add(
                        Produto(
                            id = doc.id,
                            nome = doc.getString("nome") ?: "",
                            descricao = doc.getString("descricao") ?: ""
                        )
                    )
                }
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "CRUD de Produtos",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5EA500)
        )

        // Campos de entrada para nome e descrição
        TextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        TextField(
            value = descricao,
            onValueChange = { descricao = it },
            label = { Text("Descrição") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        // Botão que cria ou atualiza um documento no Firestore
        Button(
            onClick = {
                val dados = mapOf(
                    "nome" to nome,
                    "descricao" to descricao
                )
                if (editandoId == null) {
                    db.collection("produtos").add(dados) // Create
                } else {
                    db.collection("produtos").document(editandoId!!).set(dados) // Update
                }
                nome = ""
                descricao = ""
                editandoId = null
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(if (editandoId == null) "Salvar" else "Atualizar")
        }

        // Exibe os produtos e permite editar ou excluir
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 16.dp)
        ) {
            items(produtos) { produto ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(produto.nome, fontWeight = FontWeight.Bold)
                            Text(produto.descricao)
                        }
                        Row {
                            IconButton(onClick = {
                                nome = produto.nome
                                descricao = produto.descricao
                                editandoId = produto.id
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = {
                                db.collection("produtos").document(produto.id).delete() // Delete
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                            }
                        }
                    }
                }
            }
        }

        // Retorna para a tela anterior
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar")
        }
    }
}