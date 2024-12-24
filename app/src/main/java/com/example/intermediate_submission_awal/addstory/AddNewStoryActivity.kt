package com.example.intermediate_submission_awal.addstory


import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.intermediate_submission_awal.R
import com.example.intermediate_submission_awal.ViewModelFactory
import com.example.intermediate_submission_awal.createMultipartBodyPart
import com.example.intermediate_submission_awal.createRequestBody
import com.example.intermediate_submission_awal.data.UserPreference
import com.example.intermediate_submission_awal.data.api.ApiConfig
import com.example.intermediate_submission_awal.databinding.ActivityAddNewStoryBinding
import com.example.intermediate_submission_awal.login.LoginViewModel
import com.example.intermediate_submission_awal.main.MainActivity
import com.example.intermediate_submission_awal.main.MainViewModel
import com.example.intermediate_submission_awal.reduceFileImage
import com.example.intermediate_submission_awal.uriToFile
import java.io.File

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "User")

class AddNewStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewStoryBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var signViewModel: LoginViewModel
    private var getFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNewStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        supportActionBar?.title = getString(R.string.add_story)

        val factory = ViewModelFactory(
            UserPreference.getInstance(dataStore),
            ApiConfig.getApiService()
        )

        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        signViewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        binding.btnAddGalery.setOnClickListener { startGallery() }
        binding.simpanButton.setOnClickListener { uploadStory() }

        observeViewModel()
    }

    private fun observeViewModel() {
        mainViewModel.isLoading.observe(this) { showLoading(it) }
        mainViewModel.isSuccess.observe(this) { isSuccess ->
            if (isSuccess) {
                navigateToMainActivity()
            } else {
                Toast.makeText(this, getString(R.string.add_story_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadStory() {
        if (getFile != null) {
            if (binding.edAddDescription.text.toString().isNotEmpty()) {
                val file = reduceFileImage(getFile as File)
                val imagePart = createMultipartBodyPart(file, "photo")
                val descriptionPart = createRequestBody(binding.edAddDescription.text.toString())

                signViewModel.getUser().observe(this) { user ->
                    val token = user.token ?: ""
                    mainViewModel.postNewStory(token, imagePart, descriptionPart)
                }
            } else {
                Toast.makeText(this@AddNewStoryActivity, getString(R.string.description_mandatory), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this@AddNewStoryActivity, getString(R.string.image_mandatory), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarAdd.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_picture))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this@AddNewStoryActivity)
            getFile = myFile

            binding.imageView.setImageURI(selectedImg)
        }
    }
}