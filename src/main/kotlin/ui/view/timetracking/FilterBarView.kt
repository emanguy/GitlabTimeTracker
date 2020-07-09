package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
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
class FilterBarView : SuspendingIOSafeView() {
    private val issueController by inject<IssueController>()
    private var filterFieldFlow by singleAssign<Flow<String>>()
    private var milestoneFilterFlow by singleAssign<Flow<MilestoneFilterOption>>()

    override val root = hbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing, LayoutStyles.centerAlignLeft)
        text("Filter issues: ")

        hbox {
            addClass(LayoutStyles.typicalSpacing, LayoutStyles.centerAlignLeft)
            label("Issue name/number: ")
            textfield {
                filterFieldFlow = textProperty().asFlow().debounce(250)
            }
        }

        hbox {
            addClass(LayoutStyles.typicalSpacing, LayoutStyles.centerAlignLeft)
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