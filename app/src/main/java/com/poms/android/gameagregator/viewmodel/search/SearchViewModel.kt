package com.poms.android.gameagregator.viewmodel.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.models.repository.IIgdbRepository
import com.poms.android.gameagregator.models.repository.IgdbRepository

class SearchViewModel : ViewModel() {

    // Livedata objects
    val getSearchResultSuccessful: MutableLiveData<List<GameListEntity>> by lazy { MutableLiveData<List<GameListEntity>>() }
    val getSearchResultError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // IGDB repository instance
    private val igdbRepository: IIgdbRepository = IgdbRepository()

    // Success searching games
    private val getSearchResultSuccessfulFunction = Observer<List<GameListEntity>> {
        getSearchResultSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error searching games
    private val getSearchResultErrorFunction = Observer<Exception> {
        getSearchResultError.value = it
        clearInterfaceObservers()
    }

    /**
     * Searches for games by their name
     */
    fun searchGamesByName(gameName: String) {
        igdbRepository.getSearchResultSuccessful.observeForever(getSearchResultSuccessfulFunction)
        igdbRepository.getSearchResultError.observeForever(getSearchResultErrorFunction)

        igdbRepository.searchGamesByName(gameName)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        igdbRepository.getSearchResultError.removeObserver { }
        igdbRepository.getSearchResultSuccessful.removeObserver { }
    }
}