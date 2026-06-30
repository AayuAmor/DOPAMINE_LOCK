package com.teamdobermans.dopamine_lock.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teamdobermans.dopamine_lock.model.FocusPreferences
import com.teamdobermans.dopamine_lock.repo.FocusPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusPreferencesViewModel(
    private val repository: FocusPreferencesRepository
) : ViewModel() {
    private val _preferences = MutableStateFlow(FocusPreferences())
    val preferences: StateFlow<FocusPreferences> = _preferences.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePreferences().collect { prefs ->
                _preferences.value = prefs
            }
        }
    }

    fun updatePreferences(preferences: FocusPreferences) {
        _preferences.value = preferences
        viewModelScope.launch {
            repository.savePreferences(preferences)
        }
    }
}

class FocusPreferencesViewModelFactory(
    private val repository: FocusPreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FocusPreferencesViewModel::class.java)) {
            return FocusPreferencesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
