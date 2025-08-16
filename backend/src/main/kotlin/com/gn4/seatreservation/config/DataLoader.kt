package com.gn4.seatreservation.config

import com.gn4.seatreservation.entity.Seat
import com.gn4.seatreservation.entity.SeatStatus
import com.gn4.seatreservation.repository.SeatRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataLoader {
    private val log = LoggerFactory.getLogger(DataLoader::class.java)

    @Bean
    fun seedSeats(seatRepository: SeatRepository) =
        ApplicationRunner {
            // 이미 존재하면 스킵
            if (seatRepository.count() > 0) {
                log.info("Seat seeding skipped (existing count={})", seatRepository.count())
                return@ApplicationRunner
            }

            val labels =
                buildList {
                    listOf("A", "B", "C").forEach { row ->
                        (1..3).forEach { col ->
                            add("$row$col")
                        }
                    }
                }

            val seats =
                labels.map { label ->
                    Seat(
                        id = null,
                        label = label,
                        status = SeatStatus.AVAILABLE,
                        holdToken = null,
                        holdExpiresAt = null,
                        reservedAt = null,
                    )
                }

            seatRepository.saveAll(seats)
            log.info("Seeded {} seats: {}", seats.size, labels)
        }
}
