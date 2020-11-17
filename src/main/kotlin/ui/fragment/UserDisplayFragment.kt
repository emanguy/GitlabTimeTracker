package edu.erittenhouse.gitlabtimetracker.ui.fragment

import edu.erittenhouse.gitlabtimetracker.model.User
import edu.erittenhouse.gitlabtimetracker.ui.style.LayoutStyles
import edu.erittenhouse.gitlabtimetracker.ui.style.TypographyStyles
import edu.erittenhouse.gitlabtimetracker.ui.util.Debouncer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.flexSpacer
import edu.erittenhouse.gitlabtimetracker.ui.util.extensions.showErrorModalForIOErrors
import edu.erittenhouse.gitlabtimetracker.ui.util.suspension.SuspendingItemFragment
import javafx.beans.property.SimpleStringProperty
import javafx.scene.shape.Circle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tornadofx.*
import kotlin.coroutines.CoroutineContext

class UserDisplayFragment : SuspendingItemFragment<User>() {
    private val usersNameProperty = SimpleStringProperty("")
    private val usersUsernameProperty = SimpleStringProperty("")
    private val usersPhotoURLProperty = SimpleStringProperty("/LoadingPlaceholder.jpg")

    private val ioExceptionDebouncer = Debouncer()

    private val _settingsTriggerFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)
    val settingsTriggerFlow = _settingsTriggerFlow.asSharedFlow()

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
        addClass(LayoutStyles.typicalPaddingAndSpacing)
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

        flexSpacer()

        button("Settings") {
            action {
                println(_settingsTriggerFlow.tryEmit(Unit))
            }
        }
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        ioExceptionDebouncer.runDebounced { showErrorModalForIOErrors(exception) }
    }
}