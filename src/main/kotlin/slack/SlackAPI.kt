package edu.erittenhouse.gitlabtimetracker.slack

import tornadofx.Component
import tornadofx.ScopedInstance

open class SlackAPI : Component(), ScopedInstance {
    open val authHandler: ISlackAuthHandler = SlackAuthHandler()
    open val profileAPI: ISlackProfileAPI = SlackProfileAPI()
}