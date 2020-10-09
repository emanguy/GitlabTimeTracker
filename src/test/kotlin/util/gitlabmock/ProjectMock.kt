package edu.erittenhouse.gitlabtimetracker.util.gitlabmock

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabMilestone
import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject

data class ProjectMock(val projectData: GitlabProject, val milestones: List<GitlabMilestone> = emptyList(), val issues: List<AssignedIssueMock> = emptyList())