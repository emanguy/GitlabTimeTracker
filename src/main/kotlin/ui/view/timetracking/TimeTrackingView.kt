package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.UserController
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.controller.result.UserLoadResult
import edu.erittenhouse.gitlabtimetracker.ui.fragment.UserDisplayFragment
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModalForIOErrors
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class TimeTrackingView : SuspendingView("Gitlab Time Tracker") {
    private var swappedChildren = false
    private var issueListPane by singleAssign<Pane>()
    private var userDisplay by singleAssign<UserDisplayFragment>()

    private val issueController by inject<IssueController>()
    private val userController by inject<UserController>()
    private val projectController by inject<ProjectController>()
    private val ioErrorDebouncer = Debouncer()

    init {
        issueController.selectedProject.onChange {
            if (!swappedChildren) {
                swappedChildren = true
                issueListPane.replaceChildren(find<IssueListView>())
            }
        }
        @Suppress("RemoveExplicitTypeArguments")
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

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }

    override fun onDock() {
        super.onDock()
        this.currentWindow?.apply {
            width = 1300.0
            height = 768.0
        }

        // Load current user data so we can have it ready and display it above the project list
        launch {
            when (userController.loadCurrentUser()) {
                is UserLoadResult.GotUser -> { /* Good to go! */ }
                is UserLoadResult.NoCredentials -> showErrorModal("Something went wrong. We didn't retrieve the credentials from the login page," +
                        " please notify a developer!")
            }
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