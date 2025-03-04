package com.example.intermediate_submission_awal.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.intermediate_submission_awal.data.StoryPagingSource
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.repository.StoryRepository
import com.example.intermediate_submission_awal.data.response.AddStoryResponse
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import com.example.intermediate_submission_awal.data.response.StoryResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreference, private val repository: StoryRepository) : ViewModel() {

    private val _storyList = MutableLiveData<List<ListStoryItem>>()
    val storyList: LiveData<List<ListStoryItem>> = _storyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isSuccess = MutableLiveData<Boolean>()
    val isSuccess: LiveData<Boolean> = _isSuccess

    fun postNewStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        val client = ApiConfig.getApiService().postNewStory("Bearer $token", file, description)
        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(call: Call<AddStoryResponse>, response: Response<AddStoryResponse>) {
                handlePostNewStoryResponse(response, token)
            }

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                handlePostNewStoryFailure(t)
            }
        })
    }

    private fun handlePostNewStoryResponse(response: Response<AddStoryResponse>, token: String) {
        _isLoading.value = false
        if (response.isSuccessful && response.body()?.error == false) {
            _isSuccess.value = true
            getListStory(token)
        } else {
            _isSuccess.value = false
        }
    }

    private fun handlePostNewStoryFailure(t: Throwable) {
        _isLoading.value = false
        _isSuccess.value = false
    }

    fun getListStory(token: String): Flow<PagingData<ListStoryItem>> {
        return repository.getStories(token).cachedIn(viewModelScope)
    }

    private fun handleGetListStoryResponse(response: Response<StoryResponse>) {
        _isLoading.value = false
        if (response.isSuccessful) {
            val list = response.body()?.listStory?.filterNotNull() ?: emptyList()
            _storyList.value = list
        } else {
            Log.e(TAG, "onFailure: ${response.message()}")
        }
    }

    private fun handleGetListStoryFailure(t: Throwable) {
        _isLoading.value = false
        Log.e(TAG, "onFailure: ${t.message}")
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}