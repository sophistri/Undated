package com.example.undated.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("categoryId")]
)
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startDay: Long,          // epoch day
    val endDay: Long,            // epoch day, inclusive
    val categoryId: Long? = null,
    val color: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceEventId: Long? = null,    // id of the Google event it was copied from
    val sourceBegin: Long? = null       // start of that occurrence, in millis


)