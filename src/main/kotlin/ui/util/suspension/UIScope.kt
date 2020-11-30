package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

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
    fun <T> EventTarget.scopeAdd(type: KClass<T>, params: Map<*, Any?>? = null, op: T.() -> Unit = {})
        where T: UIComponent,
              T: UIScope

    fun startBackgroundTasks()
    fun viewClosing()

    fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        println("Uncaught coroutine exception.")
        exception.printStackTrace()
    }
}

class UIScopeImpl : UIScope {
    private val ceh = CoroutineExceptionHandler(::onUncaughtCoroutineException)
    override val coroutineContext = SupervisorJob() + Dispatchers.JavaFx + ceh

    private lateinit var component: UIComponent
    private var childScopes = emptyList<UIScope>()

    fun registerComponent(component: UIComponent) {
        this.component = component
    }

    override fun startBackgroundTasks() {
        childScopes.forEach { it.startBackgroundTasks() }
    }

    override fun viewClosing() {
        coroutineContext.cancelChildren()
        childScopes.forEach { it.viewClosing() }
    }

    override fun <T> EventTarget.scopeAdd(type: KClass<T>, params: Map<*, Any?>?, op: T.() -> Unit)
        where T : UIComponent,
              T : UIScope {
        val view = find(type, component.scope, params)
        plusAssign(view.root)
        childScopes = childScopes + view
        op(view)
    }
}
