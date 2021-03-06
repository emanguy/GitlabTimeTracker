package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.result.IssueRefreshResult
import edu.erittenhouse.gitlabtimetracker.model.filter.MilestoneFilterOption
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexspacer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.view.IssueLookupView
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.javafx.asFlow
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class FilterBarView : SuspendingView() {
    private val issueController by inject<IssueController>()
    private var filterFieldFlow by singleAssign<Flow<String>>()
    private var milestoneFilterFlow by singleAssign<Flow<MilestoneFilterOption>>()
    private var issueFindStage: Stage? = null
    private val issueFindDebouncer = Debouncer(debounceTime = 250)
    private val ioErrorDebouncer = Debouncer()

    init {
        registerBackgroundTaskInit {
            launch {
                filterFieldFlow.collect {
                    issueController.filterByIssueText(it)
                }
            }
        }
        registerBackgroundTaskInit {
            launch {
                milestoneFilterFlow.collect {
                    issueController.selectMilestoneFilterOption(it)
                }
            }
        }

        registerCoroutineExceptionHandler { _, exception ->
            ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
        }
    }

    override val root = hbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        form {
            addClass(LayoutStyles.noPadding, LayoutStyles.typicalSpacing)

            text("Filter issues: ")
            fieldset {
                addClass(LayoutStyles.noPadding)

                field("Issue name/number: ") {
                    addClass(LayoutStyles.noPadding)
                    textfield {
                        filterFieldFlow = textProperty().asFlow().debounce(250)
                        issueController.filter.onChange {
                            if (it?.filterText != this@textfield.text) {
                                this@textfield.text = it?.filterText
                            }
                        }
                    }
                }
                field("Milestone: ") {
                    style {
                        padding = box(8.px, 0.px, 0.px, 0.px)
                    }
                    combobox<MilestoneFilterOption> {
                        items = issueController.milestoneFilterOptions
                        issueController.milestoneFilterOptions.onChange {
                            selectionModel.selectFirst()
                        }
                        milestoneFilterFlow = selectionModel.selectedItemProperty().asFlow().filterNotNull()
                        issueController.filter.onChange {
                            if (it?.selectedMilestone != this.selectedItem) {
                                this.selectionModel.select(it?.selectedMilestone)
                            }
                        }
                    }
                }
            }
        }

        flexspacer()

        vbox {
            addClass(LayoutStyles.typicalSpacing, LayoutStyles.bottomAlignRight)

            button("Track other issue") {
                maxWidth = Double.MAX_VALUE
                imageview(Images.searchIssues)

                action {
                    issueFindDebouncer.runDebounced {
                        val findStageCopy = issueFindStage
                        if (findStageCopy == null) {
                            val newFindStage = find<IssueLookupView>().openWindow()?.apply {
                                addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST) {
                                    issueFindStage = null
                                }
                            }
                            issueFindStage = newFindStage
                        } else {
                            findStageCopy.requestFocus()
                        }
                    }
                }
            }
            button("Refresh issues") {
                maxWidth = Double.MAX_VALUE
                imageview(Images.refreshIssues)

                suspendingAction {
                    this@button.text = "Loading..."
                    val refreshResult = try {
                        issueController.refreshIssues()
                    } finally {
                        this@button.text = "Refresh issues"
                    }

                    when (refreshResult) {
                        is IssueRefreshResult.RefreshSuccess -> { /* All good! */ }
                        is IssueRefreshResult.NoCredentials -> showErrorModal("There was a problem. Couldn't" +
                                " retrieve your GitLab auth data to identify you while refreshing issues.")
                        is IssueRefreshResult.NoProject -> showErrorModal("Couldn't refresh issues, no project" +
                                " is selected.")
                    }
                }
            }
        }
    }
}