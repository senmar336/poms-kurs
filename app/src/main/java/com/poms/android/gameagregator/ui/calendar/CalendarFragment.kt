package com.poms.android.gameagregator.ui.calendar

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.poms.android.gameagregator.R
import com.poms.android.gameagregator.databinding.CalendarDayBinding
import com.poms.android.gameagregator.databinding.CalendarFragmentBinding
import com.poms.android.gameagregator.databinding.CalendarHeaderBinding
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.ui.shared.LoadingDialog
import com.poms.android.gameagregator.utils.ListType
import com.poms.android.gameagregator.viewmodel.calendar.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment : Fragment() {

    companion object {
        fun newInstance() = CalendarFragment()
    }

    private var _binding: CalendarFragmentBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: CalendarViewModel by viewModels()

    private lateinit var calendarGamesMap: Map<String, List<CalendarGameEntity>>
    private var selectedDate: LocalDate? = null

    // Pattern to format dates
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    @RequiresApi(Build.VERSION_CODES.O)
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    /**
     * Function launched with the creation of the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CalendarFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Function launched when the fragment is started
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            LoadingDialog().show(childFragmentManager, "progress")
            viewModel.getCalendarGames()
        }

        // Success getting calendar games
        val getCalendarGamesSuccessful = Observer<Map<String, List<CalendarGameEntity>>> {
            calendarGamesMap = it
            buildCalendarView(calendarGamesMap)
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
        }

        // Error getting calendar games
        val getCalendarGamesError = Observer<Exception> {
            (childFragmentManager.findFragmentByTag("progress") as? DialogFragment)?.dismiss()
            Toast.makeText(
                requireContext(),
                requireActivity().getString(R.string.error_unknown),
                Toast.LENGTH_SHORT
            ).show()
        }

        // Observe the liveData of the ViewModel
        viewModel.getGamesFromDefaultListSuccessful.observe(this, getCalendarGamesSuccessful)
        viewModel.getGamesFromDefaultListError.observe(this, getCalendarGamesError)
    }

    /**
     * Sets the data for the calendar view
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildCalendarView(calendarGames: Map<String, List<CalendarGameEntity>>) {
        val mAdapter = CalendarViewHolder()

        binding.calendarRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = mAdapter
            addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
        }
        mAdapter.notifyDataSetChanged()

        // Set the months and days to the calendar
        val daysOfWeek = DayOfWeek.values()
        val currentMonth = YearMonth.now()
        binding.calendarView.setup(currentMonth.minusMonths(5), currentMonth.plusMonths(5), daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val calendarDayBinding = CalendarDayBinding.bind(view)
            init {
                view.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH) {
                        if (selectedDate != day.date) {
                            val oldDate = selectedDate
                            selectedDate = day.date
                            val binding = this@CalendarFragment.binding
                            binding.calendarView.notifyDateChanged(day.date)
                            oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                            updateAdapterForDate(mAdapter, day.date)
                        }
                    }
                }
            }
        }

        // Build the day cells with it corresponding color
        binding.calendarView.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.calendarDayBinding.calendarDayText
                val layout = container.calendarDayBinding.calendarDayLayout
                textView.text = day.date.dayOfMonth.toString()

                val completedView = container.calendarDayBinding.completedView
                val playingView = container.calendarDayBinding.playingView
                val abandonedView = container.calendarDayBinding.abandonedView
                completedView.background = null
                playingView.background = null
                abandonedView.background = null

                if (day.owner == DayOwner.THIS_MONTH) {
                    layout.setBackgroundResource(
                        if (selectedDate == day.date) R.drawable.calendar_selected_day_border
                        else 0
                    )

                    calendarGames[day.date.format(dateFormat)]?.forEach { game ->
                        when (game.listName) {
                            ListType.PLAYING -> playingView.setBackgroundColor(
                                view?.context!!.getColor(
                                    R.color.blue
                                )
                            )
                            ListType.COMPLETED -> completedView.setBackgroundColor(
                                view?.context!!.getColor(
                                    R.color.green
                                )
                            )
                            ListType.ABANDONED -> abandonedView.setBackgroundColor(
                                view?.context!!.getColor(
                                    R.color.red
                                )
                            )
                        }
                    }
                } else {
                    textView.setTextColor(resources.getColor(R.color.gray))
                    layout.background = null
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = CalendarHeaderBinding.bind(view).legendLayout.root
        }
        binding.calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
            override fun create(view: View) = MonthViewContainer(view)
            override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                if (container.legendLayout.tag == null) {
                    container.legendLayout.tag = month.yearMonth
                    month.yearMonth
                }
            }
        }

        binding.calendarView.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            binding.monthText.text = title.replaceFirstChar { it.uppercase() }

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                binding.calendarView.notifyDateChanged(it)
                updateAdapterForDate(mAdapter, null)
            }
        }

        binding.previousMonthBtn.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.previous)
            }
        }

        binding.nextMonthBtn.setOnClickListener {
            binding.calendarView.findFirstVisibleMonth()?.let {
                binding.calendarView.smoothScrollToMonth(it.yearMonth.next)
            }
        }
    }

    /**
     * Updates the games in the calendar view
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateAdapterForDate(adapter: CalendarViewHolder, date: LocalDate?) {
        adapter.games.clear()
        adapter.games.addAll(calendarGamesMap[date?.format(dateFormat)].orEmpty())
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}