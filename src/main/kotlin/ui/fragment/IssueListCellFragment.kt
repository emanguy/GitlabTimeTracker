package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.ProgressStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingListCellFragment
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import tornadofx.*

class IssueListCellFragment : SuspendingListCellFragment<Issue>() {
    private var issue: Issue? = null
    private val idProperty = SimpleStringProperty("#0")
    private val issueTitleProperty = SimpleStringProperty("")
    private val createTimeProperty = SimpleStringProperty("")
    private val progressPercentageProperty = SimpleDoubleProperty(0.0)
    private val timeSummaryProperty = SimpleStringProperty("")
    private val shouldNotShowEstimateProperty = SimpleBooleanProperty(true)

    private val buttonText = SimpleStringProperty("Start")

    private var issueProgress by singleAssign<ProgressBar>()

    private val issueController by inject<IssueController>()
    private val timeRecordingController by inject<TimeRecordingController>()

    override val root = hbox {
        addClass(LayoutStyles.typicalSpacing)
        button(buttonText) {
            suspendingAction {
                val issueSnapshot = issue
                if (issueSnapshot != null && issueSnapshot == timeRecordingController.recordingIssueProperty.get()) {
                    buttonText.set("Stop")
                    timeRecordingController.startTiming(issueSnapshot)
                    // TODO take the result from the previous timing if it exists and tell the issue controller to record the time
                } else {
                    // Stop timing and tell the issue controller to record the time
                }
            }
        }

        vbox {
            hgrow = Priority.ALWAYS

            hbox {
                hyperlink(idProperty) {
                    action {
                        // Make the snapshot local to the listener so we can smartcast w/o worry of concurrent modification
                        val issueSnapshot = issue
                        if (issueSnapshot != null) {
                            hostServices.showDocument(issueSnapshot.url.toString())
                        }
                    }
                }
                text(issueTitleProperty)
            }
            hbox {
                text(createTimeProperty) {
                    addClass(TypographyStyles.subtitle)
                }
                text("|") {
                    addClass(TypographyStyles.subtitle)
                }
                text(timeSummaryProperty) {
                    addClass(TypographyStyles.subtitle)
                }
            }
        }

        issueProgress = progressbar(progressPercentageProperty) {
            hiddenWhen(shouldNotShowEstimateProperty)
        }
    }

    init {
        itemProperty.onChange {
            handleIssueUpdate(it)
        }
        timeRecordingController.recordingIssueProperty.onChange {

        }
    }

    private fun handleIssueUpdate(updatedIssue: Issue?) {
        if (updatedIssue == null) return

        issue = updatedIssue
        idProperty.set("#${updatedIssue.idInProject}")
        issueTitleProperty.set(updatedIssue.title)
        createTimeProperty.set("Created ${updatedIssue.creationTime.toString("MM/dd/yyyy")}")
        val timeSummaryFirstHalf = "Spent ${updatedIssue.timeSpent}"

        if (updatedIssue.timeEstimate != null) {
            val minutesEstimated = updatedIssue.timeEstimate.totalMinutes
            val minutesSpent = updatedIssue.timeSpent.totalMinutes.coerceAtMost(minutesEstimated)
            val percentComplete = minutesSpent.toDouble() / minutesEstimated.toDouble()

            timeSummaryProperty.set("$timeSummaryFirstHalf of estimated ${updatedIssue.timeEstimate}")
            progressPercentageProperty.set(percentComplete)
            shouldNotShowEstimateProperty.set(updatedIssue.timeSpent.totalMinutes == 0L)

            if (updatedIssue.timeSpent > updatedIssue.timeEstimate) {
                issueProgress.addClass(ProgressStyles.redBar)
            } else {
                issueProgress.removeClass(ProgressStyles.redBar)
            }
        } else {
            timeSummaryProperty.set(timeSummaryFirstHalf)
            progressPercentageProperty.set(0.0)
            shouldNotShowEstimateProperty.set(true)
        }
    }

    private fun handleRecordingStateChange(issueBeingRecorded: Issue?) {
        if (issueBeingRecorded != null && issueBeingRecorded.idInProject == this.issue?.idInProject) {
            buttonText.set("Stop")
        } else {
            buttonText.set("Start")
        }
    }
}