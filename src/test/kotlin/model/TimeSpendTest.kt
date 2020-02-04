package edu.erittenhouse.gitlabtimetracker.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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
}