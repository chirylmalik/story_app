package com.example.intermediate_submission_awal.data.api

import com.example.intermediate_submission_awal.data.response.AddStoryResponse
import com.example.intermediate_submission_awal.data.response.LoginResponse
import com.example.intermediate_submission_awal.data.response.RegisterResponse
import com.example.intermediate_submission_awal.data.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun doSignup(
        @Field("name") name: String?,
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    fun doSignin(
        @Field("email") email: String?,
        @Field("password") password: String?
    ): Call<LoginResponse>

    @GET("stories")
    fun getListStory(
        @Header("Authorization") bearer: String?
    ): Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun postNewStory(
        @Header("Authorization") bearer: String?,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody?,
    ): Call<AddStoryResponse>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoryResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): StoryResponse

}