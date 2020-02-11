package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.UserController
import edu.erittenhouse.gitlabtimetracker.controller.error.GitlabError
import edu.erittenhouse.gitlabtimetracker.ui.fragment.ErrorFragment
import edu.erittenhouse.gitlabtimetracker.ui.fragment.UserDisplayFragment
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch
import tornadofx.*
import java.util.logging.Level

class TimeTrackingView : SuspendingView() {
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
                    setDividerPositions(0.25)

                    vbox {
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
            width = 800.0
            height = 600.0
        }
        launch {
            try {
                userController.loadCurrentUser()
                projectController.fetchProjects()
            } catch (e: GitlabError) {
                log.log(Level.SEVERE, "Something went wrong talking to GitLab.", e)
                find<ErrorFragment>("errorMessage" to e.message).openModal()
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