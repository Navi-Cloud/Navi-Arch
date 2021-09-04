package com.navi.file.helper

import java.util.regex.Pattern

object FormValidator {
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

    fun validateModel(email: String, password: String): Boolean  {
        return emailPattern.matcher(email).matches() && passwordPattern.matcher(password).matches()
    }
}