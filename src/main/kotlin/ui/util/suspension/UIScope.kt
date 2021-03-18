package edu.erittenhouse.gitlabtimetracker.ui.util.suspension

import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import kotlinx.coroutines.*
import kotlinx.coroutines.javafx.JavaFx
import tornadofx.*
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * UIScope is a TornadoFX specific implementation of CoroutineScope which provides coroutine-friendly
 * extensions for many parts of TornadoFX and supports nested cancellation of coroutines when top-level components
 * are detached from the UI.
 */
interface UIScope : CoroutineScope {
    val tornadofxScope: Scope

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

    /**
     * Adds an instance of [type] as a child of the receiver and registers it as a child UIScope of this UIScope.
     */
    fun <T> EventTarget.scopeAdd(type: KClass<T>, params: Map<*, Any?>? = null, op: T.() -> Unit = {})
        where T: UIComponent,
              T: UIScope {
        val view = find(type, tornadofxScope, params)
        plusAssign(view.root)
        registerChildScope(view)
        op(view)
      }

    /**
     * Adds [child] as a child of the receiver and registers it as a child UIScope of this UIScope.
     */
    fun <T> EventTarget.scopeAdd(child: T)
        where T: UIComponent,
              T: UIScope {
        plusAssign(child.root)
        registerChildScope(child)
    }

    /**
     * Creates an instance of [type], registers it as a child UIScope of this UIScope, and places it in the
     * top region of the BorderPane
     */
    fun <T> BorderPane.scopeTop(type: KClass<T>)
        where T: UIComponent,
              T: UIScope {
        val component = find(type, tornadofxScope)
        top = component.root
        registerChildScope(component)
    }

    /**
     * Creates an instance of [type], registers it as a child UIScope of this UIScope, and places it in the
     * right region of the BorderPane
     */
    fun <T> BorderPane.scopeRight(type: KClass<T>)
        where T: UIComponent,
              T: UIScope {
        val component = find(type, tornadofxScope)
        right = component.root
        registerChildScope(component)
    }

    /**
     * Creates an instance of [type], registers it as a child UIScope of this UIScope, and places it in the
     * bottom region of the BorderPane
     */
    fun <T> BorderPane.scopeBottom(type: KClass<T>)
        where T: UIComponent,
              T: UIScope {
        val component = find(type, tornadofxScope)
        bottom = component.root
        registerChildScope(component)
    }

    /**
     * Creates an instance of [type], registers it as a child UIScope of this UIScope, and places it in the
     * left region of the BorderPane
     */
    fun <T> BorderPane.scopeLeft(type: KClass<T>)
        where T: UIComponent,
              T: UIScope {
        val component = find(type, tornadofxScope)
        left = component.root
        registerChildScope(component)
    }

    /**
     * Creates an instance of [type], registers it as a child UIScope of this UIScope, and places it in the
     * center region of the BorderPane
     */
    fun <T> BorderPane.scopeCenter(type: KClass<T>)
        where T: UIComponent,
              T: UIScope {
        val component = find(type, tornadofxScope)
        center = component.root
        registerChildScope(component)
    }

    fun triggerBackgroundTasks()
    fun triggerViewClosing()
    fun registerBackgroundTaskInit(backgroundTaskInitFunction: () -> Unit)
    fun registerBackgroundTaskCleanup(backgroundTaskCleanupFunction: () -> Unit)
    fun registerCoroutineExceptionHandler(coroutineExceptionHandleFunction: (CoroutineContext, Throwable) -> Unit)
    fun registerChildScope(scope: UIScope)

    fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        println("Uncaught coroutine exception.")
        exception.printStackTrace()
    }
}

class UIScopeImpl : UIScope {
    private val ceh = CoroutineExceptionHandler(this::onUncaughtCoroutineException)
    override val coroutineContext = SupervisorJob() + Dispatchers.JavaFx + ceh
    private lateinit var backingScope: Scope
    override val tornadofxScope: Scope
        get() = backingScope

    private var backgroundJobsStarted = false
    private var childScopes = emptyList<UIScope>()
    private var backgroundTaskInitFunctions: List<() -> Unit> = emptyList()
    private var backgroundTaskCleanupFunctions: List<() -> Unit> = emptyList()
    private var coroutineExceptionHandlers: List<(CoroutineContext, Throwable) -> Unit> = emptyList()

    fun registerComponent(component: UIComponent) {
        this.backingScope = component.scope
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

    override fun registerCoroutineExceptionHandler(coroutineExceptionHandleFunction: (CoroutineContext, Throwable) -> Unit) {
        coroutineExceptionHandlers = coroutineExceptionHandlers + coroutineExceptionHandleFunction
    }

    override fun registerChildScope(scope: UIScope) {
        childScopes = childScopes + scope
    }

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        this.coroutineExceptionHandlers.forEach { it(context, exception) }
    }
}
