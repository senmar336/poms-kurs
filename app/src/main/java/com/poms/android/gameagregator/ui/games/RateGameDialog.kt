package com.poms.android.gameagregator.ui.games

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.poms.android.gameagregator.databinding.RateGameDialogBinding
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.games.RateGameViewModel

class RateGameDialog : DialogFragment() {

    private var rateGameDialogInterface: RateGameDialogInterface? = null

    fun setRateGameDialogInterface(rateGameDialogInterface: RateGameDialogInterface?) {
        this.rateGameDialogInterface = rateGameDialogInterface
    }

    private var _binding: RateGameDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = DialogFragment()
    }

    // ViewModel
    private val viewModel: RateGameViewModel by viewModels()

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

        val rateGameSuccessful = Observer<Boolean> {
            if (it) {
                rateGameDialogInterface?.gameRated()
                Toast.makeText(
                    requireContext(),
                    requireActivity().getString(R.string.game_rated_successfully),
                    Toast.LENGTH_SHORT
                ).show()
                dismiss()
            }
        }

        val rateGameException = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
        }

        // Observe the viewModel liveData
        viewModel.rateGameSuccessful.observe(this, rateGameSuccessful)
        viewModel.rateGameException.observe(this, rateGameException)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        _binding = RateGameDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRateGame.setOnClickListener {
            if (validate()) {
                KeyboardManager.closeKeyboardDialogFragment(requireActivity(), binding.rateInput)
                viewModel.rateGame(binding.rateInput.editText?.text.toString().toInt())
            }
        }
    }

    /**
     * Validates the rate input
     */
    private fun validate(): Boolean {
        if (binding.rateInput.editText?.text.toString().isBlank()) {
            binding.rateInput.error = requireActivity().getString(R.string.error_required)
            return false
        }
        try {
            binding.rateInput.editText?.text.toString().toInt()
        } catch (e: Exception) {
            binding.rateInput.error = requireActivity().getString(R.string.error_rate_incorrect)
            return false
        }
        if (binding.rateInput.editText?.text.toString().toInt() > 100 || binding.rateInput.editText?.text.toString().toInt() < 0) {
            binding.rateInput.error = requireActivity().getString(R.string.error_rate_incorrect)
            return false
        }
        return true
    }

    /**
     * Interface with the method called when the game has been rated
     */
    interface RateGameDialogInterface {
        fun gameRated()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}