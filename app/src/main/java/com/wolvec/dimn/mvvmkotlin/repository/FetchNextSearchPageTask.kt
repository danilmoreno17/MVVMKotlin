package com.wolvec.dimn.mvvmkotlin.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.wolvec.dimn.mvvmkotlin.api.*
import com.wolvec.dimn.mvvmkotlin.db.GithubDb
import com.wolvec.dimn.mvvmkotlin.model.RepoSearchResult
import java.io.IOException

class FetchNextSearchPageTask constructor(
    private val query: String,//lo que se quiere buscar
    private val githubApi: GithubApi,//la clase para consultar webservice
    private val db: GithubDb//clase para consultar bd local
): Runnable  {
    private val _liveData = MutableLiveData<Resource<Boolean>>()
    val liveData: LiveData<Resource<Boolean>> = _liveData

    override fun run() {
        val current = db.repoDao().findSearchResult(query)//primero lee en la bd la query
        if(current == null){
            _liveData.postValue(null)//returna null
            return
        }
        val nextPage = current.next
        if(nextPage == null){
            _liveData.postValue(Resource.success(false))//guarda el valor de success para cn data false
            return
        }
        val newValue = try {
            val response = githubApi.searchRepos(query, nextPage).execute()//consulta en el webservices con la sig pagina como page:Int
            val apiResponse = ApiResponse.create(response)//guarda el resultado de la consulta en el obk apiResponse
            when (apiResponse){
                is ApiSuccessResponse ->{//si el apiResponse es exitoso
                    val ids = arrayListOf<Int>()
                    ids.addAll(current.repoIds)//agrega los ids actuales guaradados en la bd local
                    ids.addAll(apiResponse.body.items.map { it.id })//agrega los ids nuevos de la sig pagina
                    val merged = RepoSearchResult(query, ids, apiResponse.body.total, apiResponse.nextPage)//guarda los datos en el obj RepoSearchResult

                    try{
                        db.beginTransaction()
                        db.repoDao().insert(merged)//inserta en la bd local(repoSearchResult)
                        db.repoDao().insertRepos(apiResponse.body.items)//inserta los repositorios a la tabla
                        db.setTransactionSuccessful()
                    }finally {
                        db.endTransaction()
                    }
                    Resource.success(apiResponse.nextPage != null)
                }
                is ApiEmptyResponse ->{
                    Resource.success(false)
                }
                is ApiErrorResponse ->{
                    Resource.error(apiResponse.errorMessage, true)
                }
            }
        }catch (e: IOException){
            Resource.error(e.message!!, true)
        }
        _liveData.postValue(newValue)
    }
}
