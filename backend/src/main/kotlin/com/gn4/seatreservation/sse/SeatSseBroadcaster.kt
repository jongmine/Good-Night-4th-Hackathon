package com.gn4.seatreservation.sse

import com.gn4.seatreservation.dto.SeatDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

@Component
class SeatSseBroadcaster {
    private val log = LoggerFactory.getLogger(SeatSseBroadcaster::class.java)
    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun register(timeoutMillis: Long = TimeUnit.MINUTES.toMillis(5)): SseEmitter {
        val emitter = SseEmitter(timeoutMillis)
        emitters.add(emitter)

        emitter.onCompletion { emitters.remove(emitter) }
        emitter.onTimeout { emitters.remove(emitter) }
        emitter.onError { emitters.remove(emitter) }

        return emitter
    }

    fun broadcastSeatUpdate(dto: SeatDto) {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("seat_update")
                        .data(dto),
                )
            } catch (ex: IOException) {
                dead.add(emitter)
            } catch (ex: IllegalStateException) {
                dead.add(emitter)
            }
        }
        if (dead.isNotEmpty()) {
            emitters.removeAll(dead)
            log.debug("Removed {} dead SSE emitters.", dead.size)
        }
    }

    fun broadcastPing() {
        val dead = mutableListOf<SseEmitter>()
        emitters.forEach { emitter ->
            try {
                emitter.send(
                    SseEmitter.event()
                        .name("ping")
                        .data("keepalive"),
                )
            } catch (_: Exception) {
                dead.add(emitter)
            }
        }
        if (dead.isNotEmpty()) emitters.removeAll(dead)
    }
}
