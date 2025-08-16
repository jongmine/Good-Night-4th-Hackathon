package com.gn4.seatreservation.repository

import com.gn4.seatreservation.entity.Seat
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional

interface SeatRepository : JpaRepository<Seat, Long> {
    fun findByLabel(label: String): Seat?

    // 예약 확정 단계에서 좌석 행을 잠그고 읽고 싶을 때 사용
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Seat s where s.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: Long,
    ): Optional<Seat>

    /**
     * 조건부 HOLD 시도 — 네이티브 쿼리(간결/명시적)
     *  - 상태가 AVAILABLE이거나, HELD지만 만료된 경우에만 HELD로 전환
     *  - 반환값은 영향받은 행 수(1: 성공, 0: 실패)
     */
    @Modifying(clearAutomatically = true)
    @Query(
        value = """
        UPDATE seat
           SET status = :heldStatus,
               hold_token = :token,
               hold_expires_at = :expiresAt,
               updated_at = :nowTs
         WHERE id = :id
           AND (
             status = :availableStatus
             OR (status = :heldStatus AND (hold_expires_at IS NULL OR hold_expires_at <= :nowTs))
           )
        """,
        nativeQuery = true,
    )
    fun tryHold(
        @Param("id") id: Long,
        @Param("token") token: String,
        @Param("expiresAt") expiresAt: Instant,
        @Param("nowTs") nowTs: Instant,
        // 상태는 문자열로 전달 (nativeQuery)
        @Param("availableStatus") availableStatus: String = "AVAILABLE",
        @Param("heldStatus") heldStatus: String = "HELD",
    ): Int

    /**
     * 만료된 HOLD를 즉시 해제(AVAILABLE로)
     */
    @Modifying(clearAutomatically = true)
    @Query(
        value = """
        UPDATE seat
           SET status = :availableStatus,
               hold_token = NULL,
               hold_expires_at = NULL,
               updated_at = :nowTs
         WHERE status = :heldStatus
           AND hold_expires_at IS NOT NULL
           AND hold_expires_at <= :nowTs
        """,
        nativeQuery = true,
    )
    fun releaseExpiredHolds(
        @Param("nowTs") nowTs: Instant,
        @Param("availableStatus") availableStatus: String = "AVAILABLE",
        @Param("heldStatus") heldStatus: String = "HELD",
    ): Int
}
