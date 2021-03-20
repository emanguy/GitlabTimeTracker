package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.result.TimeRecordResult
import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.model.TimeSpend
import edu.erittenhouse.gitlabtimetracker.ui.style.FormStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingFragment
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.TextField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.javafx.asFlow
import kotlinx.coroutines.launch
import tornadofx.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TimeRecordEditFragment : SuspendingFragment() {
    private val recordedTime: IssueWithTime by param()
    private val issueController: IssueController by inject()

    private var timeToRecordField by singleAssign<TextField>()
    private val timeNotValid = SimpleBooleanProperty(false)

    init {
        registerBackgroundTaskInit {
            timeToRecordField.textProperty().set(recordedTime.elapsedTime.toString())
        }

        timeNotValid.onChange { invalid ->
            if (invalid) {
                timeToRecordField.addClass(FormStyles.fieldInvalidBorder)
            } else {
                timeToRecordField.removeClass(FormStyles.fieldInvalidBorder)
            }
        }

        registerBackgroundTaskInit {
            launch {
                timeToRecordField.textProperty().asFlow()
                    .debounce(500)
                    .collect {  timeSpendStr ->
                        timeNotValid.set(!TimeSpend.isValidTimeSpend(timeSpendStr))
                    }
            }
        }
    }

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text("Edit time spent") {
            addClass(TypographyStyles.title)
        }

        form {
            fieldset {
                field {
                    timeToRecordField = textfield {
                        promptText = "Time spent, i.e. 1mo 2w 3d 4h 5m"
                    }
                }
                text("""The entered time spent is not in a valid format. 
                    |Make sure each measurement is separated by spaces and the units are both valid and not duplicated.""".trimMargin()) {
                    addClass(TypographyStyles.errorText)
                    visibleWhen(timeNotValid)
                }

                hbox {
                    addClass(LayoutStyles.typicalSpacing)

                    button("Submit time spent") {
                        disableWhen(timeNotValid)
                        imageview(Images.submit)

                        suspendingAction {
                            val timeSpentStr = timeToRecordField.textProperty().get()
                            val timeSpent = TimeSpend.fromString(timeSpentStr)
                            val issueWithUpdatedTime = recordedTime.copy(elapsedTime = timeSpent)

                            when (issueController.recordTime(issueWithUpdatedTime)) {
                                is TimeRecordResult.NegligibleTime, is TimeRecordResult.TimeRecorded -> {
                                    close()
                                }
                                is TimeRecordResult.TimeFailedToRecord -> {
                                    showErrorModal(
                                        "Failed to record your spent time to GitLab. You may want to try this slash command on " +
                                                "issue #${recordedTime.issue.idInProject} on GitLab: /spend $timeSpentStr"
                                    ) {
                                        close()
                                    }
                                }
                                is TimeRecordResult.NoCredentials -> {
                                    showErrorModal(
                                        "Something went very wrong. we managed to lose your GitLab credentials. To avoid losing " +
                                                "your spent time, run the following slash command on issue #${recordedTime.issue.idInProject} on Gitlab: /spend $timeSpentStr"
                                    ) {
                                        close()
                                    }
                                }
                            }
                        }
                    }

                    button("Cancel") {
                        action {
                            close()
                        }
                    }
                }
            }
        }
    }
}