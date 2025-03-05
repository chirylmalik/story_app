package com.example.intermediate_submission_awal.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.login.LoginViewModel
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.data.response.RegisterResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupViewModel(private val pref: UserPreference) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val error = MutableLiveData("")
    val message = MutableLiveData("")

    private val TAG = LoginViewModel::class.simpleName

    fun signup(name: String, email: String, password: String) {
        _isLoading.value = true
        makeSignupCall(name, email, password)
    }

    private fun makeSignupCall(name: String, email: String, password: String) {
        val client = ApiConfig.getApiService().doSignup(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                handleResponse(response)
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                handleFailure(t)
            }
        })
    }

    private fun handleResponse(response: Response<RegisterResponse>) {
        when (response.code()) {
            400 -> error.postValue("400")
            201 -> message.postValue("201")
            else -> error.postValue("ERROR ${response.code()} : ${response.errorBody()}")
        }
        _isLoading.value = false
    }

    private fun handleFailure(t: Throwable) {
        _isLoading.value = true
        Log.e(TAG, "onFailure Call: ${t.message}")
        error.postValue(t.message)
    }
}