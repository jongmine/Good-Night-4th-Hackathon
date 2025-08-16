package com.gn4.seatreservation.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gn4.seatreservation.dto.ReserveRequest
import com.gn4.seatreservation.dto.SeatDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
class SeatControllerIntegrationTest {
    @TestConfiguration
    internal class ControllerTestConfig {
        @Bean
        @Primary
        fun clock(): Clock = Mockito.mock(Clock::class.java)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var clock: Clock

    private val objectMapper = jacksonObjectMapper()

    @Test
    @DisplayName("좌석 목록 조회 API는 정상적으로 200 OK를 반환한다")
    fun `listSeats returns 200 OK`() {
        // given
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))

        // Act & Assert
        mockMvc.get("/api/seats")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    @DisplayName("좌석 선점과 예약이 순차적으로 성공한다")
    fun `hold and reserve seat successfully`() {
        // given
        val clientToken = UUID.randomUUID().toString()
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))

        // 1. 좌석 목록을 조회해서 AVAILABLE 상태인 좌석을 찾는다.
        val availableSeat = findAvailableSeat()

        // 2. 좌석을 선점(hold)한다.
        mockMvc.post("/api/seats/{id}/hold", availableSeat.id) {
            header("X-Client-Token", clientToken)
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("HELD") }
            jsonPath("$.heldByMe") { value(true) }
        }

        // 3. 좌석을 예약(reserve)한다.
        val reservationRequest = ReserveRequest("Jongmin", "010-1234-5678")
        mockMvc.post("/api/seats/{id}/reserve", availableSeat.id) {
            header("X-Client-Token", clientToken)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reservationRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.seatId") { value(availableSeat.id) }
            jsonPath("$.name") { value(reservationRequest.name) }
        }

        // 4. 최종적으로 좌석 상태가 RESERVED로 변경되었는지 확인한다.
        val finalSeatStatus = getSeatStatus(availableSeat.id)
        assertEquals("RESERVED", finalSeatStatus)
    }

    @Test
    @DisplayName("다른 사용자가 선점한 좌석은 선점할 수 없으며 409 Conflict를 반환한다")
    fun `hold fails when seat is already held by another client`() {
        // given
        val clientTokenA = "client-A"
        val clientTokenB = "client-B"
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))

        // 1. 좌석 목록을 조회해서 AVAILABLE 상태인 좌석을 찾는다.
        val availableSeat = findAvailableSeat()

        // 2. 사용자 A가 좌석을 선점(hold)한다.
        mockMvc.post("/api/seats/{id}/hold", availableSeat.id) {
            header("X-Client-Token", clientTokenA)
        }.andExpect {
            status { isOk() }
        }

        // 3. 사용자 B가 동일한 좌석을 선점하려고 시도하면 409 Conflict가 발생한다.
        mockMvc.post("/api/seats/{id}/hold", availableSeat.id) {
            header("X-Client-Token", clientTokenB)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    @DisplayName("선점하지 않은 좌석은 예약할 수 없으며 409 Conflict를 반환한다")
    fun `reserve fails when seat is not held`() {
        // given
        val clientToken = "some-client"
        whenever(clock.instant()).thenReturn(Instant.now())
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))

        // 1. 좌석 목록을 조회해서 AVAILABLE 상태인 좌석을 찾는다.
        val availableSeat = findAvailableSeat()

        // 2. 선점(hold) 과정 없이 바로 예약을 시도하면 409 Conflict가 발생한다.
        val reservationRequest = ReserveRequest("Jongmin", "010-1234-5678")
        mockMvc.post("/api/seats/{id}/reserve", availableSeat.id) {
            header("X-Client-Token", clientToken)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reservationRequest)
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    @DisplayName("선점이 만료된 좌석은 예약할 수 없으며 409 Conflict를 반환한다")
    fun `reserve fails when hold is expired`() {
        val clientToken = "some-client"
        val now = Instant.parse("2025-01-01T12:00:00Z")
        whenever(clock.instant()).thenReturn(now)
        whenever(clock.zone).thenReturn(ZoneId.of("UTC"))

        // 1. 좌석을 선점한다. 만료 시각은 now + 90초로 설정된다.
        val availableSeat = findAvailableSeat()
        mockMvc.post("/api/seats/{id}/hold", availableSeat.id) {
            header("X-Client-Token", clientToken)
        }.andExpect {
            status { isOk() }
        }

        // 2. 시간을 만료 시각 이후로 돌린다. (90초가 TTL)
        val expiredTime = now.plusSeconds(100)
        whenever(clock.instant()).thenReturn(expiredTime)

        // 3. 만료된 선점으로 예약을 시도하면 409 Conflict가 발생한다.
        val reservationRequest = ReserveRequest("Jongmin", "010-1234-5678")
        mockMvc.post("/api/seats/{id}/reserve", availableSeat.id) {
            header("X-Client-Token", clientToken)
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(reservationRequest)
        }.andExpect {
            status { isConflict() }
        }
    }

    private fun findAvailableSeat(): SeatDto {
        val result = mockMvc.get("/api/seats").andReturn()
        val seats = objectMapper.readValue<List<SeatDto>>(result.response.contentAsString)
        return seats.first { it.status == "AVAILABLE" }
    }

    private fun getSeatStatus(seatId: Long): String {
        val result = mockMvc.get("/api/seats").andReturn()
        val seats = objectMapper.readValue<List<SeatDto>>(result.response.contentAsString)
        return seats.first { it.id == seatId }.status
    }
}
