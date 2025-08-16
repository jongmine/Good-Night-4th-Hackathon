package com.gn4.seatreservation.service

import com.gn4.seatreservation.dto.ReservationSummary
import com.gn4.seatreservation.dto.ReserveRequest
import com.gn4.seatreservation.dto.SeatDto

interface SeatService {
    fun listSeats(clientToken: String?): List<SeatDto>

    fun hold(
        seatId: Long,
        clientToken: String,
    ): SeatDto

    fun heartbeat(
        seatId: Long,
        clientToken: String,
    ): SeatDto

    fun reserve(
        seatId: Long,
        clientToken: String,
        req: ReserveRequest,
    ): ReservationSummary
}
