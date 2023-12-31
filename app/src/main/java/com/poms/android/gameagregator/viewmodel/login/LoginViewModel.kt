package com.poms.android.gameagregator.viewmodel.login

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class LoginViewModel : ViewModel() {

    // Livedata objects
    val loginSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val loginException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Login successful
    private val loginSuccessfulFunction = Observer<Boolean> {
        loginSuccessful.value = it
        clearInterfaceObservers()
    }

    // Login error
    private val loginExceptionFunction = Observer<Exception> {
        loginException.value = it
    }

    /**
     * Login with the input data
     */
    fun login(email: String, password: String) {
        firebaseInterface.loginSuccessful.observeForever(loginSuccessfulFunction)
        firebaseInterface.loginError.observeForever(loginExceptionFunction)

        firebaseInterface.loginWithEmailAndPassword(email, password)
    }

    /**
     * Login with Google
     */
    fun loginWithGoogle(data: Intent?) {
        firebaseInterface.loginSuccessful.observeForever(loginSuccessfulFunction)
        firebaseInterface.loginError.observeForever(loginExceptionFunction)

        firebaseInterface.loginWithGoogle(data)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.loginSuccessful.removeObserver { }
        firebaseInterface.loginError.removeObserver { }
    }
}