package com.poms.android.gameagregator.viewmodel.lists

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class ListsViewModel : ViewModel() {

    // Livedata objects
    val getListSuccessful: MutableLiveData<List<ListEntity>> by lazy { MutableLiveData<List<ListEntity>>() }
    val getListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    val deleteListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val deleteListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success getting list
    private val getListSuccessfulFunction = Observer<List<ListEntity>> {
        getListSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error getting list
    private val getListErrorFunction = Observer<Exception> {
        getListError.value = it
        clearInterfaceObservers()
    }

    // Success deleting list
    private val deleteListSuccessfulFunction = Observer<Boolean> {
        deleteListSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error deleting lists
    private val deleteListErrorFunction = Observer<Exception> {
        deleteListError.value = it
        clearInterfaceObservers()
    }

    // Get the list of the users
    fun getUserLists() {
        firebaseInterface.getListsSuccessful.observeForever(getListSuccessfulFunction)
        firebaseInterface.getListsError.observeForever(getListErrorFunction)

        firebaseInterface.getUserLists()
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
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.getListsError.removeObserver { }
        firebaseInterface.getListsSuccessful.removeObserver { }
        firebaseInterface.deleteListSuccessful.removeObserver { }
        firebaseInterface.deleteListError.removeObserver { }
    }
}