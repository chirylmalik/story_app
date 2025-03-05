package com.example.intermediate_submission_awal.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.intermediate_submission_awal.R
import com.example.intermediate_submission_awal.ViewModelFactory
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.databinding.ActivityMainBinding
import com.example.intermediate_submission_awal.login.LoginActivity
import com.example.intermediate_submission_awal.login.LoginViewModel
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import com.example.intermediate_submission_awal.addstory.AddNewStoryActivity
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.data.repository.StoryRepository
import com.example.intermediate_submission_awal.databinding.ItemLoadingBinding
import com.example.intermediate_submission_awal.maps.MapsActivity
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "User")

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Story App"

        setupViewModel()
        observeViewModel()
        setupRecyclerView()

        binding.btnAddStory.setOnClickListener {
            val intent = Intent(this@MainActivity, AddNewStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupViewModel() {
        val apiService = ApiConfig.getApiService()
        val repository = StoryRepository(apiService)

        val factory = ViewModelFactory(
            UserPreference.getInstance(dataStore),
            apiService,
            repository
        )
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
    }

    private fun observeViewModel() {
        loginViewModel.getUser().observe(this) { user ->
            if (user.userId?.isEmpty() == true) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val token = user.token
                if (!token.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        mainViewModel.getListStory(token).collect { pagingData ->
                            storyAdapter.submitData(pagingData)
                        }
                    }

                }
            }
        }
        mainViewModel.isLoading.observe(this) {
            showLoading(it)
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter()
        val layoutManager = LinearLayoutManager(this)
        binding.rvListStory.layoutManager = layoutManager

        binding.rvListStory.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )
        val itemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        binding.rvListStory.addItemDecoration(itemDecoration)

        storyAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.Error) {
                Toast.makeText(this, "Failed to load stories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_item, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                Log.d("MainActivity", "Logout clicked")
                loginViewModel.logout()
            }
            R.id.maps -> {
                Log.d("MainActivity", "Maps clicked")
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}