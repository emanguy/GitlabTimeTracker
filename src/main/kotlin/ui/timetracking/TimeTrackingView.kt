package edu.erittenhouse.gitlabtimetracker.ui.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import tornadofx.*

class TimeTrackingView : View() {
    private var swappedChildren = false
    private var issueListPane by singleAssign<Pane>()
    private val issueController by inject<IssueController>()

    init {
        issueController.selectedProject.onChange {
            if (!swappedChildren) {
                swappedChildren = true
                issueListPane.replaceChildren(find<IssueListView>())
            }
        }
    }

    override val root = splitpane {
        orientation = Orientation.VERTICAL
        setDividerPositions(0.8)
        splitpane {
            setDividerPositions(0.25)

            add(ProjectListView::class)
            stackpane {
                style {
                    padding = box(vertical = 0.px, horizontal = 15.percent)
                }
                issueListPane = stackpane()
            }
        }
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.apply {
            width = 800.0
            height = 600.0
        }

        // Whenever we enter the view, make sure we show the unselected state
        this.swappedChildren = false
        this.issueListPane.replaceChildren {
            text("Select a project to get started.") {
                addClass(TypographyStyles.title)
            }
        }
    }
}