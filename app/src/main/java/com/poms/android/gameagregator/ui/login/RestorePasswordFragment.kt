package com.poms.android.gameagregator.ui.login

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.RestorePasswordFragmentBinding
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.login.RestorePasswordViewModel

class RestorePasswordFragment : DialogFragment() {

    // ViewBinding object
    private var _binding: RestorePasswordFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = RestorePasswordFragment()
    }

    // ViewModel
    private val viewModel: RestorePasswordViewModel by viewModels()

    /**
     * Function called on start. Sets the dialog fragment size
     */
    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Function launched when the fragment is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Success sending the restore password email
        val restorePasswordSuccessful = Observer<Boolean> { restorePasswordSuccessful ->
            if (restorePasswordSuccessful) {
                (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

                Toast.makeText(
                    requireContext(),
                    requireActivity().getString(R.string.recover_password_email_send),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }

        // Error sending the restore password email
        val restorePasswordException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_does_not_exist),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the viewModel
        viewModel.restorePasswordSuccessful.observe(this, restorePasswordSuccessful)
        viewModel.restorePasswordException.observe(this, restorePasswordException)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = RestorePasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Press the button of restore password
        binding.btnRestorePassword.setOnClickListener {
            if (validate()) {
                LoadingDialog().show(childFragmentManager, "progress")
                KeyboardManager.closeKeyboardDialogFragment(requireActivity(), binding.textEmail)
                viewModel.restorePassword(binding.textEmail.editText?.text.toString())
            }
        }
    }

    /**
     * Validates the input fields
     */
    private fun validate(): Boolean {
        val email = binding.textEmail.editText?.text.toString()

        if (email.isBlank()) {
            binding.textEmail.error = requireActivity().getString(R.string.error_required)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textEmail.error = requireActivity().getString(R.string.error_mail_not_valid)
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}