package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import tornadofx.*

class IssueListView : SuspendingView() {
    override val root = vbox {
        style {
            maxWidth = 500.px
            backgroundColor = multi(c("#FFFFFF"))
        }
        text("This is the list view!")
    }
}