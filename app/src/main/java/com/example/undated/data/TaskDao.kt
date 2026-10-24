package com.example.undated.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isDone ASC, priority DESC, createdAt DESC")
    fun getAll(): Flow<List<Task>>  // Every time the table changes, it automatically emits a fresh list

    @Insert
    suspend fun insert(task: Task): Long  // suspend marks a function that does slow work (like disk access) and must not block the screen

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY priority DESC, createdAt DESC")
    suspend fun getOpen(): List<Task>
}