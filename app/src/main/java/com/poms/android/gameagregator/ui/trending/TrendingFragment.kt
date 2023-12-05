package com.poms.android.gameagregator.ui.trending

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
import com.poms.android.gameagregator.databinding.TrendingFragmentBinding
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.ui.games.GameDetailFragment
import com.poms.android.gameagregator.ui.games.GamesListViewHolder
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.viewmodel.trending.TrendingViewModel

class TrendingFragment : Fragment() {

    companion object {
        fun newInstance() = TrendingFragment()
    }

    private var _binding: TrendingFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: TrendingViewModel by viewModels()

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TrendingFragmentBinding.inflate(inflater, container, false)
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
            viewModel.getTrendingGames()
        }

        // Error getting trending games
        val getTrendingGamesError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()

            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Success getting trending games
        val getTrendingGamesSuccessful = Observer<List<GameListEntity>> {
            setRecycleViewList(it)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        // Observe the liveData of the viewModel
        viewModel.getTrendingGamesError.observe(this, getTrendingGamesError)
        viewModel.getTrendingGamesSuccessful.observe(this, getTrendingGamesSuccessful)
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycleViewList(emptyList())
    }

    /**
     * Function launched when the screen resume
     */
    override fun onResume() {
        super.onResume()
        if (viewModel.getTrendingGamesSuccessful.value != null) {
            setRecycleViewList(viewModel.getTrendingGamesSuccessful.value!!)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }
    }

    /**
     * Set the content of the recycler view
     */
    @Suppress("UNSAFE_CALL_ON_PARTIALLY_DEFINED_RESOURCE")
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

        binding.trendingGames.layoutManager = mLayoutManager
        binding.trendingGames.itemAnimator = DefaultItemAnimator()
        binding.trendingGames.adapter = mAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}