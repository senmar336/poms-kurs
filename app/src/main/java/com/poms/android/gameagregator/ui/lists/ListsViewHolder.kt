package com.poms.android.gameagregator.ui.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.ListsViewHolderBinding
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.utils.ListType

class ListsViewHolder(private val dataSet: List<ListEntity>) :
    RecyclerView.Adapter<ListsViewHolder.ViewHolder>() {

    private var recyclerViewListInterface: RecyclerViewListInterface? = null

    /**
     * Function to get the value of the delete dialog interface
     */
    fun setViewItemInterface(viewItemInterface: RecyclerViewListInterface?) {
        this.recyclerViewListInterface = viewItemInterface
    }

    /**
     * ViewHolder of the view
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ListsViewHolderBinding.bind(view)
        val listCard: MaterialCardView = binding.listCard
        val listName: TextView = binding.textViewNameList
        val btnShare: ImageView = binding.btnShare
        val btnDelete: ImageView = binding.btnDelete
    }

    /**
     * Create new views
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.lists_view_holder, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Replace the contents of a view
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.listName.text = dataSet[position].name

        // Default lists cannot be deleted or shared
        if (dataSet[position].type == ListType.DEFAULT) {
            viewHolder.btnShare.visibility = View.GONE
            viewHolder.btnDelete.visibility = View.GONE

        } else {
            // Click on the share button
            viewHolder.btnShare.setOnClickListener {
                recyclerViewListInterface?.onShareClick(dataSet[position])
            }

            // Click on the delete button
            viewHolder.btnDelete.setOnClickListener {
                recyclerViewListInterface?.onDeleteClick(dataSet[position])
            }
        }

        // Click on the list
        viewHolder.listCard.setOnClickListener {
            recyclerViewListInterface?.onListClick(dataSet[position])
        }
    }

    /**
     * Return the size of dataSet
     */
    override fun getItemCount() = dataSet.size

    /**
     * list's interface
     */
    interface RecyclerViewListInterface {
        fun onListClick(listEntity: ListEntity)
        fun onShareClick(listEntity: ListEntity)
        fun onDeleteClick(listEntity: ListEntity)
    }
}