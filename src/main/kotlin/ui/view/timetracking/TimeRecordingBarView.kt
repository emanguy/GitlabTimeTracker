package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.controller.event.TimeRecordingState
import edu.erittenhouse.gitlabtimetracker.controller.result.RecordingStopResult
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.control.Button
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class TimeRecordingBarView : SuspendingView() {
    private var stopButton by singleAssign<Button>()
    private val issueNameProperty = SimpleStringProperty("No issue being tracked")

    private val timeRecordingController by inject<TimeRecordingController>()
    private val issueController by inject<IssueController>()
    private val ioErrorDebouncer = Debouncer()

    init {
        registerCoroutineExceptionHandler { _, exception ->
            ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
        }
    }
    override val root = hbox {
            addClass(LayoutStyles.typicalPaddingAndSpacing)
            alignment = Pos.CENTER

            stopButton = button("Select issue") {
                isDisable = true
                imageview(Images.stopAndSubmit)

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
                        is TimeRecordResult.TimeRecorded, is TimeRecordResult.NegligibleTime -> { /* Good to go! */ }
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
        registerBackgroundTaskInit {
            launch {
                timeRecordingController.recordingIssueState.collect {
                    handleRecordingIssueUpdate(it)
                }
            }
        }
    }

    private fun handleRecordingIssueUpdate(recordingState: TimeRecordingState) {
        when (recordingState) {
            is TimeRecordingState.IssueRecording -> {
                stopButton.text = "Stop and submit"
                stopButton.isDisable = false
                issueNameProperty.set("Tracking: ${recordingState.issue.title}")
            }
            is TimeRecordingState.NoIssueRecording -> {
                stopButton.text = "Select issue"
                stopButton.isDisable = true
                issueNameProperty.set("No issue being tracked")
            }
        }
    }
}