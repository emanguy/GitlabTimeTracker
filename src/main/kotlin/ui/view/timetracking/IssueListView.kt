package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.ui.fragment.IssueListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import javafx.scene.layout.Priority
import tornadofx.*

class IssueListView : SuspendingIOSafeView() {
    private val issueController by inject<IssueController>()

    override val root = vbox {
        style {
            maxWidth = 1000.px
            backgroundColor = multi(c("#FFFFFF"))
        }
        borderpane {
            top {
                add(FilterBarView::class)
            }
            center {
                listview(issueController.issueList) {
                    cellFragment(IssueListCellFragment::class)
                    vgrow = Priority.ALWAYS
                }
                vgrow = Priority.ALWAYS
            }
        }
    }

}