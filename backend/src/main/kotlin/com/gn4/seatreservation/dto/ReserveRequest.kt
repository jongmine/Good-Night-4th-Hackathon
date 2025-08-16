package com.gn4.seatreservation.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReserveRequest(
    // 예약자 이름
    @field:NotBlank
    @field:Size(min = 1, max = 50)
    val name: String,
    // 예약자 연락처
    @field:NotBlank
    @field:Size(min = 3, max = 20)
    val phone: String,
)
