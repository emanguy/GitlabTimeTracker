package edu.erittenhouse.gitlabtimetracker.ui.view.settings

import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.toggleswitch
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingView
import tornadofx.*

class SlackSettingsSubview : SuspendingView() {
    override val root = vbox {
        text("Slack") {
            addClass(TypographyStyles.subtitle)
        }
        button("Connect to slack") {

        }
        hbox {
            label("Status emoji (i.e. :tada:): ")
            textfield()
        }
        hbox {
            label("Slack status template: ")
            textfield()
        }
        toggleswitch("Enable slack integration") {
//            isSelected = false
        }
    }
}