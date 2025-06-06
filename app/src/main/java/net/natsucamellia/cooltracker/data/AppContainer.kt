package net.natsucamellia.cooltracker.data

import android.app.Application
import net.natsucamellia.cooltracker.network.CoolApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val coolRepository: CoolRepository
}

class DefaultAppContainer(
    private val application: Application
) : AppContainer {
    private val PREF_NAME = "cool_tracker_prefs"
    private val coolApiUrl = "https://cool.ntu.edu.tw/api/v1/"
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(coolApiUrl)
        .build()
    private val retrofitService: CoolApiService by lazy {
        retrofit.create(CoolApiService::class.java)
    }

    override val coolRepository: CoolRepository by lazy {
        NetworkCoolRepository(
            coolApiService =  retrofitService,
            sharedPref = application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE)
        )
    }
}