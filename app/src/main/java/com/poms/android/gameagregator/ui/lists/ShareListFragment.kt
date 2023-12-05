package com.poms.android.gameagregator.ui.lists

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
import com.google.firebase.auth.FirebaseAuth
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.ShareListFragmentBinding
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.lists.ShareListViewModel

class ShareListFragment : DialogFragment() {

    companion object {
        fun newInstance() = ShareListFragment()
    }

    private var _binding: ShareListFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: ShareListViewModel by viewModels()

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
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        _binding = ShareListFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the fragment is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the information checking if the screen is new or is reloading
        if (savedInstanceState == null) {
            val bundle = this.arguments
            if (bundle != null) {
                viewModel.listId = bundle.getString("listId", "")
                viewModel.listName = bundle.getString("listName", "")
                viewModel.listType = bundle.getString("listType", "")
            }
        }

        // Success sharing the list
        val shareListSuccessful = Observer<Boolean> { shareListSuccessful ->
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

            if (shareListSuccessful) {
                Toast.makeText(
                    requireContext(),
                    requireActivity().getString(R.string.list_shared_successfully),
                    Toast.LENGTH_SHORT
                ).show()

                dismiss()
            }
        }

        // Error sharing the list
        val shareListException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Error: The user already has the list
        val userAlreadyHasListError = Observer<Boolean> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_user_already_has_list),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Error: User does not exist
        val userDoesNotExistError = Observer<Boolean> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_does_not_exist),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.shareListSuccessful.observe(this, shareListSuccessful)
        viewModel.shareListException.observe(this, shareListException)
        viewModel.userDoesNotExistError.observe(this, userDoesNotExistError)
        viewModel.userAlreadyHasListError.observe(this, userAlreadyHasListError)
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Click the share button
        binding.btnShareList.setOnClickListener {
            if (validate()) {
                LoadingDialog().show(childFragmentManager, "progress")
                KeyboardManager.closeKeyboardDialogFragment(requireActivity(), binding.textEmail)
                viewModel.shareList(binding.textEmail.editText?.text.toString())
            }
        }
    }

    /**
     * Validates the input field
     */
    private fun validate(): Boolean {
        val email = binding.textEmail.editText?.text.toString()

        if (email.isBlank()) {
            binding.textEmail.error = requireActivity().getString(R.string.error_required)
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.textEmail.error = requireActivity().getString(R.string.error_mail_not_valid)
            return false
        } else if (email == FirebaseAuth.getInstance().currentUser!!.email) {
            binding.textEmail.error = requireActivity().getString(R.string.error_same_email)
            return false
        }

        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}