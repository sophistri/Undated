package com.example.undated.ui

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.undated.data.AppDatabase
import com.example.undated.data.Category
import com.example.undated.data.DeviceEvent
import com.example.undated.data.Event
import com.example.undated.data.Task
import com.example.undated.data.readDeviceEvents
import com.example.undated.widget.TaskWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

import com.example.undated.widget.refreshWidgets

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.taskDao()
    private val categoryDao = db.categoryDao()

    // whileSuscribed means it stops listening to the database 5 seconds after the screen goes away, which saves battery but survives quick rotations.
    val tasks: StateFlow<List<Task>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = categoryDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    private val _events = MutableStateFlow<List<DeviceEvent>>(emptyList())
    val events: StateFlow<List<DeviceEvent>> = _events.asStateFlow()

    private val eventDao = db.eventDao()

    val myEvents: StateFlow<List<Event>> = eventDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editingTask = MutableStateFlow<Task?>(null)
    val editingTask: StateFlow<Task?> = _editingTask.asStateFlow()

    private val _editingEvent = MutableStateFlow<Event?>(null)
    val editingEvent: StateFlow<Event?> = _editingEvent.asStateFlow()

    init {
        viewModelScope.launch {
            if (categoryDao.count() == 0) {
                categoryDao.insert(Category(name = "Personal", color = 0xFFAE4948.toInt()))
                categoryDao.insert(Category(name = "Study", color = 0xFFD48354.toInt()))
                categoryDao.insert(Category(name = "Work", color = 0xFF5E8C6A.toInt()))
                categoryDao.insert(Category(name = "Home", color = 0xFFE0B84C.toInt()))
            }
        }
    }

    init {
        viewModelScope.launch { refreshWidgets(getApplication<Application>()) }
    }

    // .launch starts a coroutine, the background context that the suspend functions from the DAO require.

    fun addTask(
        title: String,
        categoryId: Long?,
        priority: Int,
        dueStart: Long?,
        dueEnd: Long?
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insert(
                Task(
                    title = title.trim(),
                    categoryId = categoryId,
                    priority = priority,
                    dueStart = dueStart,
                    dueEnd = dueEnd
                )
            )
            refreshWidgets(getApplication<Application>())
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            dao.update(task.copy(isDone = !task.isDone))
            refreshWidgets(getApplication<Application>())
        }
    }

    fun delete(task: Task) {
        viewModelScope.launch {
            dao.delete(task)
            refreshWidgets(getApplication<Application>())
        }
    }

    fun addCategory(name: String, color: Int) {
        if (name.isBlank()) return
        viewModelScope.launch { categoryDao.insert(Category(name = name.trim(), color = color)) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
            refreshWidgets(getApplication<Application>())
        }
    }

    fun loadEvents(from: LocalDate, to: LocalDate) {
        viewModelScope.launch {
            _events.value = withContext(Dispatchers.IO) {
                readDeviceEvents(getApplication<Application>(), from, to)
            }
        }
    }

    fun addEvent(title: String, startDay: Long, endDay: Long, categoryId: Long?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val event = Event(
                title = title.trim(),
                startDay = startDay,
                endDay = endDay,
                categoryId = categoryId
            )
            val id = eventDao.insert(event)
            refreshWidgets(getApplication<Application>())
            _editingEvent.value = event.copy(id = id)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            eventDao.delete(event)
            refreshWidgets(getApplication<Application>())
        }
    }

    fun addSubtask(eventId: Long, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insert(Task(title = title.trim(), eventId = eventId))
            refreshWidgets(getApplication<Application>())
        }
    }

    fun startEdit(task: Task) { _editingTask.value = task }
    fun startEditEvent(event: Event) { _editingEvent.value = event }
    fun stopEdit() {
        _editingTask.value = null
        _editingEvent.value = null
    }

    fun updateTask(task: Task) {
        stopEdit()
        viewModelScope.launch {
            dao.update(task)
            refreshWidgets(getApplication<Application>())
        }
    }

    fun updateEvent(event: Event) {
        stopEdit()
        viewModelScope.launch {
            eventDao.update(event)
            refreshWidgets(getApplication<Application>())
        }
    }

    fun importEvent(e: DeviceEvent) {
        viewModelScope.launch {
            eventDao.insert(
                Event(
                    title = e.title,
                    startDay = e.startDay,
                    endDay = e.endDay,
                    color = e.color,
                    sourceEventId = e.id,
                    sourceBegin = e.startMillis
                )
            )
            refreshWidgets(getApplication<Application>())
        }
    }
}