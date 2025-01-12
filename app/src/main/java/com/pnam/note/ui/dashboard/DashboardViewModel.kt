package com.pnam.note.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnam.note.database.data.locals.NoteLocals
import com.pnam.note.database.data.locals.entities.Note
import com.pnam.note.database.data.models.PagingList
import com.pnam.note.throwable.NoConnectivityException
import com.pnam.note.utils.AppConstants.LIMIT_ON_PAGE
import com.pnam.note.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val useCase: DashboardUseCase,
    val noteLocals: NoteLocals
) : ViewModel() {
    var page = 0
    val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    private val _dashboard: MutableLiveData<Resource<PagingList<Note>>> by lazy {
        MutableLiveData<Resource<PagingList<Note>>>()
    }
    private val _deleteNote: MutableLiveData<Resource<Note>> by lazy {
        MutableLiveData<Resource<Note>>()
    }
    private val _searchNotes: MutableLiveData<Resource<MutableList<Note>>> by lazy {
        MutableLiveData<Resource<MutableList<Note>>>()
    }

    private val _refresh: MutableLiveData<Resource<PagingList<Note>>> by lazy {
        MutableLiveData<Resource<PagingList<Note>>>()
    }

    internal val dashboard: MutableLiveData<Resource<PagingList<Note>>> get() = _dashboard
    internal val deleteNote: MutableLiveData<Resource<Note>> get() = _deleteNote
    internal val searchNotes: MutableLiveData<Resource<MutableList<Note>>> get() = _searchNotes
    internal val refresh: MutableLiveData<Resource<PagingList<Note>>> get() = _refresh

    private val composite: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    private var dashboardDisposable: Disposable? = null
    private var deleteNoteDisposable: Disposable? = null
    private var searchNotesDisposable: Disposable? = null
    private var refreshDisposable: Disposable? = null

    private val observerDashboard: Consumer<PagingList<Note>> by lazy {
        Consumer<PagingList<Note>> { paging ->
            val ls = if (dashboard.value != null && dashboard.value is Resource.Success) {
                val ls = (dashboard.value as Resource.Success).data.data.toMutableList()
                ls.addAll(paging.data)
                ls
            } else {
                paging.data
            }
            _dashboard.postValue(
                Resource.Success(
                    PagingList(
                        ls,
                        paging.hasNextPage,
                        paging.hasPrePage
                    )
                )
            )
            if (paging.hasNextPage) {
                page++
            }
        }
    }
    private val observerDeleteNote: Consumer<Note> by lazy {
        Consumer<Note> { note ->
            _deleteNote.postValue(Resource.Success(note))
        }
    }
    private val observerSearchNotes: Consumer<MutableList<Note>> by lazy {
        Consumer<MutableList<Note>> { list ->
            _searchNotes.postValue(Resource.Success(list))
        }
    }
    private val observerRefresh: Consumer<PagingList<Note>> by lazy {
        Consumer<PagingList<Note>> { paging ->
            _refresh.postValue(Resource.Success(paging))
        }
    }

    internal fun getNotes() {
        _dashboard.postValue(Resource.Loading())
        dashboardDisposable?.let {
            composite.remove(it)
            it.dispose()
        }
        dashboardDisposable = useCase.getNotes(page, LIMIT_ON_PAGE)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observerDashboard) { t ->
                when (t) {
                    is NoConnectivityException -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            dashboardDisposable =
                                noteLocals.findNotes(page, LIMIT_ON_PAGE).map { localNotes ->
                                    /* Tạo thêm DAO để check hasNextPage, hasPrePage */
                                    if (localNotes.size > LIMIT_ON_PAGE) {
                                        page++
                                    }
                                    PagingList(
                                        localNotes,
                                        hasNextPage = true,
                                        hasPrePage = page > 0
                                    )
                                }
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(observerDashboard) { localError ->
                                        _dashboard.postValue(
                                            Resource.Error(
                                                localError.message ?: "Unknown error"
                                            )
                                        )
                                    }
                        }
                    }
                    else -> {
                        _dashboard.postValue(Resource.Error(t.message ?: "Unknown error"))
                    }
                }
            }
        dashboardDisposable?.let { composite.add(it) }
    }

    internal fun refreshNotes() {
        _refresh.postValue(Resource.Loading())
        refreshDisposable?.let {
            composite.remove(it)
            it.dispose()
        }
        refreshDisposable = useCase.refreshNotes(page, LIMIT_ON_PAGE)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observerRefresh) { t ->
                when (t) {
                    is NoConnectivityException -> {
                        error.postValue("No internet connection")
                    }
                    else -> {
                        _refresh.postValue(Resource.Error(t.message ?: "Unknown error"))
                    }
                }
            }
        refreshDisposable?.let { composite.add(it) }
    }

    internal fun deleteNote(note: Note) {
        _deleteNote.postValue(Resource.Loading())
        deleteNoteDisposable?.let {
            composite.remove(it)
            it.dispose()
        }
        deleteNoteDisposable = useCase.deleteNote(note)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(observerDeleteNote) { t ->
                when (t) {
                    is NoConnectivityException -> {
                        viewModelScope.launch(Dispatchers.IO) {
                            when (noteLocals.deleteNoteOffline(note)) {
                                null -> {
                                    _deleteNote.postValue(
                                        Resource.Error(
                                            t.message
                                        )
                                    )
                                }
                                else -> {
                                    deleteNoteDisposable = Single.just(note)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(observerDeleteNote) { localError ->
                                            _deleteNote.postValue(
                                                Resource.Error(
                                                    localError.message ?: "Unknown error"
                                                )
                                            )
                                        }
                                }
                            }

                        }
                    }
                    else -> {
                        _deleteNote.postValue(Resource.Error(t.message ?: "Unknown error"))
                    }
                }
            }
        deleteNoteDisposable?.let { composite.add(it) }
    }

    internal fun updateDashboardLiveData(ls: List<Note>) {
        val pgList = (dashboard.value as Resource.Success).data
        _dashboard.postValue(
            Resource.Success(
                PagingList(
                    ls,
                    pgList.hasNextPage,
                    pgList.hasPrePage
                )
            )
        )
    }

    internal fun searchNotes(keySearch: String) {
        _searchNotes.postValue(Resource.Loading())
        searchNotesDisposable?.let {
            composite.remove(it)
            it.dispose()
        }
        searchNotesDisposable = if (keySearch.isEmpty()) {
            noteLocals.findNotes(page, LIMIT_ON_PAGE)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(observerSearchNotes) { localError ->
                    _searchNotes.postValue(Resource.Error(localError.message ?: "Unknown error"))
                }
        } else {
            useCase.searchNotes(keySearch)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(observerSearchNotes) { t ->
                    _searchNotes.postValue(Resource.Error(t.message ?: "Unknown error"))
                }
        }
        searchNotesDisposable?.let { composite.add(it) }
    }

    override fun onCleared() {
        super.onCleared()
        composite.dispose()
    }
}