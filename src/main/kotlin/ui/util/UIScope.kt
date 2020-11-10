package edu.erittenhouse.gitlabtimetracker.ui.util

import javafx.scene.control.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tornadofx.action
import tornadofx.onUserSelect
import kotlin.coroutines.CoroutineContext

interface UIScope : CoroutineScope {
    fun ButtonBase.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@UIScope.launch(block = action)
        }
    }
    fun ChoiceBox<*>.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@UIScope.launch(block = action)
        }
    }
    fun TextField.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@UIScope.launch(block = action)
        }
    }
    fun MenuItem.suspendingAction(action: suspend CoroutineScope.() -> Unit) {
        action {
            this@UIScope.launch(block = action)
        }
    }
    fun <T> ListView<T>.suspendingOnUserSelect(clickCount: Int = 2, action: suspend CoroutineScope.(T) -> Unit) {
        onUserSelect(clickCount) {
            this@UIScope.launch {
                action(it)
            }
        }
    }
    fun <T> ListView<T>.suspendingOnUserSelectOnce(action: suspend CoroutineScope.(T) -> Unit) = suspendingOnUserSelect(1, action)

    fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        println("Uncaught coroutine exception.")
        exception.printStackTrace()
    }
}