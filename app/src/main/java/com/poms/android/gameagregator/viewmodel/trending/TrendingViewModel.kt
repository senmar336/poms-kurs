package com.poms.android.gameagregator.viewmodel.trending

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.models.repository.IIgdbRepository
import com.poms.android.gameagregator.models.repository.IgdbRepository

class TrendingViewModel : ViewModel() {

    // Livedata objects
    val getTrendingGamesSuccessful: MutableLiveData<List<GameListEntity>> by lazy { MutableLiveData<List<GameListEntity>>() }
    val getTrendingGamesError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // IGDB repository instance
    private val igdbRepository: IIgdbRepository = IgdbRepository()

    // Success getting trending games
    private val getTrendingGamestSuccessfulFunction = Observer<List<GameListEntity>> {
        getTrendingGamesSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error getting trending games
    private val getTrendingGamesErrorFunction = Observer<Exception> {
        getTrendingGamesError.value = it
        clearInterfaceObservers()
    }

    /**
     * Gets trending games
     */
    fun getTrendingGames() {
        igdbRepository.getTrendingGamesSuccessful.observeForever(getTrendingGamestSuccessfulFunction)
        igdbRepository.getTrendingGamesError.observeForever(getTrendingGamesErrorFunction)

        igdbRepository.getTrendingGames()
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        igdbRepository.getTrendingGamesSuccessful.removeObserver { }
        igdbRepository.getTrendingGamesError.removeObserver { }
    }
}