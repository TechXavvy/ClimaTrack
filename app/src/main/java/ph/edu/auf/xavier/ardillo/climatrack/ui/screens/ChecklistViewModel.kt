package ph.edu.auf.xavier.ardillo.climatrack.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ph.edu.auf.xavier.ardillo.climatrack.local.ChecklistDao
import ph.edu.auf.xavier.ardillo.climatrack.local.models.ChecklistItem

class ChecklistViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ChecklistItem>>(emptyList())
    val items: StateFlow<List<ChecklistItem>> = _items

    init { refresh() }

    fun refresh() {
        _items.value = ChecklistDao.all()
    }
    fun add(label: String) = viewModelScope.launch {
        ChecklistDao.add(label)
        refresh()
    }
    fun toggle(id: String, done: Boolean) = viewModelScope.launch {
        ChecklistDao.toggle(id, done)
        refresh()
    }
}
