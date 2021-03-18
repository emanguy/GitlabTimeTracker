package edu.erittenhouse.gitlabtimetracker.ui.view

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.result.IssueFetchResult
import edu.erittenhouse.gitlabtimetracker.model.Issue
import edu.erittenhouse.gitlabtimetracker.ui.fragment.IssueListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class IssueLookupView : SuspendingView() {
    private val issueController by inject<IssueController>()
    private val issueFragment = find<IssueListCellFragment>()
    private val ioErrorDebouncer = Debouncer()

    private var issueNumText = SimpleStringProperty("")
    private val statusMessage = SimpleStringProperty("")
    private val foundIssue = SimpleObjectProperty<Issue?>(null)
    private val numberRegex = """^\d+$""".toRegex()

    private val issueCanBeFetched = issueNumText.booleanBinding(issueController.selectedProject) {
        issueController.selectedProject.get() != null && numberRegex.matches(it ?: "")
    }
    private val issueFindTitleBinding = issueController.selectedProject.stringBinding { project ->
        if (project != null) "Find issue to track in ${project.name}" else "Find issue to track"
    }

    init {
        issueFragment.itemProperty.bind(foundIssue)
        issueFragment.root.hgrow = Priority.ALWAYS
        issueController.selectedProject.onChange {
            foundIssue.set(null)
            statusMessage.set("")
        }

        registerBackgroundTaskInit {
            currentStage?.width = 600.0
            issueNumText.set("")
            statusMessage.set("")
            foundIssue.set(null)
        }

        registerCoroutineExceptionHandler { _, exception ->
            ioErrorDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
        }
    }

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text(issueFindTitleBinding) {
            addClass(TypographyStyles.title)
        }
        form {
            fieldset {
                field("Issue number: ") {
                    textfield {
                        promptText = "1234"
                        issueNumText.bindBidirectional(textProperty())
                        setOnKeyReleased {
                            if (issueCanBeFetched.get() && it.code == KeyCode.ENTER) {
                                launch { fetchIssue() }
                            }
                        }
                    }
                    button("Find issue") {
                        enableWhen(issueCanBeFetched)
                        imageview(Images.searchIssues)
                        suspendingAction {
                            fetchIssue()
                        }
                    }
                }
            }
        }

        separator {
            visibleWhen(statusMessage.booleanBinding(foundIssue) { message ->
                !message.isNullOrEmpty() || foundIssue.get() != null
            })
        }

        text(statusMessage) {
            visibleWhen(statusMessage.booleanBinding {  !it.isNullOrEmpty() } )
        }
        hbox {
            visibleWhen(foundIssue.isNotNull)
            add(issueFragment)
        }
    }

    /**
     * Tries to fetch the issue entered into the text field
     */
    private suspend fun fetchIssue() {
        statusMessage.set("Fetching issue...")
        foundIssue.set(null)

        val retrievedIssue =
            when (val issueFetchResult = issueController.fetchIssueByID(issueNumText.get().toInt())) {
                is IssueFetchResult.NoCredentials -> {
                    statusMessage.set(
                        "Something went wrong. Somehow we lost your Gitlab credentials. " +
                                "Please log in again and notify a developer that this occurred."
                    )
                    return
                }
                is IssueFetchResult.NoProject -> {
                    statusMessage.set("Cannot fetch an issue when no project is selected.")
                    return
                }
                is IssueFetchResult.IssueNotFound -> {
                    statusMessage.set("We couldn't find that issue. Please try a different issue number.")
                    return
                }
                is IssueFetchResult.IssueFound -> {
                    statusMessage.set("Here's what we found:")
                    issueFetchResult.issue
                }
            }

        foundIssue.set(retrievedIssue)
    }
}