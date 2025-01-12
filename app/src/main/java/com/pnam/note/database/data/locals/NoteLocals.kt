package com.pnam.note.database.data.locals

import com.pnam.note.database.data.locals.entities.Note
import com.pnam.note.database.data.locals.entities.NoteStatus
import com.pnam.note.database.data.locals.entities.NoteAndStatus
import io.reactivex.rxjava3.core.Single
import javax.inject.Singleton

@Singleton
interface NoteLocals {
    fun findNotes(page: Int, limit: Int): Single<MutableList<Note>>
    fun findNoteDetail(id: String): Single<Note>
    fun addNote(note: Note)
    fun addNote(notes: List<Note>)
    fun editNote(note: Note)
    fun deleteNote(note: Note)
    fun searchNotes(keySearch: String): Single<MutableList<Note>>

    fun deleteAllNote()

    fun addNoteStatus(noteStatus: NoteStatus)
    fun deleteNoteStatus(noteStatus: NoteStatus)

    fun deleteAllNoteStatus()

    fun addNoteOffline(note: Note): Note?
    fun afterAddNoteOffline(oldNote: Note, newNote: Note)

    fun editNoteOffline(note: Note): Note?
    fun afterEditNoteOffline(oldNote: Note, newNote: Note)

    fun deleteNoteOffline(note: Note): Note?
    fun afterDeleteNoteOffline(deletedNote: Note)

    fun findAsyncNotes(): List<NoteAndStatus>
}