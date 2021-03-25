package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.NamedCallback
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingFragment
import tornadofx.*

class MultiButtonDialogFragment : SuspendingFragment() {
    private val modalTitle: String by param()
    private val message: String by param()
    private val buttonsAndActions: List<NamedCallback> by param()

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text(modalTitle) {
            addClass(TypographyStyles.title)
        }
        text(message)

        hbox {
            addClass(LayoutStyles.typicalSpacing)

            for ((buttonName, callback) in buttonsAndActions) {
                button(buttonName) {
                    suspendingAction {
                        callback()
                        close()
                    }
                }
            }
        }
    }
}
