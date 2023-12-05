package com.poms.android.gameagregator.ui.games

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.api.igdb.utils.ImageSize
import com.api.igdb.utils.ImageType
import com.api.igdb.utils.imageBuilder
import com.google.android.material.card.MaterialCardView
import com.squareup.picasso.Picasso
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.models.entity.GameListEntity

class GamesListViewHolder(private val dataSet: List<GameListEntity>) :
    RecyclerView.Adapter<GamesListViewHolder.ViewHolder>() {

    private var recyclerViewGameInterface: RecyclerViewGameInterface? = null

    // Variable to show/hide delete button
    var showDeleteButton = false

    /**
     * Function to get the value of the delete dialog interface
     */
    fun setViewItemInterface(viewItemInterface: RecyclerViewGameInterface?) {
        this.recyclerViewGameInterface = viewItemInterface
    }

    /**
     * ViewHolder of the view
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.gameCard)
        val name: TextView = view.findViewById(R.id.textGameName)
        val textGameGenres: TextView = view.findViewById(R.id.textGameGenres)
        val textGamePlatforms: TextView = view.findViewById(R.id.textGamePlatforms)
        val cover: ImageView = view.findViewById(R.id.gameCoverImage)
        val rating: TextView = view.findViewById(R.id.textRate)
        val btnDelete: ImageView = view.findViewById(R.id.btnDeleteList)
    }

    /**
     * Create new views (invoked by the layout manager)
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.games_list_view_holder, viewGroup, false)

        return ViewHolder(view)
    }

    /**
     * Replace the contents of a view
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.name.text = dataSet[position].name
        viewHolder.textGameGenres.text = dataSet[position].genres
        viewHolder.textGamePlatforms.text = dataSet[position].platforms
        viewHolder.rating.text = dataSet[position].rating.toString()

        Picasso.get()
            .load(imageBuilder(dataSet[position].imageId, ImageSize.COVER_SMALL, ImageType.PNG))
            .error(R.drawable.ic_baseline_no_image_24)
            .into(viewHolder.cover)

        viewHolder.card.setOnClickListener {
            recyclerViewGameInterface?.onItemClick(dataSet[position])
        }

        if (showDeleteButton) {
            viewHolder.btnDelete.visibility = View.VISIBLE

            viewHolder.btnDelete.setOnClickListener {
                recyclerViewGameInterface?.onDeleteClick(dataSet[position])
            }
        } else {
            viewHolder.btnDelete.visibility = View.GONE
        }
    }

    /**
     * Return the size of dataSet
     */
    override fun getItemCount() = dataSet.size

    /**
     * Interface of the recyclerview
     */
    interface RecyclerViewGameInterface {
        fun onItemClick(gameListEntity: GameListEntity)
        fun onDeleteClick(gameListEntity: GameListEntity)
    }
}