package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import io.ktor.http.Url

data class Project(val id: Int, val description: String, val name: String, val gitlabPath: String, val url: Url) {
    companion object {
        fun fromGitlabDto(dto: GitlabProject) = Project(
            dto.id,
            dto.description ?: "",
            dto.name,
            dto.pathWithNamespace,
            Url(dto.webURL)
        )
    }
}