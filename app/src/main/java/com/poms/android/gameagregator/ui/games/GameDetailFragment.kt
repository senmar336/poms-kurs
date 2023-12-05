package com.poms.android.gameagregator.ui.games

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
import com.squareup.picasso.Picasso
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.GameDetailFragmentBinding
import com.poms.android.gameagregator.models.entity.GameDetailEntity
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.ui.shared.DeleteDialog
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.viewmodel.games.GameDetailViewModel

class GameDetailFragment : Fragment() {

    companion object {
        fun newInstance() = GameDetailFragment()
    }

    private var _binding: GameDetailFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: GameDetailViewModel by viewModels()

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
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

        val getGameDetailSuccessful = Observer<GameDetailEntity> {
            setGameData()
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        val getGameDetailError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().onBackPressed()
        }

        val getListsOfGameSuccessful = Observer<List<ListEntity>> {
            setListsOfGame(it)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        val getListError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        val getUserRateSuccessful = Observer<Int?> {
            showUserRate()
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        val getUserRateError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        val deleteUserRateSuccessful = Observer<Boolean> {
            showUserRate()
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        val deleteUserRateError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the viewModel liveData
        viewModel.gameDetails.observe(this, getGameDetailSuccessful)
        viewModel.getGameDetailError.observe(this, getGameDetailError)
        viewModel.listsOfGame.observe(this, getListsOfGameSuccessful)
        viewModel.getListError.observe(this, getListError)
        viewModel.getUserRateSuccessful.observe(this, getUserRateSuccessful)
        viewModel.getUserRateError.observe(this, getUserRateError)
        viewModel.deleteUserRateSuccessful.observe(this, deleteUserRateSuccessful)
        viewModel.deleteUserRateError.observe(this, deleteUserRateError)
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            LoadingDialog().show(childFragmentManager, "progress")
            viewModel.getGameDetail()
            viewModel.getUserLists()
            viewModel.getRate()
        } else {
            setGameData()
            showUserRate()
        }

        binding.btnAddList.setOnClickListener {
            val fragment = AddGameToListDialog()

            val arguments = Bundle()
            arguments.putString("gameId", viewModel.gameId)
            fragment.arguments = arguments

            // Refresh lists of game when the game is added to a list
            fragment.setAddGameToListDialogInterface(object : AddGameToListDialog.AddGameToListDialogInterface {
                override fun gameAddedToList() {
                    viewModel.getListsOfGame()
                }
            })
            fragment.show(childFragmentManager, "AddGameToListDialog")
        }

        binding.btnRate.setOnClickListener {
            val fragment = RateGameDialog()

            val arguments = Bundle()
            arguments.putString("gameId", viewModel.gameId)
            fragment.arguments = arguments

            // Refresh the user rate when the game has been rated
            fragment.setRateGameDialogInterface(object : RateGameDialog.RateGameDialogInterface {
                override fun gameRated() {
                    viewModel.getRate()
                }
            })
            fragment.show(childFragmentManager, "AddGameToListDialog")
        }

        binding.btnRateDelete.setOnClickListener {
            val fragment = DeleteDialog()

            fragment.setDeleteDialogInterface(object : DeleteDialog.DeleteDialogInterface {
                override fun optionSelected(option: Boolean) {
                    if (option) {
                        viewModel.deleteRate()
                    }
                }
            })
            fragment.show(childFragmentManager, "deleteDialog")
        }
    }

    /**
     * Sets the game data
     */
    private fun setGameData() {
        binding.gameName.text = viewModel.gameDetails.value!!.name
        binding.gameRate.text = viewModel.gameDetails.value!!.rating.toString()
        binding.textGameDate.text = viewModel.gameDetails.value!!.firstReleaseDate
        binding.textGameGenres.text = viewModel.gameDetails.value!!.genres
        binding.textGamePlatforms.text = viewModel.gameDetails.value!!.platforms

        if (viewModel.gameDetails.value!!.summary.length > 1) {
            binding.description.text = viewModel.gameDetails.value!!.summary
        } else {
            binding.description.text = viewModel.gameDetails.value!!.storyline
        }

        Picasso.get()
            .load(imageBuilder(viewModel.gameDetails.value!!.imageId, ImageSize.COVER_BIG, ImageType.PNG))
            .error(R.drawable.ic_baseline_no_image_24)
            .into(binding.gameCoverImage)

        if (viewModel.gameDetails.value!!.artworksIds.isNotEmpty()) {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.setMargins(5, 0, 5, 0)

            val horizontalScrollLinearLayout = binding.horizontalScrollLinearLayout
            viewModel.gameDetails.value!!.artworksIds.forEachIndexed { _, image ->
                val imageView = layoutInflater.inflate(R.layout.carousel_image, null) as ImageView
                Picasso.get()
                    .load(imageBuilder(image, ImageSize.COVER_BIG, ImageType.PNG))
                    .error(R.drawable.ic_baseline_no_image_24)
                    .into(imageView)
                imageView.layoutParams = params
                horizontalScrollLinearLayout.addView(imageView)
            }

        } else {
            binding.textNoImages.visibility = View.VISIBLE
        }
    }

    /**
     * Sets the lists in which the game is included
     */
    private fun setListsOfGame(lists: List<ListEntity>) {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(5, 0, 5, 0)
        //val verticalLayout = binding.listsVerticalLayout
        lists.forEachIndexed { index, listEntity ->
            val horizontalLayout = binding.listsHorizontalLayout1
            if (index == 0) horizontalLayout.removeAllViews()
            val button = layoutInflater.inflate(R.layout.game_list_button, null) as Button
            button.text = listEntity.name
            button.layoutParams = params
            horizontalLayout.addView(button)
        }
    }

    /**
     * Shows/hides the rate button or the user rate when the game has been rated
     */
    private fun showUserRate() {
        if (viewModel.getUserRateSuccessful.value != null) {
            binding.btnRateDelete.text = requireActivity().getString(R.string.your_rate).plus(" ")
                .plus(viewModel.getUserRateSuccessful.value.toString())
            binding.btnRate.visibility = View.GONE
            binding.btnRateDelete.visibility = View.VISIBLE
        } else {
            binding.btnRate.visibility = View.VISIBLE
            binding.btnRateDelete.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}