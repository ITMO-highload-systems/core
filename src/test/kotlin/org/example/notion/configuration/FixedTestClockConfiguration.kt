package org.example.notion.configuration

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.concurrent.Volatile

@TestConfiguration
class ClockTestConfiguration {

    @Bean
    fun fixedTestClock(): Clock {
        return TestClockProxy()
    }

    class TestClockProxy : Clock() {
        init {
            setToFixedClock()
        }

        override fun getZone(): ZoneId {
            return CLOCK!!.zone
        }

        override fun withZone(zone: ZoneId): Clock {
            return CLOCK!!.withZone(zone)
        }

        override fun instant(): Instant {
            return CLOCK!!.instant()
        }

        companion object {
            @Volatile
            private var CLOCK: Clock? = null

            fun setToFixedClock() {
                CLOCK = fixed(
                    Instant.now().minusSeconds(60L).truncatedTo(ChronoUnit.SECONDS),
                    ZoneId.systemDefault()
                )
            }

            fun setToSystemDefault() {
                CLOCK = systemDefaultZone()
            }
        }
    }
}