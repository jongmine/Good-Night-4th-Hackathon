package com.gn4.seatreservation.controller

import com.gn4.seatreservation.dto.ReserveRequest
import com.gn4.seatreservation.service.SeatService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class SeatController(
    private val seatService: SeatService,
) {
    // 좌석 목록 조회
    @GetMapping("/seats")
    fun list(
        @RequestHeader(name = "X-Client-Token", required = false)
        token: String?,
    ) = seatService.listSeats(token)

    // 좌석 HOLD
    @PostMapping("/seats/{id}/hold")
    fun hold(
        @PathVariable("id")
        id: Long,
        @RequestHeader("X-Client-Token")
        token: String,
    ) = seatService.hold(id, token)

    // HOLD 연장(heartbeat)
    @PostMapping("/seats/{id}/heartbeat")
    fun heartbeat(
        @PathVariable("id")
        id: Long,
        @RequestHeader("X-Client-Token")
        token: String,
    ) = seatService.heartbeat(id, token)

    // 예약 확정
    @PostMapping("/seats/{id}/reserve")
    fun reserve(
        @PathVariable("id")
        id: Long,
        @RequestHeader("X-Client-Token")
        token: String,
        @Valid @RequestBody
        body: ReserveRequest,
    ) = seatService.reserve(id, token, body)
}
