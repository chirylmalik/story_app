package com.example.intermediate_submission_awal.main

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.intermediate_submission_awal.DataDummy
import com.example.intermediate_submission_awal.MainDispatcherRule
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiService
import com.example.intermediate_submission_awal.data.dataStore
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import com.example.intermediate_submission_awal.data.response.StoryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var apiService: ApiService

    private lateinit var mainViewModel: MainViewModel
    private lateinit var fakeDataStore: DataStore<Preferences>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        fakeDataStore = PreferenceDataStoreFactory.create {
            File("test_datastore.preferences_pb").apply { deleteOnExit() }
        }

        val userPreference = UserPreference.getInstance(fakeDataStore)
        mainViewModel = MainViewModel(userPreference, apiService)
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStories)

        `when`(apiService.getStories(anyString(), anyInt(), anyInt()))
            .thenReturn(StoryResponse(listStory = dummyStories, error = false, message = "Success"))

        val actualStories: PagingData<ListStoryItem> = mainViewModel.getListStory("Bearer dummy_token").getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DiffCallback,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStories)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStories.size, differ.snapshot().size)
        Assert.assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())

        `when`(apiService.getStories(anyString(), anyInt(), anyInt()))
            .thenReturn(StoryResponse(listStory = emptyList(), error = false, message = "No stories"))

        val actualStories: PagingData<ListStoryItem> = mainViewModel.getListStory("Bearer dummy_token").getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DiffCallback,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        Assert.assertEquals(0, differ.snapshot().size)
    }
}

fun Flow<PagingData<ListStoryItem>>.getOrAwaitValue(): PagingData<ListStoryItem> {
    return runBlocking {
        first()
    }
}

class StoryPagingSource(private val apiService: ApiService, private val token: String) : PagingSource<Int, ListStoryItem>() {
    companion object {
        const val INITIAL_PAGE_INDEX = 1

        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getStories(
                token = token,
                page = page,
                size = params.loadSize
            )

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

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}