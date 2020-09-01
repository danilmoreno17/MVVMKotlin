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

abstract class NetworkBoundResource<ResultType, RequestType>//Gestiona el recurso q proviene desde la nube o la bd local//RequestType es el tipo de dato de la solicitud y ResultType tipo de datos de la respuesta
@MainThread/*etiqueta q indica q se va llamar en el hilo principal*/
constructor(private val appExecutors: AppExecutors){
    private val result = MediatorLiveData<Resource<ResultType>>()//permite combinar varios objetos livedata cuando se notifica q hay un cambio en uno

    init {
        result.value = Resource.loading(null)//inicia el stado cn loading
        val dbSource = loadFromDb()//llama a una clase
        result.addSource(dbSource){data->//se va a ejecutar cada vez que dbSource obtenga un nuevo cambio
            result.removeSource(dbSource)//borra el contenido que ha guardado antes el result(MediatorLiveData)
            if(shouldFetch(data)){//condicion para saber si se llama a la bd o al webservice
                fetchFromNetwork(dbSource)//funcion para llamar desde la red(webservice)
            } else{
                result.addSource(dbSource){newData->
                    setValue(Resource.success(newData))
                }
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>){//clase actualiza los datos
        if(result.value != newValue){
            result.value = newValue
        }
    }

    private fun fetchFromNetwork(dbSource: LiveData<ResultType>){
        val apiResponse = createCall()//crea la llamada al webservice
        result.addSource(dbSource){newData->//
            setValue(Resource.loading(newData))//
        }//con esta funcion queremos mostrar primeros los datos de la bd hasta q termine de descargar los datos de la red
        result.addSource(apiResponse){response->//añade una nueva fuente de datos
            result.removeSource(apiResponse)//borra los datos del mediator del apiResponse
            result.removeSource(dbSource)//borra los datos del mediator dbSource
            when(response){//Cuando el resultado
                is ApiSuccessResponse->{//Es exitoso
                    appExecutors.diskIO().execute{//Se ejecuta en el hilo para room
                        saveCallResult(processResponse(response))//guarda los datos dentro de la bd local
                        appExecutors.mainThread().execute{//se ejecuta en el hilo principal
                            result.addSource(loadFromDb()){newData->//llama los datos de la bd recien cargados
                                setValue(Resource.success(newData))//y los agrega al mediator
                            }
                        }
                    }
                }
                is ApiEmptyResponse->{//si llega vacio o no retorna datos
                    appExecutors.mainThread().execute{//se ejecuta en el hilo principal
                        result.addSource(loadFromDb()){newData->//llama los datos de la bd
                            setValue(Resource.success(newData))//los setea al mediator
                        }//es decir si no llega datos del webservice, les mandamos la data de la bd
                    }
                }
                is ApiErrorResponse ->{//si es fallido
                    onFetchFailed()
                    result.addSource(dbSource){newData->//va a mostrar los datos q tiene en la bd local
                        setValue(Resource.error(response.errorMessage, newData))//setea un Resource.error
                    }
                }
            }

        }
    }

    protected open fun onFetchFailed() {//funcion en caso de q la operacion a fallado

    }

    fun asLiveData() = result as LiveData<Resource<ResultType>>//funcion q castea el MediatorLiveData a LiveData

    @WorkerThread
    protected open fun processResponse(response: ApiSuccessResponse<RequestType>) = response.body//funcion en caso de q la operacion sea exitosa

    @WorkerThread
    protected abstract fun saveCallResult(item: RequestType)//funcion para guardar los datos

    @MainThread
    protected abstract fun shouldFetch(data: ResultType?): Boolean//Consulta si hay datos en la bd o esos datos han expirado(segun el tiempo q una crea q deba tener los datos en la bd), true = carga desde la bd; false = carga desde el webservice

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>//clase abstracta q carga los datos de la base de datos

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<RequestType>>//funcion q hace la llamada
}