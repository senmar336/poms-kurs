package com.poms.android.gameagregator.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.CreateListDialogBinding
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.lists.CreateListViewModel

class CreateListFragment : DialogFragment() {

    private var _binding: CreateListDialogBinding? = null
    private val binding get() = _binding!!

    private var createListInterface: CreateListInterface? = null

    /**
     * Function to get the value of the delete dialog interface
     */
    fun setViewItemInterface(createListInterface: CreateListInterface?) {
        this.createListInterface = createListInterface
    }

    // ViewModel
    private val viewModel: CreateListViewModel by viewModels()

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

        val createListSuccessful = Observer<Boolean> {
            viewModel.createListInterface?.listCreated()
            dismiss()
        }

        val createListError = Observer<Exception> {
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.createListSuccessful.observe(this, createListSuccessful)
        viewModel.createListError.observe(this, createListError)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CreateListDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load the information checking if the screen is new or is reloading
        if (savedInstanceState == null) {
            viewModel.createListInterface = createListInterface
        }

        // Press the create list button
        binding.btnCreateList.setOnClickListener {
            if (binding.textListName.editText?.text.toString().isBlank()) {
                binding.textListName.error = requireActivity().getString(R.string.error_required)
            } else {
                KeyboardManager.closeKeyboardDialogFragment(requireActivity(), binding.textListName)
                viewModel.createList(binding.textListName.editText?.text.toString())
            }
        }
    }

    /**
     * Interface with the method called when the list has been created
     */
    interface CreateListInterface {
        fun listCreated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}