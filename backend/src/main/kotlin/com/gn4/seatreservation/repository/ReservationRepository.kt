package com.gn4.seatreservation.repository

import com.gn4.seatreservation.entity.Reservation
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ReservationRepository : JpaRepository<Reservation, UUID> {
    fun existsBySeatId(seatId: Long): Boolean

    fun findBySeatId(seatId: Long): Reservation?
}
