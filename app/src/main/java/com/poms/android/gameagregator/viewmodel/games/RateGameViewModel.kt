package com.poms.android.gameagregator.viewmodel.games

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class RateGameViewModel : ViewModel() {

    var gameId : String = ""

    // Livedata objects
    val rateGameSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val rateGameException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success adding user rate
    private val rateGameSuccessfulFunction = Observer<Boolean> {
        rateGameSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error adding user rate
    private val rateGameExceptionFunction = Observer<Exception> {
        rateGameException.value = it
        clearInterfaceObservers()
    }

    /**
     * Adds the user rate to a game
     */
    fun rateGame(rate: Int) {
        firebaseInterface.rateGameSuccessful.observeForever(rateGameSuccessfulFunction)
        firebaseInterface.rateGameError.observeForever(rateGameExceptionFunction)

        firebaseInterface.rateGame(gameId, rate)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.rateGameSuccessful.removeObserver { }
        firebaseInterface.rateGameError.removeObserver { }
    }
}