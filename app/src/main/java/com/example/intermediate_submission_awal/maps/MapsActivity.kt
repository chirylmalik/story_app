package com.example.intermediate_submission_awal.maps

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.intermediate_submission_awal.R
import com.example.intermediate_submission_awal.ViewModelFactory
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.data.dataStore
import com.example.intermediate_submission_awal.data.repository.StoryRepository
import com.example.intermediate_submission_awal.data.response.ListStoryItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.intermediate_submission_awal.databinding.ActivityMapsBinding
import com.example.intermediate_submission_awal.login.LoginActivity
import com.example.intermediate_submission_awal.login.LoginViewModel
import com.google.android.gms.maps.model.LatLngBounds

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "User")

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var boundsBuilder = LatLngBounds.Builder()
    private lateinit var mapsViewModel: MapsViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreference = UserPreference.getInstance(dataStore)

        val apiService = ApiConfig.getApiService()
        val repository = StoryRepository(apiService)

        val factory = ViewModelFactory(
            UserPreference.getInstance(dataStore),
            apiService,
            repository
        )

        mapsViewModel = ViewModelProvider(this, factory)[MapsViewModel::class.java]
        loginViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        loginViewModel.getUser().observe(this) { user ->
            val token = user.token
            if (!token.isNullOrEmpty()) {
                mapsViewModel.fetchStoriesWithLocation(token)
            } else {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        mapsViewModel.storiesWithLocation.observe(this) { stories ->
            addMarker(stories)
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    private fun addMarker(stories: List<ListStoryItem>) {
        mMap.clear()

        boundsBuilder = LatLngBounds.Builder()

        stories.forEach { story ->
            val lat = story.lat
            val lon = story.lon

            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.name)
                        .snippet(story.description)
                )
                boundsBuilder.include(latLng)
            }
        }

        val bounds: LatLngBounds = boundsBuilder.build()
        if (::mMap.isInitialized) {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    300
                )
            )
        }

    }
}