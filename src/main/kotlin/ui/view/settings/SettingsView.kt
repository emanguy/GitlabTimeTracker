package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import tornadofx.*

class SettingsView : SuspendingView("Time Tracker Settings") {

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text("Time Tracker Settings") {
            addClass(TypographyStyles.title)
        }
        separator()
        add(SlackSettingsSubview::class)
    }

    override fun startBackgroundTasks() {
        super.startBackgroundTasks()
        println("Root settings - background tasks started")
    }

    override fun viewClosing() {
        super.viewClosing()
        println("Root settings - view closing")
    }
}