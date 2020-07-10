package edu.erittenhouse.gitlabtimetracker.controller.result

import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime

sealed class RecordingStopResult {
    data class StoppedTiming(val issueWithTime: IssueWithTime) : RecordingStopResult()
    object NoIssueBeingRecorded : RecordingStopResult()
    object RecorderUnresponsive : RecordingStopResult()
}