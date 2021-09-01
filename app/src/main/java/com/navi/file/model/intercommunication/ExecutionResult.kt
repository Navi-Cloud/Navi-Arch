package com.navi.file.model.intercommunication

class ExecutionResult<T>(
    var resultType: ResultType,
    var value: T?,
    var message: String
)