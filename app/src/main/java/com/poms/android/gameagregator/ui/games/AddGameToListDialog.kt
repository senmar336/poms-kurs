package com.poms.android.gameagregator.ui.games

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.AddGameToListBinding
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.viewmodel.games.AddGameToListViewModel

class AddGameToListDialog : DialogFragment() {

    private var addGameToListDialogInterface: AddGameToListDialogInterface? = null

    fun setAddGameToListDialogInterface(addGameToListDialogInterface: AddGameToListDialogInterface?) {
        this.addGameToListDialogInterface = addGameToListDialogInterface
    }

    private var _binding: AddGameToListBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = DialogFragment()
    }

    // ViewModel
    private val viewModel: AddGameToListViewModel by viewModels()

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

        val bundle = this.arguments
        if (bundle != null) {
            viewModel.gameId = bundle.getString("gameId", "")
        }
        viewModel.getUserLists()

        val getAvailableListsSuccessful = Observer<List<ListEntity>> { lists ->
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            createButtons(lists)
        }

        val getAvailableListsError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
            viewModel.getUserLists()
        }

        val addGameToListSuccessful = Observer<Boolean> { gameAddedSuccessful ->
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            if (gameAddedSuccessful) {
                Toast.makeText(
                    requireContext(),
                    requireActivity().getString(R.string.game_added_list_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                addGameToListDialogInterface?.gameAddedToList()
                dismiss()
            }
        }

        val addGameToListException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the viewModel liveData
        viewModel.getAvailableListsSuccessful.observe(this, getAvailableListsSuccessful)
        viewModel.getAvailableListsError.observe(this, getAvailableListsError)
        viewModel.addGameToListSuccessful.observe(this, addGameToListSuccessful)
        viewModel.addGameToListException.observe(this, addGameToListException)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = AddGameToListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Creates the buttons to add the game to a lists
     */
    private fun createButtons(lists: List<ListEntity>) {
        val layout = binding.linearLayout
        lists.forEach { listEntity ->
            val button = layoutInflater.inflate(R.layout.game_list_button, null) as Button
            button.text = listEntity.name
            button.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            button.setOnClickListener {
                viewModel.addGameToList(listEntity, viewModel.gameId)
            }
            layout.addView(button)
        }
    }

    /**
     * Interface with the method called when the game has been added
     */
    interface AddGameToListDialogInterface {
        fun gameAddedToList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}