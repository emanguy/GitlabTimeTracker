package edu.erittenhouse.gitlabtimetracker.ui.style

import tornadofx.*

class TypographyStyles : Stylesheet() {
    companion object {
        val title by cssclass()
        val subtitle by cssclass()
    }

    init {
        hyperlink {
            padding = box(0.px)
        }
        title {
            fontSize = 20.px
        }
        subtitle {
            fill = c("#717171")
        }
    }
}