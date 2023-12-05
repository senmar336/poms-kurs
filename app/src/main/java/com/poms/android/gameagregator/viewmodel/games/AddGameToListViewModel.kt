package com.poms.android.gameagregator.viewmodel.games

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository
import com.poms.android.gameagregator.utils.ListType

class AddGameToListViewModel : ViewModel() {

    var gameId : String = ""

    private lateinit var userLists: List<ListEntity>

    // Livedata objects
    val getAvailableListsSuccessful: MutableLiveData<List<ListEntity>> by lazy { MutableLiveData<List<ListEntity>>() }
    val getAvailableListsError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val addGameToListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val addGameToListException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success adding the game to a list
    private val addGameToListSuccessfulFunction = Observer<Boolean> {
        addGameToListSuccessful.value = true
        clearInterfaceObservers()
    }

    // Error adding the game to a list
    private val addGameToListExceptionFunction = Observer<Exception> {
        addGameToListException.value = it
        clearInterfaceObservers()
    }

    // Success getting the user's lists
    private val getUserListsSuccessfulFunction = Observer<List<ListEntity>> {
        userLists = it
        getListsOfGame()
    }

    // Error getting the user's lists
    private val getUserListsExceptionFunction = Observer<Exception> {
        getAvailableListsError.value = it
        clearInterfaceObservers()
    }

    // Success getting the lists in which the game is included
    private val getListsOfGameSuccessfulFunction = Observer<List<ListEntity>> { gameLists ->
        // We only need to show the lists which the game is not included
        val gameListIds = gameLists.map { it.id }
        getAvailableListsSuccessful.value = userLists.filter { it.id !in gameListIds }
        clearInterfaceObservers()
    }

    // Error getting the lists in which the game is included
    private val getListsOfGameErrorFunction = Observer<Exception> {
        getAvailableListsError.value = it
        clearInterfaceObservers()
    }

    /**
     * Adds a game to a list
     */
    fun addGameToList(list: ListEntity, gameId: String) {
        firebaseInterface.gameAddedToListSuccessful.observeForever(addGameToListSuccessfulFunction)
        firebaseInterface.gameAddedToListError.observeForever(addGameToListExceptionFunction)

        if (list.type == ListType.PERSONAL) {
            firebaseInterface.addGameToPersonalList(list, gameId)
        } else {
            firebaseInterface.addGameToDefaultList(list, gameId, userLists)
        }
    }

    /**
     * Gets the user's lists
     */
    fun getUserLists() {
        firebaseInterface.getListsSuccessful.observeForever(getUserListsSuccessfulFunction)
        firebaseInterface.getListsError.observeForever(getUserListsExceptionFunction)

        firebaseInterface.getUserLists()
    }

    /**
     * Gets the lists in which the game is included
     */
    private fun getListsOfGame() {
        firebaseInterface.getListsOfGameSuccessful.observeForever(getListsOfGameSuccessfulFunction)
        firebaseInterface.getListsOfGameError.observeForever(getListsOfGameErrorFunction)

        firebaseInterface.getListsOfGame(gameId.toInt(), userLists)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.gameAddedToListSuccessful.removeObserver { }
        firebaseInterface.gameAddedToListError.removeObserver { }
        firebaseInterface.getListsSuccessful.removeObserver { }
        firebaseInterface.getListsError.removeObserver { }
        firebaseInterface.getListsOfGameSuccessful.removeObserver { }
        firebaseInterface.getListsOfGameError.removeObserver { }
    }
}