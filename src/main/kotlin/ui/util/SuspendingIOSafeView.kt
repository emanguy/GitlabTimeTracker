package edu.erittenhouse.gitlabtimetracker.ui.util

import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import javafx.scene.Node
import kotlin.coroutines.CoroutineContext


/**
 * SuspendingIOSafeView is a [SuspendingView] with a default uncaught coroutine exception handler that
 * automatically handles IO exceptions and shows an error modal when they occur.The error reporting is debounced,
 * so if many I/O errors come in all at once the user won't be flooded with error messages.
 */
abstract class SuspendingIOSafeView(title: String? = null, icon: Node? = null) : SuspendingView(title, icon) {
    private var lastHandleTime = Long.MIN_VALUE

    override fun onUncaughtCoroutineException(context: CoroutineContext, exception: Throwable) {
        super.onUncaughtCoroutineException(context, exception)
        val currentTime = System.currentTimeMillis()

        if (lastHandleTime + EXCEPTION_DEBOUNCE_MILLIS <= currentTime) {
            val errorMessage = generateMessageForIOExceptions(exception)
            showErrorModal(errorMessage)
            lastHandleTime = currentTime
        }
    }
}