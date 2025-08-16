package com.gn4.seatreservation.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "reservation",
    uniqueConstraints = [UniqueConstraint(name = "uk_reservation_seat", columnNames = ["seat_id"])],
)
class Reservation(
    // 예약 PK
    @Id
    val id: UUID = UUID.randomUUID(),
    // 좌석 ID (FK)
    @Column(name = "seat_id", nullable = false)
    val seatId: Long,
    // 예약자 이름
    @Column(nullable = false, length = 50)
    val name: String,
    // 예약자 연락처
    @Column(nullable = false, length = 20)
    val phone: String,
    // 예약 시각
    @Column(name = "reserved_at", nullable = false)
    val reservedAt: Instant = Instant.now(),
    // 생성 시각
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)
