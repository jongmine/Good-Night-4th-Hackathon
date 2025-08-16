package com.gn4.seatreservation.dto

data class SeatDto(
    // 좌석 ID
    val id: Long,
    // 좌석 라벨
    val label: String,
    // 좌석 상태
    val status: String,
    // 선점 만료 시각 (ISO-8601)
    val holdExpiresAt: String?,
    // 내가 선점했는지
    val heldByMe: Boolean,
)
