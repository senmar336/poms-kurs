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
import com.poms.android.gameagregator.databinding.ListsFragmentBinding
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.ui.shared.DeleteDialog
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.viewmodel.lists.ListsViewModel

class ListsFragment : Fragment() {

    companion object {
        fun newInstance() = ListsFragment()
    }

    private var _binding: ListsFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: ListsViewModel by viewModels()

    /**
     * Function launched when the fragment is started
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load the information checking if the screen is new or is reloading
        if (savedInstanceState == null) {
            LoadingDialog().show(childFragmentManager, "progress")
            viewModel.getUserLists()
        }

        // Success getting lists
        val getListSuccessful = Observer<List<ListEntity>> { list ->
            setRecycleViewList(list, true)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        // Error getting lists
        val getListError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Success deleting list
        val deleteListSuccessful = Observer<Boolean> {
            viewModel.getUserLists()
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
        viewModel.getListSuccessful.observe(this, getListSuccessful)
        viewModel.getListError.observe(this, getListError)
        viewModel.deleteListSuccessful.observe(this, deleteListSuccessful)
        viewModel.deleteListError.observe(this, deleteListError)
    }

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the view is created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecycleViewList(listOf(), false)

        // Press the create list button
        binding.btnCreateList.setOnClickListener {
            val fragment = CreateListFragment()

            fragment.setViewItemInterface(object : CreateListFragment.CreateListInterface {
                override fun listCreated() {
                    viewModel.getUserLists()
                }
            })

            fragment.show(childFragmentManager, "createList")
        }
    }

    /**
     * Function launched when the screen resume
     */
    override fun onResume() {
        super.onResume()
        if (viewModel.getListSuccessful.value != null) {
            setRecycleViewList(viewModel.getListSuccessful.value!!, true)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }
    }

    /**
     * Set the content of the recycler view
     *
     * @param listEntity: List of lists
     * @param listLoaded: Check to know if the list has been loaded or not to show the empty list message
     */
    private fun setRecycleViewList(listEntity: List<ListEntity>, listLoaded: Boolean) {
        val mAdapter = ListsViewHolder(listEntity)
        val mLayoutManager = LinearLayoutManager(requireContext())

        mAdapter.setViewItemInterface(object : ListsViewHolder.RecyclerViewListInterface {
            override fun onListClick(listEntity: ListEntity) {
                val fragment = ListDetailFragment()

                val arguments = Bundle()
                arguments.putString("listId", listEntity.id)
                arguments.putString("listName", listEntity.name)
                arguments.putString("listType", listEntity.type)
                fragment.arguments = arguments

                fragment.setListDetailInterfaceInterface(object : ListDetailFragment.ListDetailInterface {
                    override fun listDeleted() {
                        viewModel.getUserLists()
                    }
                })

                val fragmentTransaction: FragmentTransaction =
                    activity!!.supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, fragment)
                fragmentTransaction.addToBackStack(null)
                fragmentTransaction.commit()
            }

            /**
             * Press the share button of the list
             */
            override fun onShareClick(listEntity: ListEntity) {
                val fragment = ShareListFragment()

                val arguments = Bundle()
                arguments.putString("listId", listEntity.id)
                arguments.putString("listName", listEntity.name)
                arguments.putString("listType", listEntity.type)
                fragment.arguments = arguments

                fragment.show(childFragmentManager, "ShareListFragment")
            }

            /**
             * Press the delete button of the list
             */
            override fun onDeleteClick(listEntity: ListEntity) {
                val fragment = DeleteDialog()

                fragment.setDeleteDialogInterface(object : DeleteDialog.DeleteDialogInterface {
                    override fun optionSelected(option: Boolean) {
                        if (option) {
                            viewModel.deleteList(listEntity.id)
                        }
                    }
                })

                fragment.show(childFragmentManager, "deleteDialog")
            }

        })

        binding.recyclerViewList.layoutManager = mLayoutManager
        binding.recyclerViewList.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewList.adapter = mAdapter

        // Control if we should show the screen of empty list or the list itself
        if (listLoaded) {
            if (listEntity.isNotEmpty()) {
                binding.textEmptyList.visibility = View.GONE
                binding.recyclerViewList.visibility = View.VISIBLE
            } else {
                binding.textEmptyList.visibility = View.VISIBLE
                binding.recyclerViewList.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
