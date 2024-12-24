package com.example.intermediate_submission_awal.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.data.response.LoginResponse
import com.example.intermediate_submission_awal.data.response.LoginResult
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading

    val error = MutableLiveData<String>().apply { value = "" }
    val message = MutableLiveData<String>().apply { value = "" }
    val loginResult = MutableLiveData<LoginResponse>()

    private val TAG = LoginViewModel::class.java.simpleName

    fun getUser(): LiveData<LoginResult> = pref.getUser().asLiveData()

    fun saveUser(userName: String, userId: String, userToken: String) {
        viewModelScope.launch {
            pref.saveUser(userName, userId, userToken)
        }
    }

    fun logout() {
        viewModelScope.launch {
            pref.logout()
        }
    }

    fun login(email: String, password: String) {
        _isLoading.postValue(true)
        ApiConfig.getApiService().doSignin(email, password).enqueue(createLoginCallback())
    }

    private fun createLoginCallback(): Callback<LoginResponse> {
        return object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                handleResponse(response)
                _isLoading.postValue(false)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                handleFailure(t)
            }
        }
    }

    private fun handleResponse(response: Response<LoginResponse>) {
        when (response.code()) {
            200 -> {
                loginResult.postValue(response.body())
                message.postValue("200")
            }
            400 -> error.postValue("400")
            401 -> error.postValue("401")
            else -> error.postValue("ERROR ${response.code()} : ${response.message()}")
        }
    }

    private fun handleFailure(t: Throwable) {
        Log.e(TAG, "onFailure Call: ${t.message}")
        error.postValue(t.message ?: "Unknown error occurred")
        _isLoading.postValue(false)
    }
}