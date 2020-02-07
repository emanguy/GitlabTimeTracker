package edu.erittenhouse.gitlabtimetracker.model

import org.joda.time.Period

data class IssueWithTime(val issue: Issue, val elapsedTime: Period)