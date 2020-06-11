package com.wolvec.dimn.mvvmkotlin.model

import com.google.gson.annotations.SerializedName
//dataclass es una clase q solo contiene lo mas basico (constructor y getters/setters)
data class User(
    //@field:SerializedName("login") se utiliza para serializar el json q recives de una api...en este caso "login" es el nombre del atributo en el json
    @field:SerializedName("login")
    val login: String,
    @field:SerializedName("avatar_url")
    val avatarUrl: String?,
    @field:SerializedName("name")
    val name: String?,
    @field:SerializedName("company")
    val company: String?,
    @field:SerializedName("repos_url")
    val reposUrl: String?,
    @field:SerializedName("blog")
    val blog: String?
)
