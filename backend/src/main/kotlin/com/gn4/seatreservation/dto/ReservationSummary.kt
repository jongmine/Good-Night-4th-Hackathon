package com.gn4.seatreservation.dto

data class ReservationSummary(
    // 좌석 ID
    val seatId: Long,
    // 이름
    val name: String,
    // 예약 시각 (ISO-8601)
    val reservedAt: String,
)
