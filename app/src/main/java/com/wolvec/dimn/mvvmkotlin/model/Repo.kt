package com.wolvec.dimn.mvvmkotlin.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import com.google.gson.annotations.SerializedName

@Entity(
    indices = [Index("id"),
                Index("owner_login")],
    primaryKeys = ["name","owner_login"]
)
data class Repo (
    val id: Int,
    @field:SerializedName("name")
    val name: String,
    @field:SerializedName("full_name")
    val fullName: String,
    @field:SerializedName("description")
    val descripcion: String?,
    @field:SerializedName("owner")
    @field:Embedded(prefix = "owner_")//sirve para embeber una clase en otra en este caso owner estara enbebida en la clase Repo
    val owner: Owner,
    @field:SerializedName("stargazers_count")
    val stars: Int
){
    data class Owner(
        @field:SerializedName("login")
        val login: String,
        @field:SerializedName("url")
        val url: String?
    )

    companion object{
        const val UNKOWN_ID = -1//En caso de q no se encuentre el id...regresará -1 en el id
    }
}