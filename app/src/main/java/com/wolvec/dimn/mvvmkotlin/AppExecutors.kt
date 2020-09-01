package com.wolvec.dimn.mvvmkotlin

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AppExecutors(//clase para ejecutar los hilos
    val diskIO: Executor,//Hilo de bd local ROOM
    val networkIO: Executor,//Hilo de la red Retrofit
    val mainThread: Executor//Hilo principal
) {
    @Inject
    constructor(): this(
        Executors.newSingleThreadExecutor(),//esta es para diskIO..permite q una vez q se obtenga la instancia executor podemos enviar las tareas
        Executors.newFixedThreadPool(3),//networkIO...crean 3 hilos q se utilizan ...es decir 3 es el maximo de consultas
        MainThreadExecutor()//es para nuestro hilo principal
    )

    fun diskIO(): Executor{
        return diskIO
    }

    fun networkIO(): Executor{
        return networkIO
    }

    fun mainThread(): Executor{
        return mainThread
    }

    private class  MainThreadExecutor: Executor{//hace las tareas en el hilo principal
        val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable){
            mainThreadHandler.post(command)
        }
    }

}