package com.poms.android.gameagregator.models.entity

import com.google.firebase.database.Exclude

class ListEntity {

    /**
     * ListEntity id
     */
    @get:Exclude
    var id: String = ""

    /**
     * ListEntity name
     */
    var name: String = ""

    /**
     * ListEntity type
     */
    var type: String = ""

    /**
     * Empty constructor
     */
    constructor()

    /**
     * Full constructor
     */
    constructor(id: String, name: String, type: String) {
        this.id = id
        this.name = name
        this.type = type
    }
}