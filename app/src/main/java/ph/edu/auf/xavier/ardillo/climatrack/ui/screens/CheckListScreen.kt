package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.local.ChecklistDao
import ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem

@Composable
fun CheckListScreen() {
    val vm: ChecklistViewModel = viewModel()
    val items by vm.items.collectAsState()
    var newText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Go-Bag Checklist", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
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
                    vm.add(newText.trim())
                    newText = ""
                }
            }) { Text("Add") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn {
            items(items, key = { it.id }) { item ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.label)
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { checked -> vm.toggle(item.id, checked) }
                    )
                }
                Divider()
            }
        }
    }
}
