package com.poms.android.gameagregator.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.SearchFragmentBinding
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.ui.games.GameDetailFragment
import com.poms.android.gameagregator.ui.games.GamesListViewHolder
import com.poms.android.gameagregator.utils.KeyboardManager
import com.poms.android.gameagregator.viewmodel.search.SearchViewModel

class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: SearchViewModel by viewModels()

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Success searching games
        val getSearchResultSuccessful = Observer<List<GameListEntity>> { list ->
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            if (list.isNotEmpty()) {
                setRecycleViewList(list)
            } else {
                Toast.makeText(
                    requireContext(),
                    requireActivity().getString(R.string.error_no_result),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Error searching games
        val getSearchResultError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.getSearchResultSuccessful.observe(this, getSearchResultSuccessful)
        viewModel.getSearchResultError.observe(this, getSearchResultError)
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecycleViewList(emptyList())

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                KeyboardManager.closeKeyboard(requireActivity())
                if (query != null) {
                    viewModel.searchGamesByName(query)
                }
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })
    }

    /**
     * Set the content of the recycler view
     */
    private fun setRecycleViewList(gamesList: List<GameListEntity>) {
        val mAdapter = GamesListViewHolder(gamesList)
        val mLayoutManager = LinearLayoutManager(requireContext())

        mAdapter.setViewItemInterface(object : GamesListViewHolder.RecyclerViewGameInterface {
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

            override fun onDeleteClick(gameListEntity: GameListEntity) {}
        })

        binding.searchedGamesList.layoutManager = mLayoutManager
        binding.searchedGamesList.itemAnimator = DefaultItemAnimator()
        binding.searchedGamesList.adapter = mAdapter

        if (gamesList.isEmpty()) {
            binding.textNoResults.visibility = View.VISIBLE
            binding.searchedGamesList.visibility = View.GONE
        } else {
            binding.textNoResults.visibility = View.GONE
            binding.searchedGamesList.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}