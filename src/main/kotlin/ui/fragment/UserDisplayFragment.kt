package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.model.User
import edu.erittenhouse.gitlabtimetracker.ui.style.Images
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexspacer
import javafx.beans.property.SimpleStringProperty
import javafx.scene.shape.Circle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tornadofx.*

class UserDisplayFragment : ItemFragment<User>() {
    private val usersNameProperty = SimpleStringProperty("")
    private val usersUsernameProperty = SimpleStringProperty("")
    private val usersPhotoURLProperty = SimpleStringProperty(Images.loadingPlaceholder)

    private val mutableSettingsTriggerFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val settingsTriggerFlow = mutableSettingsTriggerFlow.asSharedFlow()

    init {
        itemProperty.onChange { user ->
            if (user != null) {
                usersNameProperty.set(user.name)
                usersUsernameProperty.set("@${user.username}")
                usersPhotoURLProperty.set(user.profilePictureURL.toString())
            }
        }
    }

    override val root = hbox {
        addClass(LayoutStyles.typicalPaddingAndSpacing, LayoutStyles.centerAlignLeft)
        imageview(usersPhotoURLProperty) {
            clip = Circle(20.0, 20.0, 20.0)
            fitWidth = 40.0
            fitHeight = 40.0
        }
        vbox {
            style {
                spacing = 8.px
            }
            text(usersNameProperty)
            text(usersUsernameProperty) {
                addClass(TypographyStyles.metadata)
            }
        }

        flexspacer()

        button("Settings") {
            imageview(Images.settings)
            action {
                mutableSettingsTriggerFlow.tryEmit(Unit)
            }
        }
    }
}