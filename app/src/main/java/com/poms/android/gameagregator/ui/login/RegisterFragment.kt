package com.poms.android.gameagregator.ui.login

import android.content.Intent
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
import com.poms.android.gameagregator.activities.MainActivity
import com.poms.android.gameagregator.databinding.RegisterFragmentBinding
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.login.RegisterViewModel

class RegisterFragment : DialogFragment() {

    // ViewBinding object
    private var _binding: RegisterFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = RegisterFragment()
    }

    // ViewModel
    private val viewModel: RegisterViewModel by viewModels()

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

        // Success with the register
        val registerSuccessful = Observer<Boolean> { loginSuccessful ->
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

            if (loginSuccessful) {
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        // Error with the register
        val registerException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_user_already_exist),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the viewModel liveData
        viewModel.registerSuccessful.observe(this, registerSuccessful)
        viewModel.registerException.observe(this, registerException)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = RegisterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Press the sing in button
        binding.btnSingIn.setOnClickListener {
            if (validate()) {
                LoadingDialog().show(childFragmentManager, "progress")
                KeyboardManager.closeKeyboardDialogFragment(requireActivity(), binding.editTextRegisterPassword)

                viewModel.createUser(
                    binding.editTextEmail.editText?.text.toString(),
                    binding.editTextRegisterPassword.editText?.text.toString()
                )
            }
        }
    }

    /**
     * Validates the input fields
     */
    private fun validate(): Boolean {
        val email = binding.editTextEmail.editText?.text.toString()
        val passwd = binding.editTextRegisterPassword.editText?.text.toString()

        if (email.isBlank()) {
            binding.editTextEmail.error = requireActivity().getString(R.string.error_required)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = requireActivity().getString(R.string.error_mail_not_valid)
            return false
        }

        if (passwd.isBlank()) {
            binding.editTextRegisterPassword.error =
                requireActivity().getString(R.string.error_required)
            return false
        } else if (passwd.length < 4) {
            binding.editTextRegisterPassword.error =
                requireActivity().getString(R.string.error_password_length)
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}