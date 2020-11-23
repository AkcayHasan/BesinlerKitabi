package com.hasanakcay.besinlerkitabiapp.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hasanakcay.besinlerkitabiapp.model.Besin
import com.hasanakcay.besinlerkitabiapp.service.BesinAPIService
import com.hasanakcay.besinlerkitabiapp.service.BesinDatabase
import com.hasanakcay.besinlerkitabiapp.util.OzelSharedPrefences
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class BesinListesiViewModel(application: Application) : BaseViewModel(application) {
    val besinler = MutableLiveData<List<Besin>>()
    val besinHataMesaji = MutableLiveData<Boolean>()
    val besinYukleniyor = MutableLiveData<Boolean>()

    private val besinApiServis = BesinAPIService()
    private val disposable = CompositeDisposable()
    private val ozelSharedPrefences = OzelSharedPrefences(getApplication())
    private val guncellemeZamani = 10 * 60 * 1000 * 1000 * 1000L


    fun refreshData(){
        val kaydedilmeZamani = ozelSharedPrefences.zamaniAl()

        if (kaydedilmeZamani != null && kaydedilmeZamani != 0L && System.nanoTime() - kaydedilmeZamani < guncellemeZamani){
            verileriRoomdanAl()
        }else{
            verileriInternettenAl()
        }
    }

    fun refreshFromInternet(){
        verileriInternettenAl()
    }

    private fun verileriRoomdanAl(){
        besinYukleniyor.value = true

        launch {

            val besinListesi = BesinDatabase(getApplication()).besinDao().getAllBesin()
            besinleriGoster(besinListesi)

        }
    }

    private fun verileriInternettenAl(){
        besinYukleniyor.value = true

        disposable.add(
                besinApiServis.getData()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(object : DisposableSingleObserver<List<Besin>>(){
                            override fun onSuccess(t: List<Besin>) {
                                //Hata alırsak
                                sqliteSakla(t)
                            }

                            override fun onError(e: Throwable) {
                                // Hata alırsak
                                besinHataMesaji.value=true
                                besinYukleniyor.value = false
                                e.printStackTrace()
                            }

                        })
        )


    }

    private fun besinleriGoster(besinListeleri : List<Besin>){
        besinler.value = besinListeleri
        besinHataMesaji.value = false
        besinYukleniyor.value = false
    }

    private fun sqliteSakla(besinListesi : List<Besin>){
        launch {

            val dao = BesinDatabase(getApplication()).besinDao()
            dao.deleteAllBesin()
            val uuidListesi = dao.insertAll(*besinListesi.toTypedArray())
            var i = 0
            while ( i < besinListesi.size){
                besinListesi[i].uuid = uuidListesi[i].toInt()
                i++
            }
            besinleriGoster(besinListesi)
        }

        ozelSharedPrefences.zamaniKaydet(System.nanoTime())

    }



}