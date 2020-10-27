package com.wolvec.dimn.mvvmkotlin.ui.search

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.wolvec.dimn.mvvmkotlin.model.Repo
import com.wolvec.dimn.mvvmkotlin.repository.RepoRepository
import com.wolvec.dimn.mvvmkotlin.repository.Resource
import com.wolvec.dimn.mvvmkotlin.repository.Status
import com.wolvec.dimn.mvvmkotlin.utils.AbsentLiveData
import java.util.*
import javax.inject.Inject

class SearchViewModel @Inject constructor(repoRepository: RepoRepository): ViewModel() {

    private val query = MutableLiveData<String>()
    private val nextPageHandler = NextPageHandler(repoRepository)
    val queryLD: LiveData<String> = query

    val result: LiveData<Resource<List<Repo>>> = Transformations
        .switchMap(query){search->
            if(search.isNullOrBlank()){
                AbsentLiveData.create()
            }else{
                repoRepository.search(search)
            }
        }

    val loadMoreStatus: LiveData<LoadMoreState>
        get() = nextPageHandler.loadMoreState

    fun setQuery(originalInput: String){
        val input = originalInput.toLowerCase(Locale.getDefault()).trim()
        if(input == query.value){
            return
        }
        nextPageHandler.reset()
        query.value =  input
    }

    fun loadNextPage(){
        query.value?.let {
            if(it.isNotBlank()){
                nextPageHandler.queryNextPage(it)
            }
        }
    }

    fun refresh(){
        query.value?.let {
            query.value = it
        }
    }


    class LoadMoreState(val isRunning: Boolean, val errorMessage: String?){//no sirve para saber si estamo en el estado de cargar mas repos(muestra la barra de espera de abajo)
        private var handleError = false

        val errorMessageIfNotHandled: String?
            get() {
                if(handleError){
                    return null
                }
                handleError = true
                return errorMessage
            }
    }

    class NextPageHandler(private val repository: RepoRepository): Observer<Resource<Boolean>>{//

        private var nextPageLiveData: LiveData<Resource<Boolean>>? = null//si hay nueva pagina lo capturamos en esta variable
        val loadMoreState = MutableLiveData<LoadMoreState>()
        private var query: String? = null//va a guardar la query de busqueda
        private var _hasMore: Boolean = false//para ver si hay siguiente pagina se inicializa false por defecto
        val hasMore//para obtener el value de _hasMore
            get() = _hasMore

        init {
            reset()
        }

        fun queryNextPage(query: String){
            if(this.query == query){//para verificar q las consultas sean distintas
                return
            }
            unregister()
            this.query = query
            nextPageLiveData = repository.searchNextPage(query)
            loadMoreState.value = LoadMoreState(
                isRunning = true,
                errorMessage = null
            )
            nextPageLiveData?.observeForever(this)
        }

        override fun onChanged(result: Resource<Boolean>?) {
            if(result == null){
                reset()
            }else{
                when(result.status){
                    Status.SUCCESS ->{
                        _hasMore = result.data == true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = null
                            )
                        )
                    }
                    Status.ERROR -> {
                        _hasMore = true
                        unregister()
                        loadMoreState.setValue(
                            LoadMoreState(
                                isRunning = false,
                                errorMessage = result.message
                            )
                        )
                    }
                    Status.LOADING ->{

                    }
                }
            }
        }

        private fun unregister(){
            nextPageLiveData?.removeObserver(this)
            nextPageLiveData = null
            if(_hasMore){
                query = null
            }
        }

        fun reset(){
            unregister()
            _hasMore = true
            loadMoreState.value = LoadMoreState(
                isRunning = false,
                errorMessage = null
            )
        }
    }
}