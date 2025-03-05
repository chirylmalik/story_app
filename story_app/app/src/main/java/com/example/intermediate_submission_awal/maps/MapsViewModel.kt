package com.example.intermediate_submission_awal.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val apiService: ApiService) : ViewModel() {

    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> = _storiesWithLocation

    fun fetchStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getStoriesWithLocation("Bearer $token")
                _storiesWithLocation.value = response.listStory?.filterNotNull()?.filter { story ->
                    story.lat != null && story.lon != null
                }
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error fetching stories: ${e.message}")
            }
        }
    }
}
