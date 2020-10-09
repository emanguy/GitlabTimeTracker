package edu.erittenhouse.gitlabtimetracker.util.gitlabmock

import edu.erittenhouse.gitlabtimetracker.gitlab.dto.GitlabUser

data class AuthedUser(val userData: GitlabUser, val apiCredentials: Set<String> = emptySet())