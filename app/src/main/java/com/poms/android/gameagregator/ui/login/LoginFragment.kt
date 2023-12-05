package com.poms.android.gameagregator.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.activities.MainActivity
import com.poms.android.gameagregator.databinding.LoginFragmentBinding
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.login.LoginViewModel

class LoginFragment : Fragment() {

    // ViewBinding object
    private var _binding: LoginFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = LoginFragment()
        private const val RC_SIGN_IN = 1007
    }

    // ViewModel
    private val viewModel: LoginViewModel by viewModels()
    // Client to log with Google
    private lateinit var googleSignInClient: GoogleSignInClient

    /**
     * Function launched when the fragment is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("749357893211-sdme2873jbfus4li3vsc1mmta2j6fl9q.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Success in the login
        val loginSuccessful = Observer<Boolean> { loginSuccessful ->
            if (loginSuccessful) {
                val intent = Intent(activity, MainActivity::class.java)
                (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
                startActivity(intent)
                requireActivity().finish()
            }
        }

        // Error in the login
        val loginException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_incorrect_user_or_password),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.loginSuccessful.observe(this, loginSuccessful)
        viewModel.loginException.observe(this, loginException)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LoginFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Press the login button
        binding.btnLogin.setOnClickListener {
            if (validateLogin()) {
                KeyboardManager.closeKeyboard((requireActivity()))
                LoadingDialog().show(childFragmentManager, "progress")
                viewModel.login(
                    binding.editTextEmail.editText?.text.toString(),
                    binding.editTextPassword.editText?.text.toString()
                )
            }
        }

        // Press the login with Google button
//        binding.btnLoginGoogle.setOnClickListener {
//            KeyboardManager.closeKeyboard(requireActivity())
//            val signInIntent = googleSignInClient.signInIntent
//            startActivityForResult(signInIntent, RC_SIGN_IN)
//        }

        // Press the register button
        binding.btnRegister.setOnClickListener {
            KeyboardManager.closeKeyboard((requireActivity()))
            RegisterFragment().show(childFragmentManager, "Register")
        }

        // Press the restore password button
        binding.btnRestorePassword.setOnClickListener {
            RestorePasswordFragment().show(childFragmentManager, "RestorePassword")
        }
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            viewModel.loginWithGoogle(it.data)
        }
    }

    /**
     * Function called with the result of the login with Google
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            viewModel.loginWithGoogle(data)
        }
    }

    /**
     * Validates the login fields
     */
    private fun validateLogin(): Boolean {
        val email = binding.editTextEmail.editText?.text.toString()
        val passwd = binding.editTextPassword.editText?.text.toString()

        if (email.isBlank()) {
            binding.editTextEmail.error = requireActivity().getString(R.string.error_required)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.error = requireActivity().getString(R.string.error_mail_not_valid)
            return false
        }

        if (passwd.isBlank()) {
            binding.editTextPassword.error = requireActivity().getString(R.string.error_required)
            return false
        } else if (passwd.length < 4) {
            binding.editTextPassword.error = requireActivity().getString(R.string.error_password_length)
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}