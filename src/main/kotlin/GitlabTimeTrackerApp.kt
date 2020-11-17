package edu.erittenhouse.gitlabtimetracker

import edu.erittenhouse.gitlabtimetracker.ui.style.appStyles
import edu.erittenhouse.gitlabtimetracker.ui.view.LoginView
import tornadofx.App

class GitlabTimeTrackerApp : App(LoginView::class, *appStyles)
