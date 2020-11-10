package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.ui.fragment.IssueListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModalForIOErrors
import javafx.scene.layout.Priority
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class IssueListView : SuspendingView() {
    private val issueController by inject<IssueController>()
    private val ioErrorDebouncer = Debouncer()

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

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }
}