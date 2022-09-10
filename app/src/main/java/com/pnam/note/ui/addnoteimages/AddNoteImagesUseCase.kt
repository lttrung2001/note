package com.pnam.note.ui.addnoteimages

import android.content.Context
import com.pnam.note.database.data.models.PagingList
import io.reactivex.rxjava3.core.Single
import java.io.File
import javax.inject.Singleton

@Singleton
interface AddNoteImagesUseCase {
    fun findImages(context: Context, page: Int, limit: Int): Single<PagingList<String>>
}