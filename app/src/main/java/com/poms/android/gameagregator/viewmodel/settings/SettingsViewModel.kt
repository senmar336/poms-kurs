package com.poms.android.gameagregator.viewmodel.settings

import androidx.lifecycle.ViewModel
import com.poms.android.gameagregator.models.repository.FirebaseRepository
import com.poms.android.gameagregator.models.repository.IFirebaseRepository

class SettingsViewModel : ViewModel() {

    // Firebase interface
    private val firebaseInterface: IFirebaseRepository = FirebaseRepository()

    /**
     * Gets the current user email
     */
    fun getUserEmail(): String? {
        return firebaseInterface.getEmail()
    }
}