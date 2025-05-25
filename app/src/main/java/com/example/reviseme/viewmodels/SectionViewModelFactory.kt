package com.example.reviseme.viewmodels

import AppDatabase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SectionViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SectionViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}