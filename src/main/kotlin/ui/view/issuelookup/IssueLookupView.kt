package edu.erittenhouse.gitlabtimetracker.ui.view.issuelookup

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.TimeRecordingController
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class IssueLookupView : SuspendingView() {
    private val projectController by inject<ProjectController>()
    private val issueController by inject<IssueController>()
    private val timeRecordingController by inject<TimeRecordingController>()

    private val loadingMessage = SimpleStringProperty("")
    private val foundIssue = SimpleObjectProperty<Issue?>(null)

    override val root = hbox {
        text("Find issue to track") {
            addClass(TypographyStyles.title)
        }
        field("Issue number") {
            textfield {
                promptText = "1234"
            }
            button("Find issue") {

            }
        }
        separator()

        text(loadingMessage) {
            visibleWhen(loadingMessage.booleanBinding {  !it.isNullOrEmpty() } )
        }

    }
}