package com.pnam.note.database.data.locals.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.pnam.note.database.data.locals.NoteLocals
import com.pnam.note.database.data.locals.entities.Note
import com.pnam.note.database.data.locals.entities.NoteAndStatus
import com.pnam.note.database.data.locals.entities.NoteStatus
import com.pnam.note.utils.RoomUtils.Companion.ADD_NOTE_STATUS
import com.pnam.note.utils.RoomUtils.Companion.DELETE_NOTE_STATUS
import com.pnam.note.utils.RoomUtils.Companion.EDIT_NOTE_STATUS
import io.reactivex.rxjava3.core.Single
import java.lang.Exception

@Dao
interface NoteDao : NoteLocals {

    @Insert(onConflict = REPLACE)
    override fun addNote(note: Note)

    @Insert(onConflict = REPLACE)
    override fun addNote(notes: List<Note>)

    @Update(onConflict = REPLACE)
    override fun editNote(note: Note)

    @Delete
    override fun deleteNote(note: Note)

    // Get note by pagination
    @Query("SELECT * FROM Note ORDER BY edit_at DESC, create_at DESC LIMIT :limit OFFSET :page*:limit-:limit")
    override fun findNotes(page: Int, limit: Int): Single<MutableList<Note>>

    // Search key in title and description to find notes
    @Query(
        "SELECT * FROM Note " +
                "WHERE title LIKE '%' || :keySearch || '%' " +
                "OR `desc` LIKE '%' || :keySearch || '%' " +
                "ORDER BY edit_at DESC, create_at DESC"
    )
    override fun searchNotes(keySearch: String): Single<MutableList<Note>>

    @Query("SELECT * FROM Note WHERE note_id = :id LIMIT 1")
    override fun findNoteDetail(id: String): Single<Note>

    // Delete all note (call when logout)
    @Query("DELETE FROM Note")
    override fun deleteAllNote()

    @Insert(onConflict = REPLACE)
    override fun addNoteStatus(noteStatus: NoteStatus)

    @Delete
    override fun deleteNoteStatus(noteStatus: NoteStatus)

    // Delete all note status (call when logout)
    @Query("DELETE FROM NoteStatus")
    override fun deleteAllNoteStatus()

    @Query("SELECT * FROM NoteStatus WHERE id = :id LIMIT 1")
    fun findNoteStatusById(id: String): List<NoteStatus>

    // Add note when not having internet
    @Transaction
    override fun addNoteOffline(note: Note): Note? {
        return try {
            addNote(note)
            addNoteStatus(NoteStatus(note.id, ADD_NOTE_STATUS))
            note
        } catch (ex: Exception) {
            null
        }
    }

    // Delete note status after uploading changes to cloud
    @Transaction
    override fun afterAddNoteOffline(oldNote: Note, newNote: Note) {
        deleteNote(oldNote)
        deleteNoteStatus(NoteStatus(oldNote.id, ADD_NOTE_STATUS))
        addNote(newNote)
    }

    // Edit note when not having internet
    @Transaction
    override fun editNoteOffline(note: Note): Note? {
        return try {
            editNote(note)
            if (findNoteStatusById(note.id).isEmpty()) {
                addNoteStatus(NoteStatus(note.id, EDIT_NOTE_STATUS))
            }
            note
        } catch (ex: Exception) {
            null
        }
    }

    // Delete note status after uploading changes to cloud
    @Transaction
    override fun afterEditNoteOffline(oldNote: Note, newNote: Note) {
        deleteNote(oldNote)
        deleteNoteStatus(NoteStatus(oldNote.id, EDIT_NOTE_STATUS))
        addNote(newNote)
    }

    // Delete note when not having internet
    @Transaction
    override fun deleteNoteOffline(note: Note): Note? {
        return try {
            deleteNote(note)
            val list = findNoteStatusById(note.id)
            if (list.isEmpty()) {
                addNoteStatus(NoteStatus(note.id, DELETE_NOTE_STATUS))
            } else if (list[0].status == ADD_NOTE_STATUS) {
                deleteNoteStatus(NoteStatus(note.id, ADD_NOTE_STATUS))
            } else if (list[0].status == EDIT_NOTE_STATUS) {
                addNoteStatus(NoteStatus(note.id, DELETE_NOTE_STATUS))
            }
            note
        } catch (ex: Exception) {
            null
        }
    }

    // Delete note status after uploading changes to cloud
    @Transaction
    override fun afterDeleteNoteOffline(deletedNote: Note) {
        deleteNoteStatus(NoteStatus(deletedNote.id, DELETE_NOTE_STATUS))
    }

    // Get async note
    @Query("SELECT * FROM NoteStatus")
    override fun findAsyncNotes(): List<NoteAndStatus>
}