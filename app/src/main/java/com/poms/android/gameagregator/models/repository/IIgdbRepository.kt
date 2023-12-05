package com.poms.android.gameagregator.models.repository

import androidx.lifecycle.MutableLiveData
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.models.entity.GameDetailEntity
import com.poms.android.gameagregator.models.entity.GameListEntity

interface IIgdbRepository {

    // MutableLiveData variables for IGDB games
    val getTrendingGamesSuccessful: MutableLiveData<List<GameListEntity>>
    val getTrendingGamesError: MutableLiveData<Exception>
    val getSearchResultSuccessful: MutableLiveData<List<GameListEntity>>
    val getSearchResultError: MutableLiveData<Exception>
    val getGameDetailSuccessful: MutableLiveData<GameDetailEntity>
    val getGameDetailError: MutableLiveData<Exception>
    val getGamesNamesSuccessful: MutableLiveData<List<CalendarGameEntity>>
    val getGamesNamesError: MutableLiveData<Exception>


    /**
     * Gets the data of the selected game
     */
    fun getGameListData(gameId: String) : GameListEntity

    /**
     * Gets the trending games
     */
    fun getTrendingGames()

    /**
     * Gets the details of the selected game
     */
    fun getGameDetail(gameId: String)

    /**
     * Searches for games by the selected game name
     */
    fun searchGamesByName(gameName: String)

    /**
     * Gets the name of the games and updates the list
     */
    fun getGamesNames(games: List<CalendarGameEntity>)
}