package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabMilestone
import org.joda.time.DateTime

data class Milestone(
    val idInProject: Int,
    val projectID: Int,
    val title: String,
    val endDate: DateTime?
) {
    companion object {
        fun fromGitlabDto(milestone: GitlabMilestone) = Milestone(
            idInProject = milestone.idInProject,
            projectID = milestone.projectID,
            title = milestone.title,
            endDate = if (milestone.endDate != null) DateTime.parse(milestone.endDate) else null
        )
    }
}