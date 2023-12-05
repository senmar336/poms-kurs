package com.poms.android.gameagregator.viewmodel.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class RegisterViewModel : ViewModel() {

    // Livedata objects
    val registerSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val registerException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success with the register
    private val registerSuccessfulFunction = Observer<Boolean> {
        registerSuccessful.value = true
        clearInterfaceObservers()
    }

    // Error with the register
    private val registerExceptionFunction = Observer<Exception> {
        registerException.value = it
        clearInterfaceObservers()
    }

    /**
     * Creates a new user
     */
    fun createUser(email: String, password: String) {
        firebaseInterface.createUserSuccessful.observeForever(registerSuccessfulFunction)
        firebaseInterface.createUserError.observeForever(registerExceptionFunction)

        firebaseInterface.createUser(email, password)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.createUserSuccessful.removeObserver { }
        firebaseInterface.createUserError.removeObserver { }
    }
}