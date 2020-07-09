package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import edu.erittenhouse.gitlabtimetracker.ui.fragment.IssueListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import javafx.scene.layout.Priority
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.javafx.asFlow
import kotlinx.coroutines.launch
import tornadofx.*

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class IssueListView : SuspendingIOSafeView() {
    private val issueController by inject<IssueController>()
    private var filterFieldFlow by singleAssign<Flow<String>>()
    private var milestoneFilterFlow by singleAssign<Flow<MilestoneFilterOption>>()

    override val root = vbox {
        style {
            maxWidth = 1000.px
            backgroundColor = multi(c("#FFFFFF"))
        }
        borderpane {
            top {
                hbox {
                    text("Filter issues: ")

                    hbox {
                        label("Issue name/number: ")
                        textfield {
                            filterFieldFlow = textProperty().asFlow().debounce(250)
                        }
                    }

                    hbox {
                        label("Milestone: ")
                        combobox<MilestoneFilterOption> {
                            items = issueController.milestoneFilterOptions
                            issueController.milestoneFilterOptions.onChange {
                                selectionModel.selectFirst()
                            }
                            milestoneFilterFlow = selectionModel.selectedItemProperty().asFlow().filterNotNull()
                        }
                    }
                }
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

    override fun onDock() {
        super.onDock()
        launch {
            filterFieldFlow.collect {
                issueController.filterByIssueText(it)
            }
        }
        launch {
            milestoneFilterFlow.collect {
                issueController.selectMilestoneFilterOption(it)
            }
        }
    }
}