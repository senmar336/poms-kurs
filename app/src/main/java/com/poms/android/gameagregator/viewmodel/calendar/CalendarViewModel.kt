package com.poms.android.gameagregator.viewmodel.calendar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository
import com.poms.android.gameagregator.models.repository.IIgdbRepository
import com.poms.android.gameagregator.models.repository.IgdbRepository

class CalendarViewModel : ViewModel() {

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()
    // IGDB repository instance
    private val igdbRepository: IIgdbRepository = IgdbRepository()

    // Livedata objects
    val getGamesFromDefaultListSuccessful: MutableLiveData<Map<String, List<CalendarGameEntity>>> by lazy { MutableLiveData<Map<String, List<CalendarGameEntity>>>() }
    val getGamesFromDefaultListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Success getting games without game name
    private val getGamesFromDefaultListSuccessfulFunction = Observer<List<CalendarGameEntity>> {
        getGamesNames(it)
        clearInterfaceObservers()
    }

    // Success getting games with names
    private val getGamesNamesSuccessfulFunction = Observer<List<CalendarGameEntity>> { list ->
        val aux = list.groupBy { it.dateGameAddedToList }
        getGamesFromDefaultListSuccessful.value = aux
        clearInterfaceObservers()
    }

    // Error getting games
    private val getGamesFromDefaultListErrorFunction = Observer<Exception> {
        getGamesFromDefaultListError.value = it
        clearInterfaceObservers()
    }

    // Get the list of games for calendar
    fun getCalendarGames() {
        firebaseInterface.getGamesFromDefaultListSuccessful.observeForever(getGamesFromDefaultListSuccessfulFunction)
        firebaseInterface.getGamesFromDefaultListError.observeForever(getGamesFromDefaultListErrorFunction)

        firebaseInterface.getGamesFromDefaultList()
    }

    // Get the names of the games
    private fun getGamesNames(games: List<CalendarGameEntity>) {
        igdbRepository.getGamesNamesSuccessful.observeForever(getGamesNamesSuccessfulFunction)
        igdbRepository.getGamesNamesError.observeForever(getGamesFromDefaultListErrorFunction)

        igdbRepository.getGamesNames(games)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.getGamesFromDefaultListSuccessful.removeObserver { }
        firebaseInterface.getGamesFromDefaultListError.removeObserver { }
        igdbRepository.getGamesNamesSuccessful.removeObserver { }
        igdbRepository.getGamesNamesError.removeObserver { }
    }
}