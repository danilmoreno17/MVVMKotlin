package com.wolvec.dimn.mvvmkotlin.model

import androidx.room.Entity
import androidx.room.TypeConverters
import com.wolvec.dimn.mvvmkotlin.db.GithubTypeConverters

@Entity(primaryKeys = ["query"])
@TypeConverters(GithubTypeConverters::class)
class RepoSearchResult(
    val query: String,
    val reporIds: List<Int>,
    val totalCount: Int,
    val next: Int?
) {
}