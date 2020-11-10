package edu.erittenhouse.gitlabtimetracker.ui.util

/**
 * Helps debounce invocations of a function.
 *
 * @param debounceTime The amount of time to wait before allowing another invocation of runDebounced()
 */
class Debouncer(private val debounceTime: Long = EXCEPTION_DEBOUNCE_MILLIS) {
    private var lastHandleTime = Long.MIN_VALUE

    fun runDebounced(action: () -> Unit) {
        val currentTime = System.currentTimeMillis()

        if (lastHandleTime + debounceTime <= currentTime) {
            action()
            lastHandleTime = currentTime
        }
    }
}