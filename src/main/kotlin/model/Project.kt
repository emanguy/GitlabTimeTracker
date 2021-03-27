package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabProject
import io.ktor.http.Url

data class Project(val id: Int, val description: String, val name: String, val gitlabPath: String, val url: Url, val pinned: Boolean = false) {
    companion object {
        fun fromGitlabDto(dto: GitlabProject, isPinned: Boolean = false) = Project(
            id = dto.id,
            description = dto.description ?: "",
            name = dto.name,
            gitlabPath = dto.pathWithNamespace,
            url = Url(dto.webURL),
            pinned = isPinned,
        )
    }
}