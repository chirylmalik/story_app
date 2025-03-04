package com.example.intermediate_submission_awal.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.intermediate_submission_awal.data.StoryPagingSource
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import kotlinx.coroutines.flow.Flow

class StoryRepository(private val apiService: ApiService) {

    fun getStories(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }
}