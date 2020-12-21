package edu.erittenhouse.gitlabtimetracker.ui.style

import javafx.geometry.Pos
import tornadofx.*

class LayoutStyles : Stylesheet() {
    companion object {
        val typicalPaddingAndSpacing by cssclass()
        val typicalPadding by cssclass()
        val typicalSpacing by cssclass()
        val noPadding by cssclass()
        val centerAlignLeft by cssclass()
        val bottomAlignRight by cssclass()
    }

    init {
        typicalPaddingAndSpacing {
            padding = box(8.px)
            spacing = 8.px
        }

        typicalSpacing {
            spacing = 8.px
        }

        noPadding {
            padding = box(0.px)
        }

        centerAlignLeft {
            alignment = Pos.CENTER_LEFT
        }

        bottomAlignRight {
            alignment = Pos.BOTTOM_RIGHT
        }
    }
}