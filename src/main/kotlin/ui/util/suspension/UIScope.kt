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
    fun <T> EventTarget.scopeAdd(child: T)
        where T: UIComponent,
              T: UIScope

    fun triggerBackgroundTasks()
    fun triggerViewClosing()
    fun registerBackgroundTaskInit(backgroundTaskInitFunction: () -> Unit)
    fun registerBackgroundTaskCleanup(backgroundTaskCleanupFunction: () -> Unit)

    fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        println("Uncaught coroutine exception.")
        exception.printStackTrace()
    }
}

class UIScopeImpl : UIScope {
    private val ceh = CoroutineExceptionHandler(::onUncaughtCoroutineException)
    override val coroutineContext = SupervisorJob() + Dispatchers.JavaFx + ceh

    private var backgroundJobsStarted = false
    private lateinit var component: UIComponent
    private var childScopes = emptyList<UIScope>()
    private var backgroundTaskInitFunctions: List<() -> Unit> = emptyList()
    private var backgroundTaskCleanupFunctions: List<() -> Unit> = emptyList()

    fun registerComponent(component: UIComponent) {
        this.component = component
    }

    override fun triggerBackgroundTasks() {
        if (backgroundJobsStarted) return
        backgroundJobsStarted = true
        val initFunctions = backgroundTaskInitFunctions
        initFunctions.forEach { it() }
        childScopes.forEach { it.triggerBackgroundTasks() }
    }

    override fun triggerViewClosing() {
        if (!backgroundJobsStarted) return
        backgroundJobsStarted = false
        val cleanupFunctions = backgroundTaskCleanupFunctions
        coroutineContext.cancelChildren()
        cleanupFunctions.forEach { it() }
        childScopes.forEach { it.triggerViewClosing() }
    }

    override fun registerBackgroundTaskInit(backgroundTaskInitFunction: () -> Unit) {
        backgroundTaskInitFunctions = backgroundTaskInitFunctions + backgroundTaskInitFunction
    }

    override fun registerBackgroundTaskCleanup(backgroundTaskCleanupFunction: () -> Unit) {
        backgroundTaskCleanupFunctions = backgroundTaskCleanupFunctions + backgroundTaskCleanupFunction
    }

    override fun <T> EventTarget.scopeAdd(type: KClass<T>, params: Map<*, Any?>?, op: T.() -> Unit)
        where T : UIComponent,
              T : UIScope {
        val view = find(type, component.scope, params)
        plusAssign(view.root)
        childScopes = childScopes + view
        op(view)
    }

    override fun <T> EventTarget.scopeAdd(child: T)
        where T : UIComponent,
              T : UIScope {
        plusAssign(child.root)
        childScopes = childScopes + child
    }
}
