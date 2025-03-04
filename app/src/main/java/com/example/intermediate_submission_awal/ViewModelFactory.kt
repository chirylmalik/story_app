package com.example.intermediate_submission_awal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.repository.StoryRepository
import com.example.intermediate_submission_awal.login.LoginViewModel
import com.example.intermediate_submission_awal.main.MainViewModel
import com.example.intermediate_submission_awal.maps.MapsViewModel
import com.example.intermediate_submission_awal.signup.SignupViewModel

class ViewModelFactory(private val pref: UserPreference, private val apiService: ApiService, private val repository: StoryRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(pref) as T
            }
            modelClass.isAssignableFrom(SignupViewModel::class.java) -> {
                SignupViewModel(pref) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(pref, repository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) -> {
                MapsViewModel(apiService) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}