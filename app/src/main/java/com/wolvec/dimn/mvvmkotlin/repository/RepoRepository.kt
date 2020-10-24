package com.wolvec.dimn.mvvmkotlin.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.api.ApiResponse
import com.wolvec.dimn.mvvmkotlin.api.ApiSuccessResponse
import com.wolvec.dimn.mvvmkotlin.api.GithubApi
import com.wolvec.dimn.mvvmkotlin.db.GithubDb
import com.wolvec.dimn.mvvmkotlin.db.RepoDao
import com.wolvec.dimn.mvvmkotlin.model.Contributor
import com.wolvec.dimn.mvvmkotlin.model.Repo
import com.wolvec.dimn.mvvmkotlin.model.RepoSearchResponse
import com.wolvec.dimn.mvvmkotlin.model.RepoSearchResult
import com.wolvec.dimn.mvvmkotlin.utils.AbsentLiveData
import com.wolvec.dimn.mvvmkotlin.utils.RateLimiter
import java.security.acl.Owner
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton//unica instancia en la aplicacion
class RepoRepository @Inject constructor(
    private val appExecutors: AppExecutors,//para saber en q hilo trabajar
    private val db: GithubDb,//la bd (room)
    private val repoDao: RepoDao,
    private val githubApi: GithubApi//web service
) {
    private val repoListRateLimiter = RateLimiter<String>(10,TimeUnit.MINUTES)//esta clase nos ayudara para saber cuando debemos hacer la peticion al webservice

    fun loadRepos(owner: String): LiveData<Resource<List<Repo>>> {
        return object: NetworkBoundResource<List<Repo>, List<Repo>>(appExecutors){
            override fun saveCallResult(item: List<Repo>) {//salvar en la bd la lista de repo que nos llega
                repoDao.insertRepos(item)
            }

            override fun shouldFetch(data: List<Repo>?): Boolean {
                return data == null || data.isEmpty() || repoListRateLimiter.shoulFetch(owner)
            }

            override fun loadFromDb(): LiveData<List<Repo>> = repoDao.loadRepositories(owner)


            override fun createCall(): LiveData<ApiResponse<List<Repo>>> = githubApi.getRepos(owner)


            override fun onFetchFailed(){
                repoListRateLimiter.reset(owner)
            }

        }.asLiveData()
    }

    fun loadRepo(owner: String, name: String): LiveData<Resource<Repo>>{
        return object: NetworkBoundResource<Repo, Repo>(appExecutors){
            override fun saveCallResult(item: Repo) {
                repoDao.insert(item)
            }

            override fun shouldFetch(data: Repo?): Boolean = data == null

            override fun loadFromDb(): LiveData<Repo> = repoDao.load(
                ownerLogin = owner,
                name = name
            )

            override fun createCall(): LiveData<ApiResponse<Repo>> = githubApi.getRepo(
                owner = owner,
                name = name
            )

        }.asLiveData()
    }

    fun loadContributors(owner: String, name: String): LiveData<Resource<List<Contributor>>>{
        return object: NetworkBoundResource<List<Contributor>, List<Contributor>>(appExecutors){
            override fun saveCallResult(item: List<Contributor>) {
                item.forEach {
                    it.repoName = name
                    it.repoOwner = owner
                }
                db.runInTransaction{
                    repoDao.createRepoIfNoExists(
                        Repo(
                            id = Repo.UNKOWN_ID,
                            name = name,
                            fullName = "$owner/$name",
                            description = "",
                            owner = Repo.Owner(owner, null),
                            stars = 0
                        )
                    )
                    repoDao.insertContributors(item)
                }
            }

            override fun shouldFetch(data: List<Contributor>?): Boolean {
                return data == null || data.isEmpty()
            }

            override fun loadFromDb(): LiveData<List<Contributor>> = repoDao.loadContributors(owner, name)

            override fun createCall(): LiveData<ApiResponse<List<Contributor>>> = githubApi.getContributors(owner, name)

        }.asLiveData()
    }

    fun searchNextPage(query: String): LiveData<Resource<Boolean>>{
        val fetchNextSearchPageTask = FetchNextSearchPageTask(
            query = query,
            githubApi = githubApi,
            db = db
        )
        appExecutors.networkIO().execute(fetchNextSearchPageTask)
        return fetchNextSearchPageTask.liveData
    }

    fun search(query: String): LiveData<Resource<List<Repo>>>{
        return object : NetworkBoundResource<List<Repo>, RepoSearchResponse>(appExecutors){
            override fun saveCallResult(item: RepoSearchResponse) {
                val repoIds = item.items.map { it.id }
                val repoSearchResult = RepoSearchResult(
                    query = query,
                    repoIds = repoIds,
                    totalCount = item.total,
                    next = item.nextPage
                )
                db.runInTransaction {
                    repoDao.insertRepos(item.items)
                    repoDao.insert(repoSearchResult)
                }

            }

            override fun shouldFetch(data: List<Repo>?): Boolean = data == null

            override fun loadFromDb(): LiveData<List<Repo>> {
                return Transformations.switchMap(repoDao.search(query)){searchData ->
                    if(searchData == null){
                        AbsentLiveData.create()
                    } else{
                        repoDao.loadOrdered(searchData.repoIds)
                    }
                }
            }

            override fun createCall(): LiveData<ApiResponse<RepoSearchResponse>> = githubApi.searchRepos(query)

            override fun processResponse(response: ApiSuccessResponse<RepoSearchResponse>): RepoSearchResponse {
                val body = response.body
                body.nextPage = response.nextPage
                return body
            }

        }.asLiveData()
    }
}