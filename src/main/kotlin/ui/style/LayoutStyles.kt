package edu.erittenhouse.gitlabtimetracker.ui.style

import tornadofx.Stylesheet
import tornadofx.box
import tornadofx.cssclass
import tornadofx.px

class LayoutStyles : Stylesheet() {
    companion object {
        val typicalPaddingAndSpacing by cssclass()
        val typicalSpacing by cssclass()
    }

    init {
        typicalPaddingAndSpacing {
            padding = box(8.px)
            spacing = 8.px
        }

        typicalSpacing {
            spacing = 8.px
        }
    }
}