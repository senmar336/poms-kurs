package com.poms.android.gameagregator.models.entity

class GameDetailEntity {

    /**
     * GameDetailEntity id
     */
    var id: String = ""

    /**
     * GameDetailEntity name
     */
    var name: String = ""

    /**
     * GameDetailEntity rating
     */
    var rating: Int = 0

    /**
     * GameDetailEntity genres
     */
    var genres: String = ""

    /**
     * GameDetailEntity platforms
     */
    var platforms: String = ""

    /**
     * GameDetailEntity storyline
     */
    var storyline: String = ""

    /**
     * GameDetailEntity summary
     */
    var summary: String = ""

    /**
     * GameDetailEntity firstReleaseDate
     */
    lateinit var firstReleaseDate: String

    /**
     * GameDetailEntity imageId
     */
    var imageId: String = ""

    /**
     * GameDetailEntity artworksIds
     */
    var artworksIds: List<String> = emptyList()

    /**
     * Full constructor
     */
    constructor(
        id: String,
        name: String,
        rating: Int,
        genres: String,
        platforms: String,
        storyline: String,
        summary: String,
        first_release_date: String,
        imageId: String,
        artworksIds: List<String>
    ) {
        this.id = id
        this.name = name
        this.rating = rating
        this.genres = genres
        this.platforms = platforms
        this.storyline = storyline
        this.summary = summary
        this.firstReleaseDate = first_release_date
        this.imageId = imageId
        this.artworksIds = artworksIds
    }
}