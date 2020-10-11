package edu.erittenhouse.gitlabtimetracker.ui.view.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.controller.result.ProjectSelectResult
import edu.erittenhouse.gitlabtimetracker.ui.fragment.ProjectListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingIOSafeView
import edu.erittenhouse.gitlabtimetracker.ui.util.showErrorModal
import javafx.scene.layout.Priority
import tornadofx.listview
import tornadofx.vgrow

class ProjectListView : SuspendingIOSafeView() {
    private val projectController by inject<ProjectController>()
    private val issueController by inject<IssueController>()

    override val root = listview(projectController.projects) {
        vgrow = Priority.ALWAYS
        cellFragment(ProjectListCellFragment::class)
        suspendingOnUserSelectOnce {
            when (issueController.selectProject(it)) {
                is ProjectSelectResult.IssuesLoaded -> { /* Don't need to do anything, working as intended */ }
                is ProjectSelectResult.NoCredentials -> showErrorModal("Something's wrong, the time tracker couldn't read your credentials when pulling projects")
            }
        }
    }
}