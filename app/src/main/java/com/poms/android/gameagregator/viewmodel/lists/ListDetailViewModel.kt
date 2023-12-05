package com.poms.android.gameagregator.viewmodel.lists

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.api.igdb.exceptions.RequestException
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository
import com.poms.android.gameagregator.models.repository.IIgdbRepository
import com.poms.android.gameagregator.models.repository.IgdbRepository

class ListDetailViewModel : ViewModel() {

    // List Id
    var listId = ""

    // List name
    var listName = ""

    // List type
    var listType = ""

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // IGDB repository interface
    private val igdbRepository: IIgdbRepository = IgdbRepository()

    // Livedata objects
    val getGamesError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val getGamesSuccessful: MutableLiveData<List<GameListEntity>> by lazy { MutableLiveData<List<GameListEntity>>() }
    val deleteGamesError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val deleteGamesSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val deleteListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val deleteListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Success getting games from list
    private val getGamesFromListSuccessfulFunction = Observer<List<String>> {
        if (it.isNotEmpty()) {
            getGamesData(it)
        } else {
            getGamesSuccessful.value = listOf()
        }
        clearInterfaceObservers()
    }

    // Error getting games from list
    private val getGamesFromListErrorFunction = Observer<Exception> {
        getGamesError.value = it
        clearInterfaceObservers()
    }

    // Success deleting game
    private val deleteGamesSuccessfulFunction = Observer<Boolean> {
        deleteGamesSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error deleting game
    private val deleteGamesErrorFunction = Observer<Exception> {
        deleteGamesError.value = it
        clearInterfaceObservers()
    }

    // Success deleting list
    private val deleteListSuccessfulFunction = Observer<Boolean> {
        deleteListSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error deleting list
    private val deleteListErrorFunction = Observer<Exception> {
        deleteListError.value = it
        clearInterfaceObservers()
    }

    /**
     * Gets games of the list
     */
    fun getGames() {
        firebaseInterface.getGamesFromListSuccessful.observeForever(getGamesFromListSuccessfulFunction)
        firebaseInterface.getGamesFromListError.observeForever(getGamesFromListErrorFunction)
        firebaseInterface.getGamesFromList(listId)
    }

    /**
     * Delete game from list
     */
    fun deleteGameFromList(gameId: String) {
        firebaseInterface.gameRemovedFromListSuccessful.observeForever(deleteGamesSuccessfulFunction)
        firebaseInterface.gameRemovedFromListError.observeForever(deleteGamesErrorFunction)
        firebaseInterface.deleteGameFromList(listId, gameId)
    }

    /**
     * Removes the list
     */
    fun deleteList(idList: String) {
        firebaseInterface.deleteListSuccessful.observeForever(deleteListSuccessfulFunction)
        firebaseInterface.deleteListError.observeForever(deleteListErrorFunction)

        firebaseInterface.deleteList(idList)
    }

    /**
     * Gets the games data
     */
    private fun getGamesData(games: List<String>) {
        val gameList = arrayListOf<GameListEntity>()

        games.forEach { gameId ->
            try {
                gameList.add(igdbRepository.getGameListData(gameId))
                if (gameList.count() == games.count()) {
                    getGamesSuccessful.value = gameList
                }
            } catch (e: RequestException) {
                getGamesError.value = e
            }
        }
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.getGamesFromListSuccessful.removeObserver { }
        firebaseInterface.getGamesFromListError.removeObserver { }
        firebaseInterface.gameRemovedFromListSuccessful.removeObserver { }
        firebaseInterface.gameRemovedFromListError.removeObserver { }
        firebaseInterface.deleteListSuccessful.removeObserver { }
        firebaseInterface.deleteListError.removeObserver { }
    }
}