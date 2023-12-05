package com.poms.android.gameagregator.ui.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.ListDetailFragmentBinding
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.ui.games.GameDetailFragment
import com.poms.android.gameagregator.ui.games.GamesListViewHolder
import com.poms.android.gameagregator.ui.shared.DeleteDialog
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.ListType
import com.poms.android.gameagregator.viewmodel.lists.ListDetailViewModel

class ListDetailFragment : Fragment() {

    private var listDetailInterface: ListDetailInterface? = null

    fun setListDetailInterfaceInterface(ListDetailInterface: ListDetailInterface?) {
        this.listDetailInterface = ListDetailInterface
    }

    private var _binding: ListDetailFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: ListDetailViewModel by viewModels()

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the fragment is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the information checking if the screen is new or is reloading
        if (savedInstanceState == null) {
            LoadingDialog().show(childFragmentManager, "progress")

            val bundle = this.arguments
            if (bundle != null) {
                viewModel.listId = bundle.getString("listId", "")
                viewModel.listName = bundle.getString("listName", "")
                viewModel.listType = bundle.getString("listType", "")
            }

            viewModel.getGames()
        }

        // Error getting the games
        val getGamesError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Success getting the games
        val getGamesSuccessful = Observer<List<GameListEntity>> {
            setRecycleViewList(it, true)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        // Error deleting the games from the list
        val deleteGamesError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Success deleting the games from the list
        val deleteGamesSuccessful = Observer<Boolean> {
            viewModel.getGames()
        }

        // Success deleting list
        val deleteListSuccessful = Observer<Boolean> {
            listDetailInterface?.listDeleted()
            activity?.supportFragmentManager?.popBackStack()
        }

        // Error deleting list
        val deleteListError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.getGamesSuccessful.observe(this, getGamesSuccessful)
        viewModel.getGamesError.observe(this, getGamesError)
        viewModel.deleteGamesSuccessful.observe(this, deleteGamesSuccessful)
        viewModel.deleteGamesError.observe(this, deleteGamesError)
        viewModel.deleteListSuccessful.observe(this, deleteListSuccessful)
        viewModel.deleteListError.observe(this, deleteListError)
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycleViewList(listOf(), false)
        binding.textListName.text = viewModel.listName

        // Default lists cannot be deleted or shared
        if (viewModel.listType == ListType.DEFAULT) {
            binding.btnShare.visibility = View.GONE
            binding.btnDeleteList.visibility = View.GONE

        } else {
            // Press the delete list button
            binding.btnDeleteList.setOnClickListener {
                val fragment = DeleteDialog()

                fragment.setDeleteDialogInterface(object : DeleteDialog.DeleteDialogInterface {
                    override fun optionSelected(option: Boolean) {
                        if (option) {
                            viewModel.deleteList(viewModel.listId)
                        }
                    }
                })

                fragment.show(childFragmentManager, "deleteDialog")
            }

            // Press on the share button
            binding.btnShare.setOnClickListener {
                val fragment = ShareListFragment()

                val arguments = Bundle()
                arguments.putString("listId", viewModel.listId)
                arguments.putString("listName", viewModel.listName)
                arguments.putString("listType", viewModel.listType)
                fragment.arguments = arguments

                fragment.show(childFragmentManager, "ShareListFragment")
            }
        }
    }

    /**
     * Function launched when the screen resume
     */
    override fun onResume() {
        super.onResume()
        if (viewModel.getGamesSuccessful.value != null) {
            setRecycleViewList(viewModel.getGamesSuccessful.value!!, true)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }
    }

    /**
     * Set the content of the recycler view
     */
    @Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")
    private fun setRecycleViewList(gameListEntity: List<GameListEntity>, gamesLoaded: Boolean) {
        val mAdapter = GamesListViewHolder(gameListEntity)
        val mLayoutManager = LinearLayoutManager(requireContext())

        mAdapter.setViewItemInterface(object : GamesListViewHolder.RecyclerViewGameInterface {
            /**
             * Click on an element of the list
             */
            override fun onItemClick(gameListEntity: GameListEntity) {
                val fragment = GameDetailFragment()

                val arguments = Bundle()
                arguments.putString("gameId", gameListEntity.id)

                if (binding.gameDetailFragmentView != null) {
                    arguments.putInt("parentId", binding.gameDetailFragmentView!!.id)
                }

                fragment.arguments = arguments
                val fragmentTransaction: FragmentTransaction =
                    activity!!.supportFragmentManager.beginTransaction()

                if (binding.gameDetailFragmentView != null) {
                    fragmentTransaction.replace(binding.gameDetailFragmentView!!.id, fragment)
                } else {
                    fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, fragment)
                }

                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            /**
             * Click on the delete option of the game
             */
            override fun onDeleteClick(gameListEntity: GameListEntity) {
                val fragment = DeleteDialog()

                fragment.setDeleteDialogInterface(object : DeleteDialog.DeleteDialogInterface {
                    override fun optionSelected(option: Boolean) {
                        if (option) {
                            viewModel.deleteGameFromList(gameListEntity.id)
                        }
                    }
                })

                fragment.show(childFragmentManager, "deleteDialog")
            }
        })

        mAdapter.showDeleteButton = true

        binding.gamesList.layoutManager = mLayoutManager
        binding.gamesList.itemAnimator = DefaultItemAnimator()
        binding.gamesList.adapter = mAdapter

        // Show a message informing that there is no games on the list
        if (gamesLoaded) {
            if (gameListEntity.isNotEmpty()) {
                binding.textEmptyList.visibility = View.GONE
                binding.gamesList.visibility = View.VISIBLE

                if (binding.gameDetailFragmentView != null) {
                    binding.gameDetailFragmentView!!.visibility = View.VISIBLE
                }
            } else {
                binding.textEmptyList.visibility = View.VISIBLE
                binding.gamesList.visibility = View.GONE

                if (binding.gameDetailFragmentView != null) {
                    binding.gameDetailFragmentView!!.visibility = View.GONE
                }
            }
        }
    }

    /**
     * Interface with the method called when the list has been removed
     */
    interface ListDetailInterface {
        fun listDeleted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}