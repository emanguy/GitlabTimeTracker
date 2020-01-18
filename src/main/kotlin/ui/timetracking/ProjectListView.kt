package edu.erittenhouse.gitlabtimetracker.ui.timetracking

import edu.erittenhouse.gitlabtimetracker.controller.IssueController
import edu.erittenhouse.gitlabtimetracker.controller.ProjectController
import edu.erittenhouse.gitlabtimetracker.ui.fragment.ErrorFragment
import edu.erittenhouse.gitlabtimetracker.ui.fragment.ProjectListCellFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.SuspendingView
import kotlinx.coroutines.launch
import tornadofx.listview
import tornadofx.px
import tornadofx.style
import java.util.logging.Level

class ProjectListView : SuspendingView() {
    private val projectController by inject<ProjectController>()
    private val issueController by inject<IssueController>()

    override val root = listview(projectController.projects) {
        style {
            maxWidth = 600.px
        }
        cellFragment(ProjectListCellFragment::class)
        suspendingOnUserSelectOnce {
            issueController.selectProject(it)
        }
    }

    override fun onDock() {
        super.onDock()
        launch {
            try {
                projectController.fetchProjects()
            } catch (e: Exception) {
                log.log(Level.SEVERE, e.message, e)
                find<ErrorFragment>("errorMessage" to e.message).openModal()
            }
        }
    }
}