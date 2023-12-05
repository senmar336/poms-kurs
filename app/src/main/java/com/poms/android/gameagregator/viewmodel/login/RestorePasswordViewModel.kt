package com.poms.android.gameagregator.viewmodel.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class RestorePasswordViewModel: ViewModel() {

    // Livedata objects
    val restorePasswordSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val restorePasswordException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success restoring password
    private val restorePasswordSuccessfulFunction = Observer<Boolean> {
        restorePasswordSuccessful.value = true
        clearInterfaceObservers()
    }

    // Error restoring password
    private val restorePasswordExceptionFunction = Observer<Exception> {
        restorePasswordException.value = it
        clearInterfaceObservers()
    }

    /**
     * Restores the password
     */
    fun restorePassword(email: String) {
        firebaseInterface.restorePasswordSuccessful.observeForever(restorePasswordSuccessfulFunction)
        firebaseInterface.restorePasswordError.observeForever(restorePasswordExceptionFunction)

        firebaseInterface.restorePassword(email)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.restorePasswordSuccessful.removeObserver { }
        firebaseInterface.restorePasswordError.removeObserver { }
    }
}