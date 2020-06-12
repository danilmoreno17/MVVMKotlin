package com.wolvec.dimn.mvvmkotlin.repository

data class Resource<out T> (val status: Status, val data: T?, val message: String?){//clase generica q va a mantener el valor cuando estamos cargando los datos
    companion object{//indica que los metodos agrupados seran estaticos
        fun<T> success(data: T?): Resource<T>{
            return Resource(Status.SUCCESS, data, null)
        }
        fun<T> error(msg: String, data: T?): Resource<T>{
            return Resource(Status.ERROR, data, msg)
        }
        fun<T> loading(data: T?): Resource<T>{
            return Resource(Status.LOADING, data, null)
        }
    }
}
