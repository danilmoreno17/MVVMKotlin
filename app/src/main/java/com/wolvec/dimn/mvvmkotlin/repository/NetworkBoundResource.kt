package com.wolvec.dimn.mvvmkotlin.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.api.ApiEmptyResponse
import com.wolvec.dimn.mvvmkotlin.api.ApiErrorResponse
import com.wolvec.dimn.mvvmkotlin.api.ApiResponse
import com.wolvec.dimn.mvvmkotlin.api.ApiSuccessResponse

abstract class NetworkBoundResource<ResultType, RequestType>//Gestiona el recurso q proviene desde la nube o la bd local
@MainThread/*etiqueta q indica q se va llamar en el hilo principal*/ constructor(private val appExecutors: AppExecutors){
    private val result = MediatorLiveData<Resource<ResultType>>()//permite combinar varios objetos livedata cuando se notifica q hay un cambio en uno

    init {
        result.value = Resource.loading(null)//inicia el stado cn loading
        val dbSource = loadFromDb()//
        result.addSource(dbSource){data->
            result.removeSource(dbSource)
            if(shouldFetch(data)){
                fetchFromNetwork(dbSource)
            } else{
                result.addSource(dbSource){newData->
                    setValue(Resource.success(newData))
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>){
        if(result.value != newValue){
            result.value = newValue
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>){
        val apiResponse = createCall()
        result.addSource(dbSource){newData->
            setValue(Resource.loading(newData))
        }
        result.addSource(apiResponse){response->
            result.removeSource(apiResponse)
            result.removeSource(dbSource)
            when(response){
                is ApiSuccessResponse->{
                    appExecutors.diskIO().execute{
                        saveCallResult(processResponse(response))
                        appExecutors.mainThread().execute{
                            result.addSource(loadFromDb()){newData->
                                setValue(Resource.success(newData))
                            }
                        }
                    }
                }
                is ApiEmptyResponse->{
                    appExecutors.mainThread().execute{
                        result.addSource(loadFromDb()){newData->
                            setValue(Resource.success(newData))
                        }
                    }
                }
                is ApiEmptyResponse ->{
                    onFetchFailed()
                    result.addSource(dbSource){newData->
                        setValue(Resource.error((response as ApiErrorResponse).errorMessage, newData))
                    }
                }
            }

        }
    }

    protected open fun onFetchFailed(){}

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>
}