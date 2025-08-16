package com.gn4.seatreservation.service

import com.gn4.seatreservation.config.ErrorCodes
import com.gn4.seatreservation.config.SeatConflictException
import com.gn4.seatreservation.dto.ReservationSummary
import com.gn4.seatreservation.dto.ReserveRequest
import com.gn4.seatreservation.dto.SeatDto
import com.gn4.seatreservation.entity.Seat
import com.gn4.seatreservation.entity.SeatStatus
import com.gn4.seatreservation.repository.ReservationRepository
import com.gn4.seatreservation.repository.SeatRepository
import com.gn4.seatreservation.sse.SeatSseBroadcaster
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class SeatServiceImpl(
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository,
    private val clock: Clock,
    private val broadcaster: SeatSseBroadcaster,
) : SeatService {
    companion object {
        // HOLD 유지 시간(초).
        private const val HOLD_TTL_SECONDS: Long = 90

        // HEARTBEAT로 연장하는 시간(초).
        private const val HEARTBEAT_EXTEND_SECONDS: Long = 60

        // 예약 의도적 실패 확률(1%).
        private const val INTENTIONAL_FAILURE_RATE: Double = 0.01
    }

    override fun listSeats(clientToken: String?): List<SeatDto> {
        val now = Instant.now(clock)
        return seatRepository.findAll().map { it.toDto(clientToken, now) }
    }

    @Transactional
    override fun hold(
        seatId: Long,
        clientToken: String,
    ): SeatDto {
        val now = Instant.now(clock)

        // 1. 만료된 HOLD 정리.
        seatRepository.releaseExpiredHolds(now)

        // 2. 좌석 조회. 없으면 404.
        val seat =
            seatRepository.findById(seatId).orElseThrow {
                NoSuchElementException("Seat($seatId) not found")
            }

        // 3. 이미 예약 완료면 409.
        if (seat.status == SeatStatus.RESERVED) {
            throw SeatConflictException(
                ErrorCodes.SEAT_ALREADY_RESERVED,
                "Seat($seatId) is already reserved.",
            )
        }

        // 4. 아직 만료되지 않은 HELD 상태.
        val notExpired = seat.holdExpiresAt?.isAfter(now) ?: false
        if (seat.status == SeatStatus.HELD && notExpired) {
            // 내가 잡은 HOLD면 멱등 처리.
            if (seat.holdToken == clientToken) {
                return seat.toDto(clientToken, now)
            }
            // 남이 잡은 HOLD면 409.
            throw SeatConflictException(
                ErrorCodes.SEAT_HELD_BY_OTHERS,
                "Seat($seatId) is held by another client.",
            )
        }

        // 5. AVAILABLE 이거나 HELD지만 만료된 경우 → 조건부 HOLD 시도(원자적 업데이트).
        val affected =
            seatRepository.tryHold(
                id = seatId,
                token = clientToken,
                expiresAt = now.plusSeconds(HOLD_TTL_SECONDS),
                nowTs = now,
            )

        if (affected == 1) {
            val updated =
                seatRepository.findById(seatId).orElseThrow {
                    NoSuchElementException("Seat($seatId) not found after hold")
                }
            val dto = updated.toDto(clientToken, now)
            broadcaster.broadcastSeatUpdate(dto) // ← 브로드캐스트
            return dto
        }

        // 6. 경쟁 상황. 최신 상태로 분기.
        val latest =
            seatRepository.findById(seatId).orElseThrow {
                NoSuchElementException("Seat($seatId) not found")
            }
        return when (latest.status) {
            SeatStatus.RESERVED -> throw SeatConflictException(
                ErrorCodes.SEAT_ALREADY_RESERVED,
                "Seat($seatId) just got reserved.",
            )

            SeatStatus.HELD -> throw SeatConflictException(
                ErrorCodes.SEAT_HELD_BY_OTHERS,
                "Seat($seatId) is held by another client.",
            )

            else -> throw IllegalStateException("Failed to hold seat($seatId) for unknown reason.")
        }
    }

    @Transactional
    override fun heartbeat(
        seatId: Long,
        clientToken: String,
    ): SeatDto {
        val now = Instant.now(clock)

        // 1. 좌석 조회.
        val seat =
            seatRepository.findById(seatId).orElseThrow {
                NoSuchElementException("Seat($seatId) not found")
            }

        // 2. 예약 완료 좌석이면 연장 불가.
        if (seat.status == SeatStatus.RESERVED) {
            throw SeatConflictException(
                ErrorCodes.SEAT_ALREADY_RESERVED,
                "Seat($seatId) is already reserved.",
            )
        }

        // 3. HELD가 아니거나 토큰이 불일치면 거부.
        if (seat.status != SeatStatus.HELD || seat.holdToken != clientToken) {
            throw SeatConflictException(
                ErrorCodes.NOT_HELD_BY_CLIENT,
                "Seat($seatId) is not held by this client.",
            )
        }

        // 4. 만료되었으면 거부.
        val notExpired = seat.holdExpiresAt?.isAfter(now) ?: false
        if (!notExpired) {
            throw SeatConflictException(
                ErrorCodes.HOLD_EXPIRED,
                "Hold for seat($seatId) has expired.",
            )
        }

        // 5. 연장. 단축되지 않도록 보장.
        val base = seat.holdExpiresAt?.let { if (it.isAfter(now)) it else now } ?: now
        seat.holdExpiresAt = base.plusSeconds(HEARTBEAT_EXTEND_SECONDS)
        seat.updatedAt = now

        val dto = seat.toDto(clientToken, now)
        broadcaster.broadcastSeatUpdate(dto)
        return dto
    }

    @Transactional
    override fun reserve(
        seatId: Long,
        clientToken: String,
        req: ReserveRequest,
    ): ReservationSummary {
        val now = Instant.now(clock)

        // 1. 좌석을 쓰기 락으로 조회.
        val seat =
            seatRepository.findByIdForUpdate(seatId).orElseThrow {
                NoSuchElementException("Seat($seatId) not found")
            }

        // 2. 상태/토큰/만료 검증.
        if (seat.status == SeatStatus.RESERVED) {
            throw SeatConflictException(
                ErrorCodes.SEAT_ALREADY_RESERVED,
                "Seat($seatId) is already reserved.",
            )
        }
        if (seat.status != SeatStatus.HELD || seat.holdToken != clientToken) {
            throw SeatConflictException(
                ErrorCodes.NOT_HELD_BY_CLIENT,
                "Seat($seatId) is not held by this client.",
            )
        }
        val notExpired = seat.holdExpiresAt?.isAfter(now) ?: false
        if (!notExpired) {
            throw SeatConflictException(
                ErrorCodes.HOLD_EXPIRED,
                "Hold for seat($seatId) has expired.",
            )
        }

        // 3. 1% 의도적 실패 처리. 좌석을 AVAILABLE로 되돌리고 에러.
        if (Math.random() < INTENTIONAL_FAILURE_RATE) {
            seat.status = SeatStatus.AVAILABLE
            seat.holdToken = null
            seat.holdExpiresAt = null
            seat.updatedAt = now
            broadcaster.broadcastSeatUpdate(seat.toDto(clientToken, now))
            throw SeatConflictException(
                ErrorCodes.INTENTIONAL_FAILURE,
                "Reservation failed due to simulated failure. Please try again.",
            )
        }

        // 4. 이중 방어. 이미 예약 레코드가 있으면 RESERVED로 간주.
        if (reservationRepository.existsBySeatId(seatId)) {
            seat.status = SeatStatus.RESERVED
            seat.reservedAt = seat.reservedAt ?: now
            seat.updatedAt = now
            broadcaster.broadcastSeatUpdate(seat.toDto(clientToken, now))
            return ReservationSummary(
                seatId = seatId,
                name = req.name,
                reservedAt = DateTimeFormatter.ISO_INSTANT.format(seat.reservedAt),
            )
        }

        // 5. 성공 처리. 좌석 RESERVED 전환 + 예약 레코드 생성.
        seat.status = SeatStatus.RESERVED
        seat.reservedAt = now
        seat.updatedAt = now

        reservationRepository.save(
            com.gn4.seatreservation.entity.Reservation(
                seatId = seatId,
                name = req.name,
                phone = req.phone,
                reservedAt = now,
            ),
        )

        broadcaster.broadcastSeatUpdate(seat.toDto(clientToken, now))
        return ReservationSummary(
            seatId = seatId,
            name = req.name,
            reservedAt = DateTimeFormatter.ISO_INSTANT.format(now),
        )
    }

    private fun Seat.toDto(
        clientToken: String?,
        now: Instant,
    ): SeatDto {
        val heldByMe =
            this.status == SeatStatus.HELD &&
                    this.holdToken != null &&
                    clientToken != null &&
                    this.holdToken == clientToken &&
                    (this.holdExpiresAt == null || this.holdExpiresAt!!.isAfter(now))

        return SeatDto(
            id = requireNotNull(this.id),
            label = this.label,
            status = this.status.name,
            holdExpiresAt = this.holdExpiresAt?.let { DateTimeFormatter.ISO_INSTANT.format(it) },
            heldByMe = heldByMe,
        )
    }
}
