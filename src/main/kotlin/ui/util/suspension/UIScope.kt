package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.coroutines.CoroutineContext

interface UIScope : CoroutineScope {
    var backgroundTasksStarted: Boolean
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
    fun Node.suspendingOnMouseClick(action: suspend CoroutineScope.(MouseEvent) -> Unit) {
        setOnMouseClicked { evt ->
            this@UIScope.launch {
                action(evt)
            }
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

    fun getChildUiScopes(): List<UIScope>

    fun startBackgroundTasks() {
        if (backgroundTasksStarted) return
        backgroundTasksStarted = true
        getChildUiScopes().forEach { it.startBackgroundTasks() }
    }
    fun viewClosing() {
        coroutineContext.cancelChildren()
        backgroundTasksStarted = false
        getChildUiScopes().forEach { it.viewClosing() }
    }

    fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        println("Uncaught coroutine exception.")
        exception.printStackTrace()
    }
}

fun deepGetChildUIScopes(nextComponent: Parent): List<UIScope> {
    if (nextComponent is UIScope) return listOf(nextComponent)
    return nextComponent.getChildList()?.flatMap {
        if (it is Parent) deepGetChildUIScopes(it) else emptyList()
    } ?: emptyList()
}