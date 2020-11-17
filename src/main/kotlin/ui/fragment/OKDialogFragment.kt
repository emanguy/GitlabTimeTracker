package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.PopupCallback
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingFragment
import tornadofx.addClass
import tornadofx.button
import tornadofx.text
import tornadofx.vbox

class OKDialogFragment : SuspendingFragment() {
    private val modalTitle: String by param()
    private val message: String by param()
    private val okCallback: PopupCallback by param { /* Do nothing by default */ }

    override val root = vbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing)

        text(modalTitle) {
            addClass(TypographyStyles.title)
        }
        text(message)
        button("OK") {
            suspendingAction {
                okCallback()
                close()
            }
        }
    }
}