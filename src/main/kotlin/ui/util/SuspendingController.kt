package edu.erittenhouse.gitlabtimetracker.ui.util

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.Controller
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

abstract class SuspendingController : Controller(), CoroutineScope {
    private val ceh = CoroutineExceptionHandler { coroutineContext, throwable ->
        this.onUncaughtCoroutineException(coroutineContext,throwable)
    }
    override val coroutineContext = SupervisorJob() + Dispatchers.Default + ceh

    open fun onUncaughtCoroutineException(coroutineContext: CoroutineContext, exception: Throwable) {
        log.log(Level.WARNING, "Uncaught coroutine exception.", exception)
    }
}