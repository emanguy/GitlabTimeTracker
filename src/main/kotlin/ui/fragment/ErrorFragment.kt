package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import tornadofx.*

class ErrorFragment : Fragment() {
    val errorMessage: String by param()

    override val root = vbox {
        addClass(LayoutStyles.typicalSpacing)

        text("Whoops!") {
            addClass(TypographyStyles.title)
        }
        text(errorMessage)
        button("OK") {
            action {
                close()
            }
        }
    }
}