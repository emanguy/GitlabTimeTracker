package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.controller.result.RecordingStopResult
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModal
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import tornadofx.*

class TimeRecordingBarView : SuspendingIOSafeView() {
    private var stopButton by singleAssign<Button>()
    private val issueNameProperty = SimpleStringProperty("No issue being tracked")

    private val timeRecordingController by inject<TimeRecordingController>()
    private val issueController by inject<IssueController>()

    override val root = hbox {
            addClass(LayoutStyles.typicalPaddingAndSpacing)
            alignment = Pos.CENTER

            stopButton = button("Select issue") {
                isDisable = true
                suspendingAction {
                    val recordResult = timeRecordingController.stopTiming()
                    val timeSubmitResult = when (recordResult) {
                        is RecordingStopResult.StoppedTiming -> issueController.recordTime(recordResult.issueWithTime)
                        is RecordingStopResult.NoIssueBeingRecorded -> return@suspendingAction
                        is RecordingStopResult.RecorderUnresponsive -> {
                            showErrorModal("Something's wrong. We couldn't properly stop the issue recording. " +
                                    "If further issues occur, please restart the app.")
                            return@suspendingAction
                        }
                    }
                    when (timeSubmitResult) {
                        is TimeRecordResult.TimeRecorded -> { /* Good to go! */ }
                        is TimeRecordResult.TimeFailedToRecord -> showErrorModal("GitLab didn't accept the time you spent on the issue. " +
                                "If you'd like to keep the amount of time you spent, try running this slash command on the issue: " +
                                "/spend ${recordResult.issueWithTime.elapsedTime}")
                        is TimeRecordResult.NoCredentials -> showErrorModal("Something's wrong. The app couldn't read your credentials while " +
                                "trying to submit the time you spent on the issue. If you'd like to keep the amount of time you spent, try running " +
                                "this slash command on the issue: /spend ${recordResult.issueWithTime.elapsedTime}")
                    }
                }
            }

            text(issueNameProperty)
            text(timeRecordingController.timeSpentProperty)
    }

    init {
        timeRecordingController.recordingIssueProperty.onChange {
            handleRecordingIssueUpdate(it)
        }
    }

    private fun handleRecordingIssueUpdate(issue: Issue?) {
        if (issue == null) {
            stopButton.text = "Select issue"
            stopButton.isDisable = true
            issueNameProperty.set("No issue being tracked")
        } else {
            stopButton.text = "Stop and submit"
            stopButton.isDisable = false
            issueNameProperty.set("Tracking: ${issue.title}")
        }
    }
}