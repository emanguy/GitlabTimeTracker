package edu.erittenhouse.gitlabtimetracker.ui.util.extensions

import edu.erittenhouse.gitlabtimetracker.ui.fragment.MultiButtonDialogFragment
import edu.erittenhouse.gitlabtimetracker.ui.fragment.OKDialogFragment
import edu.erittenhouse.gitlabtimetracker.ui.util.NamedCallback
import edu.erittenhouse.gitlabtimetracker.ui.util.PopupCallback
import edu.erittenhouse.gitlabtimetracker.util.generateMessageForIOExceptions
import javafx.stage.Stage
import tornadofx.Component

private const val DEFAULT_ERROR_MSG = "Something went wrong... not sure what though"

/**
 * Shows a modal dialog with a certain [title] and [message], triggering [okAction] when the OK button is clicked
 */
fun Component.showOKModal(title: String, message: String, okAction: PopupCallback = {}): Stage? {
    return find<OKDialogFragment>(
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

/**
 * Shows a modal dialog with a certain [title] and [message], specifying [buttons][modalButtons] that can be triggered on the modal
 */
fun Component.showMultiButtonModal(title: String, message: String, vararg modalButtons: NamedCallback): Stage? {
    return find<MultiButtonDialogFragment>(
        "modalTitle" to title,
        "message" to message,
        "buttonsAndActions" to modalButtons.toList()
    ).openModal()
}