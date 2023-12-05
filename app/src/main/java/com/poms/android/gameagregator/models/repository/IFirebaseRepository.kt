package com.poms.android.gameagregator.models.repository

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.models.entity.ListEntity

interface IFirebaseRepository {

    // MutableLiveData variables for authentication
    val createUserSuccessful: MutableLiveData<Boolean>
    val createUserError: MutableLiveData<Exception>
    val loginSuccessful: MutableLiveData<Boolean>
    val loginError: MutableLiveData<Exception>
    val loginFirebaseError: MutableLiveData<Exception>
    val restorePasswordSuccessful: MutableLiveData<Boolean>
    val restorePasswordError: MutableLiveData<Exception>
    val userDoesNotExistError: MutableLiveData<Boolean>
    val userAlreadyHasListError: MutableLiveData<Boolean>

    // MutableLiveData variables for lists
    val createListSuccessful: MutableLiveData<Boolean>
    val createListError: MutableLiveData<Exception>
    val getListsSuccessful: MutableLiveData<List<ListEntity>>
    val getListsError: MutableLiveData<Exception>
    val shareListSuccessful: MutableLiveData<Boolean>
    val shareListError: MutableLiveData<Exception>
    val deleteListSuccessful: MutableLiveData<Boolean>
    val deleteListError: MutableLiveData<Exception>
    val getListsOfGameSuccessful: MutableLiveData<List<ListEntity>>
    val getListsOfGameError: MutableLiveData<Exception>
    val gameAddedToListSuccessful: MutableLiveData<Boolean>
    val gameAddedToListError: MutableLiveData<Exception>
    val getGamesFromListSuccessful: MutableLiveData<List<String>>
    val getGamesFromListError: MutableLiveData<Exception>
    val getGamesFromDefaultListSuccessful: MutableLiveData<List<CalendarGameEntity>>
    val getGamesFromDefaultListError: MutableLiveData<Exception>
    val gameRemovedFromListSuccessful: MutableLiveData<Boolean>
    val gameRemovedFromListError: MutableLiveData<Exception>

    // MutableLiveData variables for rates
    val rateGameSuccessful: MutableLiveData<Boolean>
    val rateGameError: MutableLiveData<Exception>
    val getRateGameSuccessful: MutableLiveData<Int>
    val getRateGameError: MutableLiveData<Exception>
    val deleteRateGameSuccessful: MutableLiveData<Boolean>
    val deleteRateGameError: MutableLiveData<Exception>


    /**
     * Gets the current user email
     */
    fun getEmail(): String?

    /**
     * Creates an user
     */
    fun createUser(email: String, password: String)

    /**
     * Login using email and password
     */
    fun loginWithEmailAndPassword(email: String, password: String)

    /**
     * Login using a Google account
     */
    fun loginWithGoogle(data: Intent?)

    /**
     * Restores the password of the user
     */
    fun restorePassword(email: String)

    /**
     * Creates a list
     */
    fun createList(listName: String, type: String)

    /**
     * Gets all the lists of the current user
     */
    fun getUserLists()

    /**
     * Gets the lists in which the selected game is included
     */
    fun getListsOfGame(gameId: Int, userLists: List<ListEntity>)

    /**
     * Removes the selected list
     */
    fun deleteList(listId: String)

    /**
     * Shares the selected list with another user email
     */
    fun shareList(email: String, listId: String, listName: String, listType: String)

    /**
     * Adds a game to a personal list
     */
    fun addGameToPersonalList(list: ListEntity, gameId: String)

    /**
     * Checks if the game is inside another default list, if so removes it from the list
     * then add the game to the selected default list
     */
    fun addGameToDefaultList(list: ListEntity, gameId: String, userLists: List<ListEntity>)

    /**
     * Gets the game from the selected list
     */
    fun getGamesFromList(listId: String)

    /**
     * Gets the game from the selected default list
     */
    fun getGamesFromDefaultList()

    /**
     * Removes the selected game from the selected list
     */
    fun deleteGameFromList(listId: String, gameId: String)

    /**
     * Rates a game
     */
    fun rateGame(gameId: String, rate: Int)

    /**
     * Gets the rate of the selected game
     */
    fun getRate(gameId: String)

    /**
     * Removes the rate of the selected game
     */
    fun deleteRate(gameId: String)
}