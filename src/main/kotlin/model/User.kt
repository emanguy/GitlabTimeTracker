package edu.erittenhouse.gitlabtimetracker.model

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser
import io.ktor.http.Url

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val profilePictureURL: Url
) {
    companion object {
        fun fromGitlabUser(user: GitlabUser) = User(
            user.id,
            user.name,
            user.username,
            Url(user.profilePictureURL)
        )
    }
}