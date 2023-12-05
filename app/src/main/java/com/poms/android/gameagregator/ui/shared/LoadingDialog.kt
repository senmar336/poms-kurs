package com.poms.android.gameagregator.ui.shared

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.poms.android.gameagregator.databinding.LoadingDialogBinding

class LoadingDialog : DialogFragment() {

    private var _binding: LoadingDialogBinding? = null
    private val binding get() = _binding!!

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Prevent the dialog get closed on an outside touch
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = LoadingDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the screen resume
     */
    override fun onResume() {
        super.onResume()
        // Prevent the dialog get closed on back button pressed
        dialog!!.setOnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
    }
}