package com.poms.android.gameagregator.models.entity

class GameListEntity {

    /**
     * GameListEntity id
     */
    var id: String = ""

    /**
     * GameListEntity name
     */
    var name: String = ""

    /**
     * GameListEntity rating
     */
    var rating: Int = 0

    /**
     * GameListEntity genres
     */
    var genres: String = ""

    /**
     * GameListEntity platforms
     */
    var platforms: String = ""

    /**
     * GameListEntity imageId
     */
    var imageId: String = ""

    /**
     * Empty constructor
     */
    constructor()

    /**
     * Full constructor
     */
    constructor(
        id: String,
        name: String,
        rating: Int,
        genres: String,
        platforms: String,
        coverUrl: String
    ) {
        this.id = id
        this.name = name
        this.rating = rating
        this.genres = genres
        this.platforms = platforms
        this.imageId = coverUrl
    }
}