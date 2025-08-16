package com.gn4.seatreservation.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "seat",
    uniqueConstraints = [UniqueConstraint(name = "uk_seat_label", columnNames = ["label"])],
    indexes = [
        Index(name = "idx_seat_status", columnList = "status"),
        Index(name = "idx_seat_hold_expires_at", columnList = "hold_expires_at"),
    ],
)
class Seat(
    // 좌석 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    // 좌석 라벨 (예: A1)
    @Column(nullable = false, length = 8)
    val label: String,
    // 좌석 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var status: SeatStatus = SeatStatus.AVAILABLE,
    // 선점 토큰
    @Column(name = "hold_token", length = 64)
    var holdToken: String? = null,
    // 선점 만료 시각
    @Column(name = "hold_expires_at")
    var holdExpiresAt: Instant? = null,
    // 예약 확정 시각
    @Column(name = "reserved_at")
    var reservedAt: Instant? = null,
    // 생성 시각
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
    // 수정 시각
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),
)
