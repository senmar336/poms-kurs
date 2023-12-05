package com.poms.android.gameagregator.viewmodel.lists

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class ShareListViewModel : ViewModel() {

    // ID of the list to share
    var listId = ""

    // Name of the list to share
    var listName = ""

    // Type of the list to share
    var listType = ""

    // Livedata objects
    val shareListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val userDoesNotExistError: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val shareListException: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val userAlreadyHasListError: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success sharing list
    private val shareListSuccessfulFunction = Observer<Boolean> {
        shareListSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error: User does not exist
    private val userDoesNotExistErrorFunction = Observer<Exception> {
        shareListException.value = it
        clearInterfaceObservers()
    }

    // Error sharing list
    private val shareListExceptionFunction = Observer<Boolean> {
        userDoesNotExistError.value = it
        clearInterfaceObservers()
    }

    // Error: The user already has the list
    private val userAlreadyHasListErrorFunction = Observer<Boolean> {
        userAlreadyHasListError.value = it
        clearInterfaceObservers()
    }

    /**
     * Shares the list
     */
    fun shareList(email: String) {
        firebaseInterface.shareListSuccessful.observeForever(shareListSuccessfulFunction)
        firebaseInterface.shareListError.observeForever(userDoesNotExistErrorFunction)
        firebaseInterface.userDoesNotExistError.observeForever(shareListExceptionFunction)
        firebaseInterface.userAlreadyHasListError.observeForever(userAlreadyHasListErrorFunction)

        firebaseInterface.shareList(email, listId, listName, listType)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.shareListSuccessful.removeObserver { }
        firebaseInterface.shareListError.removeObserver { }
        firebaseInterface.userDoesNotExistError.removeObserver { }
        firebaseInterface.userAlreadyHasListError.removeObserver { }
    }
}