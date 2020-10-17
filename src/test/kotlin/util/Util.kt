package edu.erittenhouse.gitlabtimetracker.util

import edu.erittenhouse.gitlabtimetracker.controller.StorageConfig
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMockAPI
import tornadofx.Scope
import tornadofx.setInScope

const val CREDENTIAL_FILE_LOCATION = "./.gtt"

fun generateTestScope(gitlabState: GitlabMock, credentialFileLocation: String = CREDENTIAL_FILE_LOCATION): Scope = Scope().apply {
    setInScope(GitlabMockAPI(gitlabState), this, GitlabAPI::class)
    setInScope(StorageConfig(credentialFileLocation), this)
}
