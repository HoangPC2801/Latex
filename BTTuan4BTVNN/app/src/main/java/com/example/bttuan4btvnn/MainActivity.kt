package com.example.bttuan4btvnn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

// --- Navigation ---
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: TaskViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController, viewModel)
        }
        composable("detail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: "1"
            DetailScreen(navController, viewModel, taskId)
        }
    }
}

// --- Màn hình Home ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: TaskViewModel) {
    val tasks by viewModel.taskList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchTasks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("UTH SmartTasks", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                        Text("A simple and efficient to-do app", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            )
        },
        bottomBar = { MyBottomBar() }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (tasks.isEmpty()) {
                EmptyView()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks) { task ->
                        TaskItem(task) {
                            navController.navigate("detail/${task.id}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ContentPaste,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color.Gray
        )
        Text("No Tasks Yet!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Stay productive—add something to do", color = Color.Gray)
    }
}

@Composable
fun TaskItem(task: Task, onClick: () -> Unit) {
    val category = task.category?.lowercase() ?: ""
    val bgColor = when (category) {
        "work" -> Color(0xFFEDB9B9)
        "fitness" -> Color(0xFFB9E6F8)
        else -> Color(0xFFE7EAB5)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title ?: "No Title", fontWeight = FontWeight.Bold)
            Text(task.description ?: "", fontSize = 14.sp, maxLines = 2)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if(task.status == "Done") Icons.Outlined.CheckCircle else Icons.Outlined.CheckBoxOutlineBlank
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Status: ${task.status ?: "Pending"}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(modifier = Modifier.weight(1f))

                // --- SỬA LỖI CRASH TẠI ĐÂY ---
                // Kiểm tra null trước khi gọi .length hoặc .take
                val rawDate = task.dueDate ?: ""
                val dateStr = if (rawDate.length >= 10) rawDate.take(10) else rawDate
                Text(dateStr, fontSize = 12.sp)
            }
        }
    }
}

// --- Màn hình Detail ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: TaskViewModel, taskId: String) {
    val detail by viewModel.taskDetail.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.fetchTaskDetail(taskId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color(0xFF2196F3))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteTask(taskId) {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373))
                    }
                }
            )
        }
    ) { padding ->
        if (detail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val task = detail!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(task.title ?: "No Title", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(task.description ?: "No Description", color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEDB9B9), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoColumn(icon = Icons.Default.Category, label = "Category", value = task.category ?: "N/A")
                    InfoColumn(icon = Icons.Default.Task, label = "Status", value = task.status ?: "Pending")
                    InfoColumn(icon = Icons.Default.Star, label = "Priority", value = task.priority ?: "Normal")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- SỬA LỖI CRASH SUBTASKS ---
                // Dùng safe call (?.) để tránh crash nếu list null
                if (!task.subtasks.isNullOrEmpty()) {
                    Text("Subtasks", fontWeight = FontWeight.Bold)
                    task.subtasks.forEach { sub ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isDone = sub.isCompleted
                            val icon = if(isDone) Icons.Outlined.CheckCircle else Icons.Outlined.CheckBoxOutlineBlank
                            Icon(icon, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(sub.title ?: "")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- SỬA LỖI CRASH ATTACHMENTS ---
                if (!task.attachments.isNullOrEmpty()) {
                    Text("Attachments", fontWeight = FontWeight.Bold)
                    task.attachments.forEach { file ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(file.fileName ?: "Document")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoColumn(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontSize = 12.sp)
        }
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun MyBottomBar() {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF2196F3))
            Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color.Gray)
            FloatingActionButton(
                onClick = {},
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
            Icon(Icons.Default.Description, contentDescription = "File", tint = Color.Gray)
            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.Gray)
        }
    }
}