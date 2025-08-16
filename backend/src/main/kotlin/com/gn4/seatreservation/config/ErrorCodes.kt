package com.gn4.seatreservation.config

object ErrorCodes {
    // 400
    const val BAD_REQUEST = "BAD_REQUEST"

    // 404
    const val NOT_FOUND = "NOT_FOUND"

    // 409
    const val SEAT_HELD_BY_OTHERS = "SEAT_HELD_BY_OTHERS"
    const val SEAT_ALREADY_RESERVED = "SEAT_ALREADY_RESERVED"
    const val HOLD_EXPIRED = "HOLD_EXPIRED"
    const val NOT_HELD_BY_CLIENT = "NOT_HELD_BY_CLIENT"
    const val INTENTIONAL_FAILURE = "INTENTIONAL_FAILURE"
}
