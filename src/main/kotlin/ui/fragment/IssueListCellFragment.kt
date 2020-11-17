package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.controller.result.RecordingStopResult
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.ProgressStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingListCellFragment
import javafx.scene.control.ProgressBar
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class IssueListCellFragment : SuspendingListCellFragment<Issue>() {
    private val ioErrorDebouncer = Debouncer()

    private val idProperty = stringBinding(itemProperty) { "#${value?.idInProject}"}
    private val issueTitleProperty = stringBinding(itemProperty) { value?.title ?: "" }
    private val createTimeProperty = stringBinding(itemProperty) {
        val issueSnapshot = value

        return@stringBinding if (issueSnapshot != null) {
            "Created ${issueSnapshot.creationTime.toString("MM/dd/yyyy")}"
        } else {
            ""
        }
    }
    private val progressPercentageProperty = doubleBinding(itemProperty) {
        val issueSnapshot = value
        if (issueSnapshot?.timeEstimate == null) return@doubleBinding 0.0

        val minutesEstimated = issueSnapshot.timeEstimate.totalMinutes
        val minutesSpent = issueSnapshot.timeSpent.totalMinutes.coerceAtMost(minutesEstimated)
        return@doubleBinding minutesSpent.toDouble() / minutesEstimated.toDouble()
    }
    private val timeSummaryProperty = stringBinding(itemProperty) {
        if (value == null) return@stringBinding ""

        val timeSummaryFirstHalf = "Spent ${value.timeSpent}"

        return@stringBinding if (value.timeEstimate != null) {
            "$timeSummaryFirstHalf of estimated ${value.timeEstimate}"
        } else {
            timeSummaryFirstHalf
        }
    }
    private val shouldNotShowEstimateProperty = booleanBinding(itemProperty) {
        val issueSnapshot = value

        return@booleanBinding if (issueSnapshot != null) {
            issueSnapshot.timeEstimate == null || issueSnapshot.timeSpent.totalMinutes == 0L
        } else {
            true
        }
    }

    private var issueProgress by singleAssign<ProgressBar>()

    private val issueController by inject<IssueController>()
    private val timeRecordingController by inject<TimeRecordingController>()

    private val buttonText = stringBinding(itemProperty, timeRecordingController.recordingIssueProperty) {
        val currentIssue = value
        val currentlyRecordingIssue = timeRecordingController.recordingIssueProperty.value

        return@stringBinding if (currentIssue != null && currentIssue.idInProject == currentlyRecordingIssue?.idInProject) {
            "Stop"
        } else {
            "Start"
        }
    }

    override val root = hbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)
        button(buttonText) {
            suspendingAction {
                val issueSnapshot = item
                val toRecord = if (issueSnapshot != null && issueSnapshot != timeRecordingController.recordingIssueProperty.get()) {
                    timeRecordingController.startTiming(issueSnapshot)
                } else {
                    timeRecordingController.stopTiming()
                }

                val timeRecordingResult = when (toRecord) {
                    is RecordingStopResult.StoppedTiming -> issueController.recordTime(toRecord.issueWithTime)
                    is RecordingStopResult.NoIssueBeingRecorded -> return@suspendingAction
                    is RecordingStopResult.RecorderUnresponsive -> {
                        showErrorModal("Couldn't stop the recording of the issue. " +
                                "Please close the app and reopen it if there are further issues.")
                        return@suspendingAction
                    }
                }
                when (timeRecordingResult) {
                    is TimeRecordResult.TimeRecorded, is TimeRecordResult.NegligibleTime -> { /* Good to go! */ }
                    is TimeRecordResult.NoCredentials -> showErrorModal("Something's wrong, the app couldn't " +
                            "read your credentials when trying to record the time you spent." +
                            "You may want to manually record your time spent on the issue: /spend ${toRecord.issueWithTime.elapsedTime}")
                    is TimeRecordResult.TimeFailedToRecord -> showErrorModal("Something's wrong, GitLab didn't accept the " +
                            "amount of time you spent on that issue. You may want to manually record your time spent on the issue: " +
                            "/spend ${toRecord.issueWithTime.elapsedTime}")
                }
            }
        }

        vbox {
            hgrow = Priority.ALWAYS

            hbox {
                addClass(LayoutStyles.typicalSpacing)

                hyperlink(idProperty) {
                    action {
                        // Make the snapshot local to the listener so we can smart cast w/o worry of concurrent modification
                        val issueSnapshot = item
                        if (issueSnapshot != null) {
                            hostServices.showDocument(issueSnapshot.url.toString())
                        }
                    }
                }
                text(issueTitleProperty)
            }
            hbox {
                addClass(LayoutStyles.typicalSpacing)

                text(createTimeProperty) {
                    addClass(TypographyStyles.metadata)
                }
                text("|") {
                    addClass(TypographyStyles.metadata)
                }
                text(timeSummaryProperty) {
                    addClass(TypographyStyles.metadata)
                }
            }
        }

        issueProgress = progressbar(progressPercentageProperty) {
            hiddenWhen(shouldNotShowEstimateProperty)

        }
    }

    init {
        itemProperty.onChange {
            root.isVisible = it != null
            updateProgress(it)
        }
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }

    private fun updateProgress(updatedIssue: Issue?) {
        if (updatedIssue?.timeEstimate != null) {
            if (updatedIssue.timeSpent > updatedIssue.timeEstimate) {
                issueProgress.addClass(ProgressStyles.redBar)
            } else {
                issueProgress.removeClass(ProgressStyles.redBar)
            }
        }
    }
}