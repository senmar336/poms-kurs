package com.poms.android.gameagregator.viewmodel.lists

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository
import com.poms.android.gameagregator.ui.lists.CreateListFragment
import com.poms.android.gameagregator.utils.ListType

class CreateListViewModel : ViewModel() {

    // Interface of create list to prevent the lost with screen rotation
    var createListInterface: CreateListFragment.CreateListInterface? = null

    // Livedata objects
    val createListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val createListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    // Success creating list
    private val createListSuccessfulFunction = Observer<Boolean> {
        createListSuccessful.value = it
        clearInterfaceObservers()
    }

    // Error creating list
    private val createListErrorFunction = Observer<Exception> {
        createListError.value = it
        clearInterfaceObservers()
    }

    /**
     * Creates a new personal list
     */
    fun createList(listName: String) {
        firebaseInterface.createListSuccessful.observeForever(createListSuccessfulFunction)
        firebaseInterface.createListError.observeForever(createListErrorFunction)

        firebaseInterface.createList(listName, ListType.PERSONAL)
    }

    /**
     * Clear all observers
     */
    private fun clearInterfaceObservers() {
        firebaseInterface.createListSuccessful.removeObserver { }
        firebaseInterface.createListError.removeObserver { }
    }
}