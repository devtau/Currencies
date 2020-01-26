package com.devtau.currencies.rest

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BackendAPI {

    @GET(COURSES_ENDPOINT)
    fun getCourses(
        @Query("base") base: String
    ): Call<CurrenciesResponse>


    companion object {
        const val COURSES_ENDPOINT = "latest"
    }
}