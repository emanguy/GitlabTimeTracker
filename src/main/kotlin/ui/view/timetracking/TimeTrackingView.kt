package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.UserController
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.ui.fragment.UserDisplayFragment
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModal
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch
import tornadofx.*

class TimeTrackingView : SuspendingIOSafeView("Gitlab Time Tracker") {
    private var swappedChildren = false
    private var issueListPane by singleAssign<Pane>()
    private var userDisplay by singleAssign<UserDisplayFragment>()

    private val issueController by inject<IssueController>()
    private val userController by inject<UserController>()
    private val projectController by inject<ProjectController>()

    init {
        issueController.selectedProject.onChange {
            if (!swappedChildren) {
                swappedChildren = true
                issueListPane.replaceChildren(find<IssueListView>())
            }
        }
        userDisplay = find<UserDisplayFragment>()
        userController.userProperty.onChange { user ->
            userDisplay.itemProperty.set(user)
        }
    }

    override val root = borderpane {
        center {
            splitpane {
                orientation = Orientation.VERTICAL
                setDividerPositions(0.8)
                splitpane {
                    setDividerPositions(0.3)

                    vbox {
                        style {
                            maxWidth = 600.px
                        }

                        add(userDisplay)
                        add(ProjectListView::class)
                    }
                    stackpane {
                        style {
                            padding = box(vertical = 0.px, horizontal = 15.percent)
                        }
                        issueListPane = stackpane()
                    }
                }
            }
        }

        bottom(TimeRecordingBarView::class)
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.apply {
            width = 1300.0
            height = 768.0
        }

        // Load current user data so we can have it ready and display it above the project list
        launch {
            userController.loadCurrentUser()
        }

        // Pull in the projects
        launch {
            when (projectController.fetchProjects()) {
                is ProjectFetchResult.ProjectsRetrieved -> { /* Good to go! */ }
                is ProjectFetchResult.NoCredentials -> showErrorModal("Something's wrong. We couldn't read your credentials " +
                        "when trying to pull the list of projects.")
            }
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