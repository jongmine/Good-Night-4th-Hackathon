package com.gn4.seatreservation.entity

enum class SeatStatus {
    AVAILABLE, // 예약 가능
    HELD, // 선점(우선권 부여)
    RESERVED, // 예약 확정
}
