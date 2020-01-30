package edu.erittenhouse.gitlabtimetracker.ui.util

import javafx.scene.Node
import javafx.scene.control.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.View
import tornadofx.action
import tornadofx.onUserSelect
import kotlin.coroutines.CoroutineContext

abstract class SuspendingView(title: String? = null, icon: Node? = null) : View(title, icon), CoroutineScope {
    private val ceh = CoroutineExceptionHandler { context, exception ->
        this.onUncaughtCoroutineException(context, exception)
    }
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    protected fun ButtonBase.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingView.launch(block = action)
        }
    }
    protected fun ChoiceBox<*>.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingView.launch(block = action)
        }
    }
    protected fun TextField.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingView.launch(block = action)
        }
    }
    protected fun MenuItem.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingView.launch(block = action)
        }
    }
    protected fun <T> ListView<T>.suspendingOnUserSelect(clickCount: Int = 2, action: suspend CoroutineScope.(T) -> Unit) {
        onUserSelect(clickCount) {
            this@SuspendingView.launch {
                action(it)
            }
        }
    }
    protected fun <T> ListView<T>.suspendingOnUserSelectOnce(action: suspend CoroutineScope.(T) -> Unit) = suspendingOnUserSelect(1, action)

    open fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        log.warning("Uncaught coroutine exception: ${exception.message}")
    }

    override fun onUndock() {
        super.onUndock()

        // Cancel running coroutines, but not the job on the SuspendingView
        // That way this scope's job is still valid, so if the view is reattached we can still run new coroutines
        this.coroutineContext.cancelChildren()
    }
}