package com.pnam.note.database.repositories

import com.pnam.note.database.data.locals.NoteLocals
import com.pnam.note.database.data.models.Note
import com.pnam.note.database.data.models.PagingList
import com.pnam.note.database.data.networks.NoteNetworks
import io.reactivex.rxjava3.core.Single
import java.io.File
import javax.inject.Singleton

@Singleton
interface NoteRepositories {
    val locals: NoteLocals
    val networks: NoteNetworks
    fun getNotes(page: Int, limit: Int): Single<PagingList<Note>>
    fun refreshNotes(page: Int, limit: Int): Single<PagingList<Note>>
    fun getNoteDetail(): Single<Note>
    fun addNote(note: Note): Single<Note>
    fun editNote(note: Note): Single<Note>
    fun deleteNote(note: Note): Single<Note>
    fun searchNotes(keySearch: String): Single<MutableList<Note>>

    fun uploadImages(noteId: String, files: List<File>): Single<String>
}