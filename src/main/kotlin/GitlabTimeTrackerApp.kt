package edu.erittenhouse.gitlabtimetracker

import edu.erittenhouse.gitlabtimetracker.ui.view.LoginView
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import tornadofx.App

class GitlabTimeTrackerApp : App(LoginView::class, TypographyStyles::class, LayoutStyles::class)