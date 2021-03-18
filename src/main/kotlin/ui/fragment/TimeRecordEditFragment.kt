package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.model.IssueWithTime
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingFragment
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class TimeRecordEditFragment : SuspendingFragment() {
    private val recordedTime: IssueWithTime by param()

    private val timeToRecord = SimpleStringProperty("")

    init {
        registerBackgroundTaskInit {
            timeToRecord.set(recordedTime.elapsedTime.toString())
        }
    }

    override val root = vbox {
        text("Edit time spent") {
            addClass(TypographyStyles.title)
        }

        form {
            fieldset {
                field {
                    textfield {
                        textProperty().bindBidirectional(timeToRecord)
                    }
                }
            }
        }
    }
}