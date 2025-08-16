package com.gn4.seatreservation.controller

import com.gn4.seatreservation.service.SeatService
import com.gn4.seatreservation.sse.SeatSseBroadcaster
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
class SeatStreamController(
    private val seatService: SeatService,
    private val broadcaster: SeatSseBroadcaster,
) {
    @GetMapping("/stream/seats", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSeats(
        @RequestHeader(name = "X-Client-Token", required = false)
        token: String?,
    ): SseEmitter {
        val emitter = broadcaster.register()

        // 초기 스냅샷을 한 번 보내 사용자 UI가 현재 상태를 바로 반영할 수 있게 함.
        seatService.listSeats(token).forEach { dto ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("seat_update")
                        .data(dto),
                )
            } catch (_: Exception) {
                // 초기 전송 실패 시에도 emitter는 브로드캐스터가 관리하므로 별도 처리 없이 반환
            }
        }
        // 간단 keepalive 한 번 전송
        try {
            emitter.send(SseEmitter.event().name("ping").data("hello"))
        } catch (_: Exception) {
        }

        return emitter
    }
}
