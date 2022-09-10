package com.pnam.note.ui.addnoteimages

import android.content.Context
import com.pnam.note.database.data.models.PagingList
import com.pnam.note.database.repositories.ImageRepositories
import com.pnam.note.database.repositories.NoteRepositories
import io.reactivex.rxjava3.core.Single
import java.io.File
import javax.inject.Inject

class AddNoteImagesUseCaseImpl @Inject constructor(
    private val imageRepositories: ImageRepositories
) : AddNoteImagesUseCase {
    override fun findImages(context: Context, page: Int, limit: Int): Single<PagingList<String>> {
        return imageRepositories.findImages(context, page, limit)
    }
}