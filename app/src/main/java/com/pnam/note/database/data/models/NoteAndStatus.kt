package com.pnam.note.database.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteAndStatus (
    @PrimaryKey @ColumnInfo(name = "note_id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "desc") val description: String,
    @ColumnInfo(name = "create_at") val createAt: Long,
    @ColumnInfo(name = "edit_at") var editAt: Long,
    @ColumnInfo(name = "status") val status: Int
        )