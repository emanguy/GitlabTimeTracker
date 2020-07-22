package edu.erittenhouse.gitlabtimetracker.model

import org.joda.time.Period

/**
 * Records a duration of time Gitlab-style. Base time unit is minutes, higher levels convert as follows:
 *
 * Minutes (m)
 * Hours (h) = 60m
 * Days (d) = 8h
 * Weeks (w) = 5d
 * Months (mo) = 4w
 */
data class TimeSpend(val totalMinutes: Long) {
    companion object {
        private const val MINUTES_IN_HOUR = 60
        private const val MINUTES_IN_DAY = 8 * MINUTES_IN_HOUR
        private const val MINUTES_IN_WEEK = 5 * MINUTES_IN_DAY
        private const val MINUTES_IN_MONTH = 4 * MINUTES_IN_WEEK

        val NONE = TimeSpend(0)

        fun fromPeriod(period: Period): TimeSpend {
            return TimeSpend(period.toStandardDuration().standardMinutes)
        }
        fun fromString(timeSpendStr: String): TimeSpend {
            val splitUpString = timeSpendStr.split(" ")
            val totalMinutes = splitUpString.map { convertToMinutes(it) }.reduce { total, value -> total + value }
            return TimeSpend(totalMinutes)
        }
        private fun convertToMinutes(timeComponent: String): Long {
            return when (timeComponent.last()) {
                'm' -> timeComponent.substring(0, timeComponent.lastIndex).toLong()
                'h' -> timeComponent.substring(0, timeComponent.lastIndex).toLong() * MINUTES_IN_HOUR
                'd' -> timeComponent.substring(0, timeComponent.lastIndex).toLong() * MINUTES_IN_DAY
                'w' -> timeComponent.substring(0, timeComponent.lastIndex).toLong() * MINUTES_IN_WEEK
                // In the case of "mo"
                'o' -> timeComponent.substring(0, timeComponent.lastIndex - 1).toLong() * MINUTES_IN_MONTH
                else -> 0
            }
        }
    }

    override fun toString() = buildString {
        if (totalMinutes == 0L) {
            append("0m")
            return@buildString
        }

        var remainingMinutes = totalMinutes
        var somethingAddedBeforeYou = false

        // See if we have more than a month
        if (remainingMinutes > MINUTES_IN_MONTH) {
            val numMonths = remainingMinutes / MINUTES_IN_MONTH
            append(numMonths)
            append("mo")
            remainingMinutes %= MINUTES_IN_MONTH
            somethingAddedBeforeYou = true
        }

        // See if we have more than a week
        if (remainingMinutes > MINUTES_IN_WEEK) {
            if (somethingAddedBeforeYou) append(" ")
            val numWeeks = remainingMinutes / MINUTES_IN_WEEK
            append(numWeeks)
            append("w")
            remainingMinutes %= MINUTES_IN_WEEK
            somethingAddedBeforeYou = true
        }

        // See if we have more than a day
        if (remainingMinutes > MINUTES_IN_DAY) {
            if (somethingAddedBeforeYou) append(" ")
            val numDays = remainingMinutes / MINUTES_IN_DAY
            append(numDays)
            append("d")
            remainingMinutes %= MINUTES_IN_DAY
            somethingAddedBeforeYou = true
        }

        // See if we have more than an hour
        if (remainingMinutes > MINUTES_IN_HOUR) {
            if (somethingAddedBeforeYou) append(" ")
            val numHours = remainingMinutes / MINUTES_IN_HOUR
            append(numHours)
            append("h")
            remainingMinutes %= MINUTES_IN_HOUR
            somethingAddedBeforeYou = true
        }

        // Print remaining minutes, if any
        if (remainingMinutes > 0) {
            if (somethingAddedBeforeYou) append(" ")
            append(remainingMinutes)
            append("m")
        }
    }

    operator fun plus(minutes: Long) = this.copy(totalMinutes = totalMinutes + minutes)
    operator fun plus(timeSpent: TimeSpend) = this.plus(timeSpent.totalMinutes)
    operator fun minus(minutes: Long) = this.plus(-minutes)
    operator fun minus(timeSpent: TimeSpend) = this.plus(-timeSpent.totalMinutes)

    operator fun compareTo(minutes: Long) = (this.totalMinutes - minutes).toInt()
    operator fun compareTo(other: TimeSpend) = this.compareTo(other.totalMinutes)
}

