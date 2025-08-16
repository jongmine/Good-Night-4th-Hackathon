package com.gn4.seatreservation.dto

data class ErrorResponse(
    // 에러 코드
    val code: String,
    // 에러 메시지
    val message: String,
)
