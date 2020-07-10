package edu.erittenhouse.gitlabtimetracker.ui.util

import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import kotlin.coroutines.CoroutineContext

/**
 * SuspendingIOSafeListCellFragment is a [SuspendingListCellFragment] with a default onUncaughtCoroutineException
 * implementation that handles I/O errors automatically. The error reporting is debounced, so if many I/O errors come
 * in all at once the user won't be flooded with error messages.
 */
abstract class SuspendingIOSafeListCellFragment<T> : SuspendingListCellFragment<T>() {
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