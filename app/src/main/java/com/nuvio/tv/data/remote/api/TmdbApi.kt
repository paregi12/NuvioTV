package com.nuvio.tv.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    
    @GET("find/{external_id}")
    suspend fun findByExternalId(
        @Path("external_id") externalId: String,
        @Query("api_key") apiKey: String,
        @Query("external_source") externalSource: String = "imdb_id"
    ): Response<TmdbFindResponse>
    
    @GET("movie/{movie_id}/external_ids")
    suspend fun getMovieExternalIds(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Response<TmdbExternalIdsResponse>
    
    @GET("tv/{tv_id}/external_ids")
    suspend fun getTvExternalIds(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Response<TmdbExternalIdsResponse>
}

@JsonClass(generateAdapter = true)
data class TmdbFindResponse(
    @Json(name = "movie_results") val movieResults: List<TmdbFindResult>? = null,
    @Json(name = "tv_results") val tvResults: List<TmdbFindResult>? = null,
    @Json(name = "tv_episode_results") val tvEpisodeResults: List<TmdbFindResult>? = null,
    @Json(name = "tv_season_results") val tvSeasonResults: List<TmdbFindResult>? = null
)

@JsonClass(generateAdapter = true)
data class TmdbFindResult(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "media_type") val mediaType: String? = null
)

@JsonClass(generateAdapter = true)
data class TmdbExternalIdsResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "tvdb_id") val tvdbId: Int? = null
)
