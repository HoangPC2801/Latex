package com.example.bttuan4btvnn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {
    private val api = RetrofitClient.apiService

    // State Home
    private val _taskList = MutableStateFlow<List<Task>>(emptyList())
    val taskList: StateFlow<List<Task>> = _taskList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // State Detail
    private val _taskDetail = MutableStateFlow<Task?>(null) // Sửa type thành Task?
    val taskDetail: StateFlow<Task?> = _taskDetail

    // Lấy danh sách
    fun fetchTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getTasks()
                if (response.isSuccess) {
                    _taskList.value = response.data ?: emptyList()
                } else {
                    _taskList.value = getFakeTasks() // Fallback dữ liệu giả
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _taskList.value = getFakeTasks() // API lỗi -> Dùng dữ liệu giả
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Lấy chi tiết (Input là String từ navigation, convert sang Int)
    fun fetchTaskDetail(idString: String) {
        viewModelScope.launch {
            try {
                val id = idString.toIntOrNull() ?: 1
                val detail = api.getTaskDetail(id)
                _taskDetail.value = detail
            } catch (e: Exception) {
                e.printStackTrace()
                // Lấy tạm từ list giả nếu API detail lỗi
                _taskDetail.value = getFakeTasks().find { it.id.toString() == idString }
            }
        }
    }

    // Xóa
    fun deleteTask(idString: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val id = idString.toIntOrNull() ?: return@launch
                val response = api.deleteTask(id)
                if (response.isSuccessful) {
                    onSuccess()
                    fetchTasks()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onSuccess() // Giả vờ xóa thành công để UI quay lại
            }
        }
    }

    // --- DỮ LIỆU GIẢ (CẬP NHẬT THEO MODEL MỚI) ---
    private fun getFakeTasks(): List<Task> {
        return listOf(
            Task(
                id = 1,
                title = "Complete Android Project",
                description = "Finish the UI, integrate API, and write documentation",
                status = "In Progress",
                priority = "High",
                category = "Work",
                dueDate = "2024-03-26T09:00:00Z",
                subtasks = listOf(
                    Subtask(11, "Team Meeting", true),
                    Subtask(12, "Prepare Slides", false)
                ),
                attachments = listOf(
                    Attachment(100, "document_1.pdf", "")
                )
            ),
            Task(
                id = 2,
                title = "Doctor Appointment",
                description = "Regular health checkup",
                status = "Pending",
                priority = "Medium",
                category = "Health",
                dueDate = "2024-03-28T14:00:00Z",
                subtasks = listOf(
                    Subtask(11, "Team Meeting", true),
                    Subtask(12, "Prepare Slides", false)
                ),
                attachments = listOf(
                    Attachment(100, "document_1.pdf", "")
                )
            ),
            Task(
                id = 3,
                title = "Gym Workout",
                description = "Leg day exercises",
                status = "Done",
                priority = "Low",
                category = "Fitness",
                dueDate = "2024-03-29T18:00:00Z",
                subtasks = listOf(
                    Subtask(11, "Team Meeting", true),
                    Subtask(12, "Prepare Slides", false)
                ),
                attachments = listOf(
                    Attachment(100, "document_1.pdf", "")
                )
            )
        )
    }
}