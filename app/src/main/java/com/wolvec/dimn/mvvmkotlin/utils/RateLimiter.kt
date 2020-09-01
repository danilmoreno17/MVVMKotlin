package com.wolvec.dimn.mvvmkotlin.utils

import android.os.SystemClock
import android.util.ArrayMap
import java.util.concurrent.TimeUnit

class RateLimiter<in KEY>(timeout: Int, timeUnit: TimeUnit) {

    private val timestamps = ArrayMap<KEY, Long>()//va a guaradar la key cuando se ha realizado la ultima peticion al servidor
    private val timeout = timeUnit.toMillis(timeout.toLong())//tiempo minimo q debera esperar para solicitar datos al servidor

    @Synchronized
    fun shoulFetch(key: KEY): Boolean{//funcion q devolvera si se debe actualizar o no
        val lastFetched = timestamps[key]
        val now= now()

        if(lastFetched == null){
            timestamps[key] = now
            return true
        }

        if(now - lastFetched > timeout){
            timestamps[key] = now
            return true
        }

        return false

    }

    private fun now() = SystemClock.uptimeMillis()

    @Synchronized
    fun reset(key: KEY){
        timestamps.remove(key)
    }
}