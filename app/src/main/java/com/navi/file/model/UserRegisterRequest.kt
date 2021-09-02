package com.navi.file.model

import android.util.Patterns
import java.util.regex.Pattern

data class UserRegisterRequest(
    var userEmail: String,
    var userName: String,
    var userPassword: String
) {
    private val emailPattern: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    private val passwordPattern: Pattern = Pattern.compile(
        "^" +
                "(?=.*[@#$%^&+=])" +
                "(?=\\S+$)" +
                ".{8,}" +
                "$"
    )

    fun validateModel(): Boolean = emailPattern.matcher(userEmail).matches() && passwordPattern.matcher(userPassword).matches()
}
