package edu.erittenhouse.gitlabtimetracker.ui.util

import edu.erittenhouse.gitlabtimetracker.ui.fragment.OKDialogFragment
import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import tornadofx.Component

private const val DEFAULT_ERROR_MSG = "Something went wrong... not sure what though"

/**
 * Shows a modal dialog with a certain [title] and [message], triggering [okAction] when the OK button is clicked
 */
fun Component.showOKModal(title: String, message: String, okAction: PopupCallback = {}) {
    find<OKDialogFragment>(
        "modalTitle" to title,
        "message" to message,
        "popupCallback" to okAction,
    ).openModal()
}

/**
 * Shows a modal dialog configured to provide detail about an error
 */
fun Component.showErrorModal(errorMessage: String?, okAction: PopupCallback = {}) = showOKModal("Whoops!", errorMessage ?: DEFAULT_ERROR_MSG, okAction)

/**
 * Shows a modal dialog, auto-generating error text for typical IO errors
 */
fun Component.showErrorModalForIOErrors(e: Throwable) = showErrorModal(generateMessageForIOExceptions(e))
