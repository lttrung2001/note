package com.pnam.note.ui.imagedetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val useCase: ImageDetailUseCase
) : ViewModel() {
    private val _downloadId: MutableLiveData<Long> by lazy {
        MutableLiveData<Long>()
    }
    private val _downloadIdValue: Long get() = _downloadId.value ?: -1

    fun download(url: String): Long {
        return useCase.download(url)
    }

    fun getDownloadStatus(id: Long): String {
        return useCase.getDownloadStatus(_downloadIdValue)
    }

    fun downloadProgress(
        id: Long,
        progressHandle: (bytesDownloaded: Long, bytesTotal: Long) -> Unit
    ) {
        useCase.downloadProgress(id, progressHandle)
    }
}