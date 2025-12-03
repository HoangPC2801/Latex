package com.example.bttuan4btvnn

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

// --- 1. DATA MODELS (Thêm ? để tránh crash khi dữ liệu null) ---

data class BaseResponse(
    val isSuccess: Boolean,
    val message: String,
    val data: List<Task>? // List cũng có thể null
)

data class Task(
    val id: Int,
    val title: String?,
    val description: String?,
    val status: String?,
    val priority: String?,
    val category: String?,
    val dueDate: String?, // <-- Thêm dấu ?
    val subtasks: List<Subtask>?, // <-- Thêm dấu ?
    val attachments: List<Attachment>? // <-- Thêm dấu ?
)

data class Subtask(
    val id: Int,
    val title: String?,
    val isCompleted: Boolean
)

data class Attachment(
    val id: Int,
    val fileName: String?,
    val fileUrl: String?
)

// --- 2. API SERVICE ---

interface ApiService {
    @GET("tasks")
    suspend fun getTasks(): BaseResponse

    @GET("task/{id}")
    suspend fun getTaskDetail(@Path("id") id: Int): Task

    @DELETE("task/{id}")
    suspend fun deleteTask(@Path("id") id: Int): retrofit2.Response<Unit>
}

// --- 3. RETROFIT CLIENT ---

object RetrofitClient {
    private const val BASE_URL = "https://amock.io/api/researchUTH/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}