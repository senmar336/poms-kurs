package com.poms.android.gameagregator.models.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.api.igdb.apicalypse.APICalypse
import com.api.igdb.apicalypse.Sort
import com.api.igdb.exceptions.RequestException
import com.api.igdb.request.IGDBWrapper
import com.api.igdb.request.TwitchAuthenticator
import com.api.igdb.request.games
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.models.entity.GameDetailEntity
import com.poms.android.gameagregator.models.entity.GameEntityTransformer
import com.poms.android.gameagregator.models.entity.GameListEntity
import com.poms.android.gameagregator.utils.IgdbConstants
import com.poms.android.gameagregator.utils.OAuthConstants

class IgdbRepository : IIgdbRepository {

    // MutableLiveData variables for IGDB games
    override val getTrendingGamesSuccessful: MutableLiveData<List<GameListEntity>> by lazy { MutableLiveData<List<GameListEntity>>() }
    override val getTrendingGamesError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getSearchResultSuccessful: MutableLiveData<List<GameListEntity>> by lazy { MutableLiveData<List<GameListEntity>>() }
    override val getSearchResultError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getGameDetailSuccessful: MutableLiveData<GameDetailEntity> by lazy { MutableLiveData<GameDetailEntity>() }
    override val getGameDetailError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getGamesNamesSuccessful: MutableLiveData<List<CalendarGameEntity>> by lazy { MutableLiveData<List<CalendarGameEntity>>() }
    override val getGamesNamesError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    /**
     * Gets the data of the selected game
     */
    override fun getGameListData(gameId: String) : GameListEntity {
        val token = TwitchAuthenticator.requestTwitchToken(OAuthConstants.clientID, OAuthConstants.accessToken)?.access_token ?: ""
        IGDBWrapper.setCredentials(OAuthConstants.clientID, token)
        val apicalypse = APICalypse()
            .fields(IgdbConstants.GAME_LIST_ENTITY_FIELDS)
            .where("id=".plus(gameId))

        try {
            val igdbGames = IGDBWrapper.games(apicalypse)
            return GameEntityTransformer.convertFromGameToGameListEntity(igdbGames[0])

        } catch(e: RequestException) {
            Log.d("IGDB", e.message.toString())
            throw e
        }
    }

    /**
     * Gets the trending games
     */
    override fun getTrendingGames() {
        val token = TwitchAuthenticator.requestTwitchToken(OAuthConstants.clientID, OAuthConstants.accessToken)?.access_token ?: ""
        IGDBWrapper.setCredentials(OAuthConstants.clientID, token)
        // Trending games are games with rating>50 and released in last 2 weeks
        val apicalypse = APICalypse()
            .fields(IgdbConstants.GAME_LIST_ENTITY_FIELDS)
            .where("total_rating>=50 & first_release_date>=1650391492")
            .limit(10)
            .sort("hypes", Sort.DESCENDING)

        try {
            val igdbGames = IGDBWrapper.games(apicalypse)
            getTrendingGamesSuccessful.value = igdbGames.sortedByDescending { it.totalRating }.map { game ->
                GameEntityTransformer.convertFromGameToGameListEntity(game)
            }

        } catch(e: RequestException) {
            Log.d("IGDB", e.message.toString())
            getTrendingGamesError.value = e
        }
    }

    /**
     * Gets the details of the selected game
     */
    override fun getGameDetail(gameId: String) {
        val token = TwitchAuthenticator.requestTwitchToken(OAuthConstants.clientID, OAuthConstants.accessToken)?.access_token ?: ""
        IGDBWrapper.setCredentials(OAuthConstants.clientID, token)
        val apicalypse = APICalypse()
            .fields(IgdbConstants.GAME_DETAIL_ENTITY_FIELDS)
            .where("id=".plus(gameId))

        try {
            val igdbGames = IGDBWrapper.games(apicalypse)
            getGameDetailSuccessful.value = GameEntityTransformer.convertFromGameToGameDetailEntity(igdbGames[0])

        } catch(e: RequestException) {
            Log.d("IGDB", e.message.toString())
            getGameDetailError.value = e
        }
    }

    /**
     * Searches for games by the selected game name
     */
    override fun searchGamesByName(gameName: String) {
        val token = TwitchAuthenticator.requestTwitchToken(OAuthConstants.clientID, OAuthConstants.accessToken)?.access_token ?: ""
        IGDBWrapper.setCredentials(OAuthConstants.clientID, token)
        val apicalypse = APICalypse()
            .fields(IgdbConstants.GAME_LIST_ENTITY_FIELDS)
            .search(gameName)
            .limit(20)

        try {
            val igdbGames = IGDBWrapper.games(apicalypse)
            getSearchResultSuccessful.value = igdbGames.sortedByDescending { it.totalRating }.map { game ->
                GameEntityTransformer.convertFromGameToGameListEntity(game)
            }

        } catch(e: RequestException) {
            Log.d("IGDB", e.message.toString())
            getSearchResultError.value = e
        }
    }

    /**
     * Gets the name of the games and updates the list
     */
    override fun getGamesNames(games: List<CalendarGameEntity>) {
        val token = TwitchAuthenticator.requestTwitchToken(OAuthConstants.clientID, OAuthConstants.accessToken)?.access_token ?: ""
        IGDBWrapper.setCredentials(OAuthConstants.clientID, token)
        games.forEach { game ->
            val apicalypse = APICalypse()
                .fields("name")
                .where("id=".plus(game.gameId))

            try {
                val igdbGames = IGDBWrapper.games(apicalypse)
                game.gameName = igdbGames[0].name

            } catch(e: RequestException) {
                Log.d("IGDB", e.message.toString())
                getGamesNamesError.value = e
            }
        }
        getGamesNamesSuccessful.value = games
    }
}