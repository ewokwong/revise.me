package com.example.reviseme.viewmodels

import AppDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reviseme.data.Section
import com.example.reviseme.data.SectionWithTopics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SectionViewModel(private val database: AppDatabase) : ViewModel() {
    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections

    init {
        loadSections()
    }

    private fun loadSections() {
        viewModelScope.launch {
            _sections.value = database.sectionDao().getAllSections()
        }
    }

    fun addSection(name: String) {
        viewModelScope.launch {
            database.sectionDao().insertSection(Section(name = name))
            loadSections() // Refresh the sections list
        }
    }

    fun deleteSection(section: Section) {
        viewModelScope.launch {
            database.sectionDao().deleteSection(section)
            loadSections() // Refresh the sections list
        }
    }
}