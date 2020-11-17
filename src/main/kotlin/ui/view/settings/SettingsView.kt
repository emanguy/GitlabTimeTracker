package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import tornadofx.*

class SettingsView : View("Time Tracker Settings") {

    override val root = vbox {
        text("Time Tracker Settings") {
            addClass(TypographyStyles.title)
        }
        separator()
        add(SlackSettingsSubview::class)
    }
}