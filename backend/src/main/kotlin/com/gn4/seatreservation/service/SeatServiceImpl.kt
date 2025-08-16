package com.gn4.seatreservation.service

import com.gn4.seatreservation.dto.ReservationSummary
import com.gn4.seatreservation.dto.ReserveRequest
import com.gn4.seatreservation.dto.SeatDto
import com.gn4.seatreservation.entity.Seat
import com.gn4.seatreservation.entity.SeatStatus
import com.gn4.seatreservation.repository.ReservationRepository
import com.gn4.seatreservation.repository.SeatRepository
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
) : SeatService {
    companion object {
        // HOLD 유지 시간(초)
        private const val HOLD_TTL_SECONDS: Long = 90

        // HEARTBEAT로 연장하는 시간(초)
        private const val HEARTBEAT_EXTEND_SECONDS: Long = 60

        // 예약 의도적 실패 확률(1%)
        private const val INTENTIONAL_FAILURE_RATE: Double = 0.01
    }

    override fun listSeats(clientToken: String?): List<SeatDto> {
        val now = Instant.now(clock)
        return seatRepository.findAll().map { it.toDto(clientToken, now) }
    }

    // 다음 단계(7~9단계)에서 트랜잭션 로직을 채운다.
    @Transactional
    override fun hold(
        seatId: Long,
        clientToken: String,
    ): SeatDto {
        error("Will be implemented in step 7 (HOLD logic)")
    }

    @Transactional
    override fun heartbeat(
        seatId: Long,
        clientToken: String,
    ): SeatDto {
        error("Will be implemented in step 8 (HEARTBEAT logic)")
    }

    @Transactional
    override fun reserve(
        seatId: Long,
        clientToken: String,
        req: ReserveRequest,
    ): ReservationSummary {
        error("Will be implemented in step 9 (RESERVE logic)")
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
