package com.poms.android.gameagregator.ui.calendar

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.CalendarEventItemViewBinding
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.utils.ListType

class CalendarViewHolder : RecyclerView.Adapter<CalendarViewHolder.ViewHolder>() {

    val games = mutableListOf<CalendarGameEntity>()

    /**
     * ViewHolder of the view
     */
    class ViewHolder(val binding: CalendarEventItemViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(game: CalendarGameEntity) {
            binding.calendarListName.text = game.listName
            binding.calendarGameName.text = game.gameName
            binding.calendarDateText.text = game.dateGameAddedToList
            when (game.listName) {
                ListType.PLAYING -> {
                    binding.calendarListName.setBackgroundColor(this.itemView.context.resources.getColor(R.color.blue))
                    binding.calendarDateTextTitle.text = this.itemView.context.getString(R.string.calendar_date_playing_title)
                }
                ListType.COMPLETED -> {
                    binding.calendarListName.setBackgroundColor(this.itemView.context.resources.getColor(R.color.green))
                    binding.calendarDateTextTitle.text = this.itemView.context.getString(R.string.calendar_date_completed_title)
                    binding.calendarListName.setTextColor(this.itemView.context.resources.getColor(R.color.black))
                }
                ListType.ABANDONED -> {
                    binding.calendarListName.setBackgroundColor(this.itemView.context.resources.getColor(R.color.red))
                    binding.calendarDateTextTitle.text = this.itemView.context.getString(R.string.calendar_date_abandoned_title)
                }
            }
        }
    }

    /**
     * Create new views
     */
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            CalendarEventItemViewBinding.inflate(parent.context.layoutInflater, parent, false)
        )
    }

    /**
     * Replace the contents of a view
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(games[position])
    }

    /**
     * Return the size of dataSet
     */
    override fun getItemCount(): Int {
        return games.size
    }
}