package com.wolvec.dimn.mvvmkotlin.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wolvec.dimn.mvvmkotlin.model.Contributor
import com.wolvec.dimn.mvvmkotlin.model.Repo
import com.wolvec.dimn.mvvmkotlin.model.RepoSearchResult
import com.wolvec.dimn.mvvmkotlin.model.User

@Database(
    entities = [
        User::class,
        Repo::class,
        Contributor::class,
        RepoSearchResult::class
    ],
    version = 1
)
abstract class GithubDb: RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun repoDao(): RepoDao
}