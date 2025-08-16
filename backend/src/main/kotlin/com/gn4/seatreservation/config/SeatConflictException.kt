package com.gn4.seatreservation.config

class SeatConflictException(
    val code: String,
    override val message: String,
) : RuntimeException(message)
