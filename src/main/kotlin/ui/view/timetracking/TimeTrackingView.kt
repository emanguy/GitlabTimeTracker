package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.SlackController
import edu.erittenhouse.gitlabtimetracker.controller.UserController
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectFetchResult
import edu.erittenhouse.gitlabtimetracker.controller.result.UserLoadResult
import edu.erittenhouse.gitlabtimetracker.ui.fragment.UserDisplayFragment
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModal
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showMultiButtonModal
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import edu.erittenhouse.gitlabtimetracker.ui.util.triggers
import edu.erittenhouse.gitlabtimetracker.ui.view.settings.SettingsView
import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import javafx.geometry.Orientation
import javafx.scene.layout.Pane
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

@OptIn(FlowPreview::class)
class TimeTrackingView : SuspendingView("Gitlab Time Tracker") {
    private var swappedChildren = false
    private var issueListPane by singleAssign<Pane>()
    private var userDisplay by singleAssign<UserDisplayFragment>()
    private var settingsStage: Stage? = null

    private val issueController by inject<IssueController>()
    private val userController by inject<UserController>()
    private val projectController by inject<ProjectController>()
    private val ioErrorDebouncer = Debouncer()

    init {
        // Need to do this once to construct the slack controller
        registerBackgroundTaskInit {
            launch {
                find<SlackController>().loadCredentials()
            }
        }

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

        registerBackgroundTaskInit {
            this.currentWindow?.apply {
                width = 1300.0
                height = 768.0
            }
        }
        registerBackgroundTaskInit {
            // Load current user data so we can have it ready and display it above the project list
            launch { loadUserData() }
            // Pull in the projects
            launch { loadProjectData() }
        }
        registerBackgroundTaskInit {
            // Listen for user clicking the settings button, show dialog
            launch {
                listenForSettingsTrigger()
            }
        }
        registerBackgroundTaskInit {
            // Whenever we enter the view, make sure we show the unselected state
            this.swappedChildren = false
            this.issueListPane.replaceChildren {
                text("Select a project to get started.") {
                    addClass(TypographyStyles.title)
                }
            }
        }

        registerCoroutineExceptionHandler { _, exception ->
            ioErrorDebouncer.runDebounced { showExceptionAndRetryModal(exception) }
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
                        scopeAdd(ProjectListView::class)
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

        scopeBottom(TimeRecordingBarView::class)
    }

    private suspend fun listenForSettingsTrigger() {
        userDisplay.settingsTriggerFlow
            .catch { e -> onUncaughtCoroutineException(coroutineContext, e) }
            .collect {
                val settingsStageCopy = settingsStage
                if (settingsStageCopy == null) {
                    val newSettingsStage = find<SettingsView>().openWindow()?.apply {
                        addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST) {
                            settingsStage = null
                        }
                    }
                    settingsStage = newSettingsStage
                } else {
                    settingsStageCopy.requestFocus()
                }
            }
    }

    private suspend fun loadUserData() {
        when (userController.loadCurrentUser()) {
            is UserLoadResult.GotUser -> { /* Good to go! */ }
            is UserLoadResult.NoCredentials -> showErrorModal("Something went wrong. We didn't retrieve the credentials from the login page," +
                    " please notify a developer!")
        }
    }

    private suspend fun loadProjectData() {
        when (projectController.fetchProjects()) {
            is ProjectFetchResult.ProjectsRetrieved -> { /* Good to go! */ }
            is ProjectFetchResult.NoCredentials -> showErrorModal("Something's wrong. We couldn't read your credentials " +
                    "when trying to pull the list of projects.")
        }
    }

    private fun showExceptionAndRetryModal(exception: Throwable) {
        val errorMessage = generateMessageForIOExceptions(exception)

        showMultiButtonModal(
            title = "Whoops!",
            message = errorMessage ?: "We're not sure what went wrong.",
            "Close App" triggers { exitProcess(0) },
            "Retry Data Load" triggers {
                launch { loadUserData() }
                launch { loadProjectData() }
            },
        )
    }
}