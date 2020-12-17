package edu.erittenhouse.gitlabtimetracker.controller.event

import edu.erittenhouse.gitlabtimetracker.model.Issue

sealed class TimeRecordingState {
    data class IssueRecording(val issue: Issue) : TimeRecordingState()
    object NoIssueRecording : TimeRecordingState()
}
