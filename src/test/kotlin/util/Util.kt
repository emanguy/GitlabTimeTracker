package edu.erittenhouse.gitlabtimetracker.util

import edu.erittenhouse.gitlabtimetracker.controller.StorageConfig
import edu.erittenhouse.gitlabtimetracker.gitlab.GitlabAPI
import edu.erittenhouse.gitlabtimetracker.slack.SlackAPI
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMock
import edu.erittenhouse.gitlabtimetracker.util.gitlabmock.GitlabMockAPI
import edu.erittenhouse.gitlabtimetracker.util.slackmock.SlackMock
import edu.erittenhouse.gitlabtimetracker.util.slackmock.SlackMockAPI
import tornadofx.Scope
import tornadofx.setInScope

const val CREDENTIAL_FILE_LOCATION = "./.gtt"

fun generateTestGitlabScope(gitlabState: GitlabMock, credentialFileLocation: String = CREDENTIAL_FILE_LOCATION): Scope = Scope().apply {
    setInScope(GitlabMockAPI(gitlabState), this, GitlabAPI::class)
    setInScope(StorageConfig(credentialFileLocation), this)
}

fun generateTestSlackScope(
    gitlabState: GitlabMock,
    slackState: SlackMock = SlackMock(),
    credentialFileLocation: String = CREDENTIAL_FILE_LOCATION,
): Scope = generateTestGitlabScope(gitlabState, credentialFileLocation).apply {
    setInScope(SlackMockAPI(slackState), this, SlackAPI::class)
}
