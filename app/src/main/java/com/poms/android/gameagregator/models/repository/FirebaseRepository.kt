package com.poms.android.gameagregator.models.repository

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.poms.android.gameagregator.models.entity.CalendarGameEntity
import com.poms.android.gameagregator.models.entity.ListEntity
import com.poms.android.gameagregator.models.entity.UserEntity
import com.poms.android.gameagregator.utils.DefaultListIds
import com.poms.android.gameagregator.utils.ListType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FirebaseRepository : IFirebaseRepository {

    // Instance of the firebase database
    private val database = FirebaseDatabase.getInstance().getReference("")

    // Instance of Firebase Authentication
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // MutableLiveData variables for authentication
    override val createUserSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val createUserError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val loginSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val loginError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val loginFirebaseError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val restorePasswordSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val restorePasswordError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val userDoesNotExistError: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val userAlreadyHasListError: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    // MutableLiveData variables for lists
    override val createListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val createListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getListsSuccessful: MutableLiveData<List<ListEntity>> by lazy { MutableLiveData<List<ListEntity>>() }
    override val getListsError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val shareListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val shareListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val deleteListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val deleteListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getListsOfGameSuccessful: MutableLiveData<List<ListEntity>> by lazy { MutableLiveData<List<ListEntity>>() }
    override val getListsOfGameError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val gameAddedToListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val gameAddedToListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getGamesFromListSuccessful: MutableLiveData<List<String>> by lazy { MutableLiveData<List<String>>() }
    override val getGamesFromListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getGamesFromDefaultListSuccessful: MutableLiveData<List<CalendarGameEntity>> by lazy { MutableLiveData<List<CalendarGameEntity>>() }
    override val getGamesFromDefaultListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val gameRemovedFromListSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val gameRemovedFromListError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }

    // MutableLiveData variables for rates
    override val rateGameSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val rateGameError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val getRateGameSuccessful: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    override val getRateGameError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }
    override val deleteRateGameSuccessful: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    override val deleteRateGameError: MutableLiveData<Exception> by lazy { MutableLiveData<Exception>() }


    /**
     * Gets the current user email
     */
    override fun getEmail(): String? {
        return mAuth.currentUser!!.email
    }

    /**
     * Creates an user
     */
    override fun createUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = UserEntity()
                    user.email = email

                    val userRef = database.child("user")
                    userRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .setValue(user)
                        .addOnSuccessListener {
                            createUserSuccessful.value = true
                            createDefaultLists()
                        }.addOnFailureListener {
                            createUserError.value = it
                        }
                } else {
                    createUserError.value = task.exception
                }
            }
    }

    /**
     * Login using email and password
     */
    override fun loginWithEmailAndPassword(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loginSuccessful.value = true
            }.addOnFailureListener {
                loginError.value = it
            }
    }

    /**
     * Login using a Google account
     */
    override fun loginWithGoogle(data: Intent?) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val idToken: String

        try {
            idToken = task.getResult(ApiException::class.java)!!.idToken!!
        } catch (e: ApiException) {
            loginFirebaseError.value = e
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener { taskSing ->
            if (taskSing.isSuccessful) {
                // Check if the user is registered in Firebase
                database.child("user").child(taskSing.result!!.user!!.uid)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            createUserError.value = error.toException()
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value == null) {
                                val user = UserEntity()
                                user.email = FirebaseAuth.getInstance().currentUser!!.email!!

                                val userRefToAdd = database.child("user")
                                userRefToAdd.child(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .setValue(user)
                                    .addOnSuccessListener {
                                        createUserSuccessful.value = true
                                        createDefaultLists()
                                        loginSuccessful.value = true
                                    }.addOnFailureListener {
                                        createUserError.value = it
                                    }
                            } else {
                                createUserSuccessful.value = true
                                loginSuccessful.value = true
                            }
                        }
                    })
            } else {
                loginFirebaseError.value = taskSing.exception
            }
        }
    }

    /**
     * Restores the password of the user
     */
    override fun restorePassword(email: String) {
        val language: String =
            if (Locale.getDefault().language.uppercase(Locale.ROOT) == "ES") {
                Locale.getDefault().toLanguageTag()
            } else {
                "en-US"
            }

        mAuth.setLanguageCode(language)
        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    restorePasswordSuccessful.value = true
                } else {
                    restorePasswordError.value = task.exception
                }
            }
    }

    /**
     * Creates a list
     */
    override fun createList(listName: String, type: String) {
        val list = ListEntity()
        list.name = listName
        list.type = type

        val listRef = database.child("list")
        val newList: DatabaseReference = listRef.push()
        newList.setValue(list)

        database.child("userList").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(newList.key!!)
            .setValue(list).addOnSuccessListener {
                createListSuccessful.value = true
            }.addOnFailureListener {
                createListError.value = it
            }
    }

    /**
     * Creates the default lists. This function is used everytime a new user is created
     */
    private fun createDefaultLists() {
        val listPlaying = ListEntity()
        listPlaying.name = ListType.PLAYING
        listPlaying.type = ListType.DEFAULT
        val listCompleted = ListEntity()
        listCompleted.name = ListType.COMPLETED
        listCompleted.type = ListType.DEFAULT
        val listAbandoned = ListEntity()
        listAbandoned.name = ListType.ABANDONED
        listAbandoned.type = ListType.DEFAULT
        val lists = listOf(listPlaying, listCompleted, listAbandoned)

        lists.forEach { list ->
            val listRef = database.child("list")
            val newList: DatabaseReference = listRef.push()
            newList.setValue(list)

            database.child("userList").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child(newList.key!!)
                .setValue(list).addOnSuccessListener {
                    createListSuccessful.value = true
                }.addOnFailureListener {
                    createListError.value = it
                }
        }
    }

    /**
     * Gets all the lists of the current user
     */
    override fun getUserLists() {
        database.child("userList").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    getListsError.value = error.toException()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val listEntityList: ArrayList<ListEntity> = arrayListOf()

                        (dataSnapshot.value!! as HashMap<*, *>).forEach {
                            var name = ""
                            var type = ""
                            (it.value as HashMap<*, *>).values.forEachIndexed { index, value ->
                                when (index) {
                                    0 -> name = value.toString()
                                    1 -> type = value.toString()
                                }
                            }
                            // Store the id of the default lists of the user
                            if (type == ListType.DEFAULT) {
                                when (name) {
                                    ListType.PLAYING -> DefaultListIds.playingId = it.key.toString()
                                    ListType.COMPLETED -> DefaultListIds.completedId = it.key.toString()
                                    ListType.ABANDONED -> DefaultListIds.abandonedId = it.key.toString()
                                }
                            }
                            listEntityList.add(ListEntity(it.key.toString(), name, type))
                        }

                        getListsSuccessful.value = listEntityList.sortedBy { it.name }
                    } else {
                        getListsSuccessful.value = listOf()
                    }
                }
            })
    }

    /**
     * Gets the lists in which the selected game is included
     */
    override fun getListsOfGame(gameId: Int, userLists: List<ListEntity>) {
        database.child("listGame")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    getListsOfGameError.value = error.toException()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val listEntityList: ArrayList<ListEntity> = arrayListOf()

                        (dataSnapshot.value!! as HashMap<*, *>).forEach { list ->
                            userLists.forEach {
                                if (it.id == list.key) {
                                    (list.value!! as HashMap<*, *>).forEach { game ->
                                        if (game.key == gameId.toString()) {
                                            listEntityList.add(
                                                ListEntity(
                                                    list.key.toString(),
                                                    it.name,
                                                    it.type
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        getListsOfGameSuccessful.value = listEntityList.sortedBy { it.name }
                    } else {
                        getListsOfGameSuccessful.value = listOf()
                    }
                }
            })
    }

    /**
     * Removes the selected list
     */
    override fun deleteList(listId: String) {
        // Delete list from the user
        database.child("userList").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(listId).removeValue()
            .addOnSuccessListener {
                // Check if the list is shared with another user
                database.child("userList")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            deleteListError.value = error.toException()
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value != null) {
                                (dataSnapshot.value!! as HashMap<*, *>).forEach { users ->
                                    (users.value!! as HashMap<*, *>).forEach { listsOfUser ->
                                        if (listsOfUser.key.toString() == listId) {
                                            deleteListSuccessful.value = true
                                            return
                                        }
                                    }
                                }
                                // Delete games in this list
                                database.child("listGame")
                                    .child(listId).removeValue().addOnSuccessListener {
                                        // Delete the list
                                        database.child("list").child(listId).removeValue()
                                            .addOnSuccessListener {
                                                deleteListSuccessful.value = true
                                            }.addOnFailureListener {
                                                deleteListError.value = it
                                            }
                                    }.addOnFailureListener {
                                        deleteListError.value = it
                                    }

                            } else {
                                // If the list is not shared with other users
                                database.child("listGame")
                                    .child(listId).removeValue().addOnSuccessListener {
                                        // Delete the list
                                        database.child("list").child(listId).removeValue()
                                            .addOnSuccessListener {
                                                deleteListSuccessful.value = true
                                            }.addOnFailureListener {
                                                deleteListError.value = it
                                            }
                                    }.addOnFailureListener {
                                        deleteListError.value = it
                                    }
                            }
                        }
                    })

            }.addOnFailureListener {
                deleteListError.value = it
            }
    }

    /**
     * Shares the selected list with another user email
     */
    override fun shareList(email: String, listId: String, listName: String, listType: String) {
        val listEntity = ListEntity(listId, listName, listType)
        database.child("user").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        (snapshot.value as HashMap<*, *>).forEach {
                            database.child("userList").child(it.key!!.toString())
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(error: DatabaseError) {
                                        shareListError.value = error.toException()
                                    }

                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if (dataSnapshot.value == null) {
                                            database.child("userList").child(it.key!!.toString())
                                                .child(listId)
                                                .setValue(listEntity).addOnSuccessListener {
                                                    shareListSuccessful.value = true
                                                }.addOnFailureListener { exception ->
                                                    shareListError.value = exception
                                                }
                                        } else {
                                            (dataSnapshot.value!! as HashMap<*, *>).forEach { lists ->
                                                if (lists.key == listId) {
                                                    userAlreadyHasListError.value = true
                                                    return
                                                }
                                            }

                                            database.child("userList").child(it.key!!.toString())
                                                .child(listId)
                                                .setValue(listEntity).addOnSuccessListener {
                                                    shareListSuccessful.value = true
                                                }.addOnFailureListener { exception ->
                                                    shareListError.value = exception
                                                }
                                        }
                                    }
                                })
                        }
                    } else {
                        userDoesNotExistError.value = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    shareListError.value = error.toException()
                }
            })
    }

    /**
     * Adds a game to a personal list
     */
    override fun addGameToPersonalList(list: ListEntity, gameId: String) {
        database.child("listGame")
            .child(list.id).child(gameId).setValue(true)
            .addOnSuccessListener {
                gameAddedToListSuccessful.value = true
            }.addOnFailureListener {
                gameAddedToListError.value = it
            }
    }

    /**
     * Checks if the game is inside another default list, if so removes it from the list
     * then add the game to the selected default list with the current day
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun addGameToDefaultList(list: ListEntity, gameId: String, userLists: List<ListEntity>) {
        val current = LocalDateTime.now()
        val currentDay = current.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))

        getListsOfGame(gameId.toInt(), userLists)
        getListsOfGameSuccessful.observeForever { gameLists ->
            val defaultListEntity = gameLists.filter { it.type == ListType.DEFAULT }
            defaultListEntity.forEach { listEntity ->
                deleteGameFromList(listEntity.id, gameId)
            }
            database.child("listGame")
                .child(list.id).child(gameId).setValue(currentDay)
                .addOnSuccessListener {
                    gameAddedToListSuccessful.value = true
                }.addOnFailureListener {
                    gameAddedToListError.value = it
                }
        }
    }

    /**
     * Gets the game from the selected list
     */
    override fun getGamesFromList(listId: String) {
        database.child("listGame").child(listId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    getGamesFromListError.value = error.toException()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val idsList = arrayListOf<String>()

                        (dataSnapshot.value!! as HashMap<*, *>).forEach {
                            idsList.add(it.key.toString())
                        }
                        getGamesFromListSuccessful.value = idsList
                    } else {
                        getGamesFromListSuccessful.value = listOf()
                    }
                }
            })
    }

    /**
     * Gets the game from the selected default list
     */
    override fun getGamesFromDefaultList() {
        database.child("listGame")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    getGamesFromDefaultListError.value = error.toException()
                }

                // Get the game and date
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        val calendarGameEntityList: ArrayList<CalendarGameEntity> = arrayListOf()

                        (dataSnapshot.value!! as HashMap<*, *>).forEach { list ->
                            when (list.key.toString()) {
                                DefaultListIds.playingId -> {
                                    (list.value!! as HashMap<*, *>).forEach { pair ->
                                        calendarGameEntityList.add(
                                            CalendarGameEntity(pair.key.toString(),
                                                null, ListType.PLAYING, pair.value.toString())
                                        )
                                    }
                                }
                                DefaultListIds.completedId -> {
                                    (list.value!! as HashMap<*, *>).forEach { pair ->
                                        calendarGameEntityList.add(
                                            CalendarGameEntity(pair.key.toString(),
                                                null, ListType.COMPLETED, pair.value.toString())
                                        )
                                    }
                                }
                                DefaultListIds.abandonedId -> {
                                    (list.value!! as HashMap<*, *>).forEach { pair ->
                                        calendarGameEntityList.add(
                                            CalendarGameEntity(pair.key.toString(),
                                                null, ListType.ABANDONED, pair.value.toString())
                                        )
                                    }
                                }
                            }
                        }
                        getGamesFromDefaultListSuccessful.value = calendarGameEntityList
                    } else {
                        getGamesFromDefaultListSuccessful.value = emptyList()
                    }
                }
            })
    }

    /**
     * Removes the selected game from the selected list
     */
    override fun deleteGameFromList(listId: String, gameId: String) {
        database.child("listGame")
            .child(listId).child(gameId).removeValue().addOnSuccessListener {
                gameRemovedFromListSuccessful.value = true
            }.addOnFailureListener {
                gameRemovedFromListError.value = it
            }
    }

    /**
     * Rates a game
     */
    override fun rateGame(gameId: String, rate: Int) {
        database.child("ratedGame").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(gameId).setValue(rate).addOnSuccessListener {
                rateGameSuccessful.value = true
            }.addOnFailureListener {
                rateGameError.value = it
            }
    }

    /**
     * Gets the rate of the selected game
     */
    override fun getRate(gameId: String) {
        database.child("ratedGame").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    getRateGameError.value = error.toException()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.value != null) {
                        (dataSnapshot.value!! as HashMap<*, *>).forEach {
                            if (it.key == gameId) {
                                getRateGameSuccessful.value = it.value.toString().toInt()
                                return
                            }
                        }
                        getRateGameSuccessful.value = null
                    } else {
                        getRateGameSuccessful.value = null
                    }
                }
            })
    }

    /**
     * Removes the rate of the selected game
     */
    override fun deleteRate(gameId: String) {
        database.child("ratedGame").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(gameId).removeValue().addOnSuccessListener {
                deleteRateGameSuccessful.value = true
            }.addOnFailureListener {
                deleteRateGameError.value = it
            }
    }
}