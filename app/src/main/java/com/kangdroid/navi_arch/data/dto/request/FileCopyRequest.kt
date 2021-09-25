package com.kangdroid.navi_arch.data.dto.request

class FileCopyRequest(
    var fromToken: String, // 원본 파일의 토큰
    var fromPrevToken: String, // 원본 파일이 들어있는 폴더의 토큰
    var toPrevToken: String, // 복사하고싶은 폴더의 토큰
    var newFileName: String // 새로운 파일 이름[같아도 되지만, null은 안됨]
)