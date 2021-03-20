@file:Suppress("PackageDirectoryMismatch", "ClassName")

package edu.erittenhouse.gitlabtimetracker.model

import org.joda.time.Period
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TimeSpendTest {
    @Test
    fun `All comparisons work`() {
        assertTrue(TimeSpend(15) > 3)
        assertTrue(TimeSpend(15) < 30)
        assertFalse(TimeSpend(15) > 30)
        assertFalse(TimeSpend(15) < 3)

        assertTrue(TimeSpend(15) >= 15)

        assertTrue(TimeSpend(15) > TimeSpend(3))
        assertTrue(TimeSpend(15) < TimeSpend(30))
        assertFalse(TimeSpend(15) > TimeSpend(30))
        assertFalse(TimeSpend(15) < TimeSpend(3))

        assertTrue(TimeSpend(15) >= TimeSpend(15))
    }

    @Test
    fun `Can be converted from a period`() {
        val testPeriod = Period(4, 5, 0, 0)
        val convertedTimeSpend = TimeSpend.fromPeriod(testPeriod)

        assertEquals(245L, convertedTimeSpend.totalMinutes)
        assertEquals("4h 5m", convertedTimeSpend.toString())

        val testWeirdConversionPeriod = Period(9, 10, 0, 0)
        val weirdConvertedTimeSpend = TimeSpend.fromPeriod(testWeirdConversionPeriod)

        assertEquals(550, weirdConvertedTimeSpend.totalMinutes)
        assertEquals("1d 1h 10m", weirdConvertedTimeSpend.toString())
    }

    @Nested
    inner class `Validation tests` {
        @Test
        fun `Validates valid time spend amounts`() {
            val validTimeSpends = listOf(
                "4m",
                "2h 3m",
                "3m 2h",
                "1mo 2w 3d 4h 5m",
            )

            validTimeSpends.forEach { timeSpendStr -> assertTrue(TimeSpend.isValidTimeSpend(timeSpendStr), "Failed value: $timeSpendStr") }
        }

        @Test
        fun `Marks invalid time spend amounts invalid`() {
            val invalidTimeSpends = listOf(
                "4z",
                "2h3m",
                "not valid time spend",
                "1om 2h",
                "3m ",
            )

            invalidTimeSpends.forEach { timeSpendStr -> assertFalse(TimeSpend.isValidTimeSpend(timeSpendStr), "Failed value: $timeSpendStr") }
        }

        @Test
        fun `Marks invalid time for valid format but duplicated units`() {
            val invalidTimeSpends = listOf(
                "4m 4m",
                "3h 4m 3h",
                "10mo 3d 4w 2d 3mo",
            )

            invalidTimeSpends.forEach { timeSpendStr -> assertFalse(TimeSpend.isValidTimeSpend(timeSpendStr), "Failed value: $timeSpendStr") }
        }
    }
}