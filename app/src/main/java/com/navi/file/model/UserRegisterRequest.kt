package com.navi.file.model

data class UserRegisterRequest(
    var userEmail: String,
    var userName: String,
    var userPassword: String
)
