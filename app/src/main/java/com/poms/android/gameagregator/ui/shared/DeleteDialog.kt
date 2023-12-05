package com.poms.android.gameagregator.ui.shared

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.poms.android.gameagregator.R

class DeleteDialog : DialogFragment() {

    private var deleteDialogInterface: DeleteDialogInterface? = null

    /**
     * Function to get the value of the delete dialog interface
     */
    fun setDeleteDialogInterface(deleteDialogInterface: DeleteDialogInterface?) {
        this.deleteDialogInterface = deleteDialogInterface
    }

    /**
     * Function called when dialog is created
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.yes) { _, _ ->
                    LoadingDialog().show(childFragmentManager, "progress")
                    deleteDialogInterface?.optionSelected(true)
                }
                .setNegativeButton(R.string.no) { _, _ ->
                    deleteDialogInterface?.optionSelected(false)
                }

            // Create the AlertDialog object and return it
            val dialog = builder.create()

            // Check the theme to establish the color of the buttons
            if (requireContext().resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            ) {
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(R.color.white, null))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(R.color.white, null))
                }
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    /**
     * Interface with the method called when an option has been selected
     */
    interface DeleteDialogInterface {
        fun optionSelected(option: Boolean)
    }
}