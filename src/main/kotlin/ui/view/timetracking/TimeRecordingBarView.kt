package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import tornadofx.*

class TimeRecordingBarView : SuspendingView() {
    private var stopButton by singleAssign<Button>()
    private val issueNameProperty = SimpleStringProperty("No issue being tracked")

    private val timeRecordingController by inject<TimeRecordingController>()
    private val issueController by inject<IssueController>()

    override val root = hbox {
        region {
            hgrow = Priority.ALWAYS
        }
        hbox {
            addClass(LayoutStyles.typicalSpacing)

            stopButton = button("Select issue") {
                isDisable = true
                suspendingAction {
                    val recordedTime = timeRecordingController.stopTiming()

                    if (recordedTime != null) {
                        issueController.recordTime(recordedTime)
                    }
                }
            }

            text(issueNameProperty)
            text(timeRecordingController.timeSpentProperty)
        }
        region {
            hgrow = Priority.ALWAYS
        }
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