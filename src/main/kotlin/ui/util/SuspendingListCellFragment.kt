package edu.erittenhouse.gitlabtimetracker.ui.util

import javafx.scene.control.*
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.ListCellFragment
import tornadofx.action
import tornadofx.onUserSelect
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

abstract class SuspendingListCellFragment<T> : ListCellFragment<T>(), CoroutineScope {
    private val ceh = CoroutineExceptionHandler { context, exception ->
        this.onUncaughtCoroutineException(context, exception)
    }
    override val coroutineContext = Dispatchers.JavaFx + SupervisorJob() + ceh

    protected fun ButtonBase.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingListCellFragment.launch(block = action)
        }
    }
    protected fun ChoiceBox<*>.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingListCellFragment.launch(block = action)
        }
    }
    protected fun TextField.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingListCellFragment.launch(block = action)
        }
    }
    protected fun MenuItem.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@SuspendingListCellFragment.launch(block = action)
        }
    }
    protected fun <T> ListView<T>.suspendingOnUserSelect(clickCount: Int = 2, action: suspend CoroutineScope.(T) -> Unit) {
        onUserSelect(clickCount) {
            this@SuspendingListCellFragment.launch {
                action(it)
            }
        }
    }
    protected fun <T> ListView<T>.suspendingOnUserSelectOnce(action: suspend CoroutineScope.(T) -> Unit) = suspendingOnUserSelect(1, action)

    open fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        log.log(Level.WARNING, "Uncaught coroutine exception.", exception)
    }

    override fun onUndock() {
        super.onUndock()

        // Cancel running coroutines, but not the job on the SuspendingView
        // That way this scope's job is still valid, so if the view is reattached we can still run new coroutines
        this.coroutineContext.cancelChildren()
    }
}