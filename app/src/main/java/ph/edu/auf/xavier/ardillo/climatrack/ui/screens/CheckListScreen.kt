package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.local.ChecklistDao
import ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem

@Composable
fun CheckListScreen() {
    var items by remember { mutableStateOf<List<ChecklistItem>>(emptyList()) }
    var newText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    fun refresh() { items = ChecklistDao.all() }

    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Go-Bag Checklist", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Row {
            OutlinedTextField(
                value = newText,
                onValueChange = { newText = it },
                label = { Text("Add item") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (newText.isNotBlank()) {
                    scope.launch {
                        ChecklistDao.add(newText.trim())
                        newText = ""
                        refresh()
                    }
                }
            }) { Text("Add") }
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(items, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.label)
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { checked ->
                            scope.launch {
                                ChecklistDao.toggle(item.id, checked)
                                refresh()
                            }
                        }
                    )
                }
                Divider()
            }
        }
    }
}
