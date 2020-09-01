package com.wolvec.dimn.mvvmkotlin.repository

import androidx.lifecycle.LiveData
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.api.ApiResponse
import com.wolvec.dimn.mvvmkotlin.api.GithubApi
import com.wolvec.dimn.mvvmkotlin.db.UserDao
import com.wolvec.dimn.mvvmkotlin.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val userDao: UserDao,
    private val githubApi: GithubApi
) {
    fun loadUser(login: String): LiveData<Resource<User>>{
        return object: NetworkBoundResource<User, User>(appExecutors){
            override fun saveCallResult(item: User) {
                userDao.insert(item)// lo inserta en la bd local
            }

            override fun shouldFetch(data: User?): Boolean {
                return data == null//pregunta si la data existe en la bd local
            }

            override fun loadFromDb(): LiveData<User> {
                return userDao.findByLogin(login)//carga los datos de la bd local
            }

            override fun createCall(): LiveData<ApiResponse<User>> {
                return githubApi.getUser(login)//obtiene los datos del webservices
            }
        }.asLiveData()//lo convierte a liveData
    }
}