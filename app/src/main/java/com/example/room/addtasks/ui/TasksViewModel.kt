package com.example.room.addtasks.ui

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.example.room.addtasks.data.AppDatabase
import com.example.room.addtasks.data.Task
import com.example.room.addtasks.data.TaskDao
import com.example.room.addtasks.ui.model.TaskModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope

class TasksViewModel: ViewModel() {

    //Los LiveData no van bien con los listados que se tienen que ir actualizando...
    //Para solucionarlo, podemos utilizar un mutableStateListOf porque se lleva mejor con LazyColumn a la hora de refrescar la información en la vista...
    private val _tasks = mutableStateListOf<TaskModel>()
    val tasks: List<TaskModel> = _tasks

    private val _showDialog = MutableLiveData<Boolean>()
    val showDialog: LiveData<Boolean> = _showDialog

    private val _myTaskText = MutableLiveData<String>()
    val myTaskText: LiveData<String> = _myTaskText

    private lateinit var taskDao: TaskDao

    // Add database initialization method
    fun initializeDatabase(context: Context) {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "Task"
        ).build()
        taskDao = db.taskDao()

        // Load tasks on background thread
        viewModelScope.launch(Dispatchers.IO) {
            val dbTasks = taskDao.getAll()
            withContext(Dispatchers.Main) {
                _tasks.clear()
                _tasks.addAll(dbTasks.map {
                    TaskModel(id = it.id.toLong(), task = it.task, selected = it.selected)
                })
            }
        }
    }

    // Load tasks from database
    private fun loadTasks() {
        val dbTasks = taskDao.getAll()
        _tasks.clear()
        _tasks.addAll(dbTasks.map {
            TaskModel(id = it.id.toLong(), task = it.task, selected = it.selected)
        })
    }

    fun onTaskCreated() {
        onDialogClose()
        viewModelScope.launch(Dispatchers.IO) {
            val newTask = TaskModel(task = _myTaskText.value ?: "")
            val roomTask = Task(
                id = newTask.id.toInt(),
                task = newTask.task,
                selected = newTask.selected
            )
            taskDao.insertTask(roomTask)
            withContext(Dispatchers.Main) {
                _tasks.add(newTask)
                _myTaskText.value = ""
            }
        }
    }

    fun onItemRemove(taskModel: TaskModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val roomTask = Task(
                id = taskModel.id.toInt(),
                task = taskModel.task,
                selected = taskModel.selected
            )
            taskDao.deleteTask(roomTask)
            withContext(Dispatchers.Main) {
                _tasks.remove(taskModel)
            }
        }
    }

    fun onCheckBoxSelected(taskModel: TaskModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTask = taskModel.copy(selected = !taskModel.selected)
            val roomTask = Task(
                id = updatedTask.id.toInt(),
                task = updatedTask.task,
                selected = updatedTask.selected
            )
            taskDao.updateTask(roomTask)

            withContext(Dispatchers.Main) {
                val index = _tasks.indexOf(taskModel)
                _tasks[index] = updatedTask
            }
        }
    }

    fun onDialogClose() {
        _showDialog.value = false
    }

    fun onShowDialogClick() {
        _showDialog.value = true
    }

    fun onTaskTextChanged(taskText: String) {
        _myTaskText.value = taskText
    }
}