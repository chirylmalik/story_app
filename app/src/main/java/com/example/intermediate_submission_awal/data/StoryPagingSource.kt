package com.example.intermediate_submission_awal.data

import android.os.Build.VERSION_CODES.P
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.response.ListStoryItem

class StoryPagingSource(private val apiService: ApiService, private val token: String) : PagingSource<Int, ListStoryItem>() {
    companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getStories(
                token = "Bearer $token",
                page = page,
                size = params.loadSize)
            val data = response.listStory?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = data,
                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                nextKey = if (data.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}