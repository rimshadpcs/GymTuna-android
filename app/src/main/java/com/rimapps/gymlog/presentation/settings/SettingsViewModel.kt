package com.rimapps.gymlog.presentation.settings

import AppTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rimapps.gymlog.domain.model.UserProfile
import com.rimapps.gymlog.domain.repository.AuthRepository
import com.rimapps.gymlog.utils.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class WeightUnit(val displayName: String) {
    KG("Kilograms (kg)"),
    LB("Pounds (lb)")
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _weightUnit = MutableStateFlow(WeightUnit.KG)
    val weightUnit: StateFlow<WeightUnit> = _weightUnit.asStateFlow()

    private val _currentTheme = MutableStateFlow(AppTheme.LIGHT)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()


    init {
        loadUserProfile()
        loadPreferences()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            authRepository.getCurrentUser()?.let { user ->
                _userProfile.value = UserProfile(
                    uid = user.uid,
                    displayName = user.displayName ?: "",
                    email = user.email ?: ""
                )
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            //_weightUnit.value = userPreferences.getWeightUnit()
        }
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            try {
                //authRepository.updateUserProfile(newName)
                _userProfile.value = _userProfile.value.copy(displayName = newName)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateWeightUnit(unit: WeightUnit) {
        viewModelScope.launch {
            //userPreferences.setWeightUnit(unit)
            _weightUnit.value = unit
        }
    }
    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
//            userPreferences.setTheme(theme)
//            _currentTheme.value = theme
        }
    }
}