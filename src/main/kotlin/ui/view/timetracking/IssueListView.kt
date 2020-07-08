package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.ui.fragment.IssueListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import javafx.scene.layout.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.javafx.asFlow
import kotlinx.coroutines.launch
import tornadofx.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class IssueListView : SuspendingIOSafeView() {
    private val issueController by inject<IssueController>()
    private var filterFieldFlow by singleAssign<Flow<String>>()

    override val root = vbox {
        style {
            maxWidth = 1000.px
            backgroundColor = multi(c("#FFFFFF"))
        }
        textfield {
            filterFieldFlow = textProperty().asFlow().debounce(250)
        }
        listview(issueController.issueList) {
            cellFragment(IssueListCellFragment::class)
            vgrow = Priority.ALWAYS
        }
    }

    override fun onDock() {
        super.onDock()
        launch {
            filterFieldFlow.collect {
                issueController.filterByIssueText(it)
            }
        }
    }
}