package com.example

import kotlinx.serialization.Serializable

@Serializable
data class Image (
    val id:Int,
    val link:String,
)