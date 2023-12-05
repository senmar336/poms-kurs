package com.poms.android.gameagregator.viewmodel.games

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.GameDetailEntity
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository
import com.poms.android.gameagregator.models.repository.IIgdbRepository
import com.poms.android.gameagregator.models.repository.IgdbRepository

class GameDetailViewModel : ViewModel() {

    // IGDB repository instance
    private val igdbRepository: IIgdbRepository = IgdbRepository()
    // Firebase repository instance
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    var gameId : String = ""
    private var userLists: List<ListEntity> = emptyList()


    // Livedata objects
    val gameDetails: MutableLiveData<GameDetailEntity> by lazy { MutableLiveData<GameDetailEntity>() }
    val getGameDetailError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val listsOfGame: MutableLiveData<List<ListEntity>> by lazy { MutableLiveData<List<ListEntity>>() }
    private val getListsOfGameError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val getListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val getUserRateSuccessful: MutableLiveData<Int?> by lazy { MutableLiveData<Int?>() }
    val getUserRateError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val deleteUserRateSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val deleteUserRateError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Success getting game details
    private val getGameDetailSuccessfulFunction = Observer<GameDetailEntity> {
        gameDetails.value = it
        clearInterfaceObservers()
    }

    // Error getting game details
    private val getGameDetailErrorFunction = Observer<Exception> {
        getGameDetailError.value = it
        clearInterfaceObservers()
    }

    // Success getting the lists in which the game is included
    private val getListsOfGameSuccessfulFunction = Observer<List<ListEntity>> {
        listsOfGame.value = it
        clearInterfaceObservers()
    }

    // Error getting the lists in which the game is included
    private val getListsOfGameErrorFunction = Observer<Exception> {
        getListsOfGameError.value = it
        clearInterfaceObservers()
    }

    // Success getting the list
    private val getListSuccessfulFunction = Observer<List<ListEntity>> {
        userLists = it
        getListsOfGame()
        clearInterfaceObservers()
    }

    // Error getting the list
    private val getListErrorFunction = Observer<Exception> {
        getListError.value = it
        clearInterfaceObservers()
    }

    // Success getting user rate
    private val getUserRateSuccessfulFunction = Observer<Int?> {
        getUserRateSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error getting user rate
    private val getUserRateErrorFunction = Observer<Exception> {
        getUserRateError.value = it
        clearInterfaceObservers()
    }

    // Success deleting user rate
    private val deleteUserRateSuccessfulFunction = Observer<Boolean> {
        getUserRateSuccessful.value = null
        deleteUserRateSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error deleting user rate
    private val deleteUserRateErrorFunction = Observer<Exception> {
        deleteUserRateError.value = it
        clearInterfaceObservers()
    }

    /**
     * Gets game details
     */
    fun getGameDetail() {
        igdbRepository.getGameDetailSuccessful.observeForever(getGameDetailSuccessfulFunction)
        igdbRepository.getGameDetailError.observeForever(getGameDetailErrorFunction)

        igdbRepository.getGameDetail(gameId)
    }

    /**
     * Gets the lists in which the game is included
     */
    fun getListsOfGame() {
        firebaseInterface.getListsOfGameSuccessful.observeForever(getListsOfGameSuccessfulFunction)
        firebaseInterface.getListsOfGameError.observeForever(getListsOfGameErrorFunction)

        firebaseInterface.getListsOfGame(gameId.toInt(), userLists)
    }

    /**
     * Gets the user lists
     */
    fun getUserLists() {
        firebaseInterface.getListsSuccessful.observeForever(getListSuccessfulFunction)
        firebaseInterface.getListsError.observeForever(getListErrorFunction)

        firebaseInterface.getUserLists()
    }

    /**
     * Gets the user rate of the game
     */
    fun getRate() {
        firebaseInterface.getRateGameSuccessful.observeForever(getUserRateSuccessfulFunction)
        firebaseInterface.getRateGameError.observeForever(getUserRateErrorFunction)

        firebaseInterface.getRate(gameId)
    }

    /**
     * Removes the user rate of the game
     */
    fun deleteRate() {
        firebaseInterface.deleteRateGameSuccessful.observeForever(deleteUserRateSuccessfulFunction)
        firebaseInterface.deleteRateGameError.observeForever(deleteUserRateErrorFunction)

        firebaseInterface.deleteRate(gameId)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        igdbRepository.getGameDetailSuccessful.removeObserver {}
        igdbRepository.getGameDetailError.removeObserver {}
        firebaseInterface.getListsOfGameSuccessful.removeObserver {}
        firebaseInterface.getListsOfGameError.removeObserver {}
        firebaseInterface.getListsSuccessful.removeObserver {}
        firebaseInterface.getListsError.removeObserver {}
        firebaseInterface.getRateGameSuccessful.removeObserver {}
        firebaseInterface.getRateGameError.removeObserver {}
        firebaseInterface.deleteRateGameSuccessful.removeObserver {}
        firebaseInterface.deleteRateGameError.removeObserver {}
    }
}