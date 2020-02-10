package edu.erittenhouse.gitlabtimetracker.model

import org.joda.time.Period
import org.junit.jupiter.api.Assertions.*
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
}