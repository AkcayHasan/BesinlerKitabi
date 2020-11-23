package com.hasanakcay.besinlerkitabiapp.service

import com.hasanakcay.besinlerkitabiapp.model.Besin
import io.reactivex.Single
import retrofit2.http.GET

interface BesinAPI {

    // GET , POST

    //https://github.com/atilsamancioglu/BTK20-JSONVeriSeti/blob/master/besinler.json
    //https://raw.githubusercontent.com/atilsamancioglu/BTK20-JSONVeriSeti/master/besinler.json
    // BASE_URL


    @GET("atilsamancioglu/BTK20-JSONVeriSeti/master/besinler.json")
    fun getBesin() : Single<List<Besin>>

}