package net.natsucamellia.cooltracker.data

import android.app.Application
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.network.CoolApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val coolRepository: CoolRepository
    val authManager: AuthManager
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

    override val authManager by lazy {
        AuthManager(
            sharedPref = application.getSharedPreferences(PREF_NAME, Application.MODE_PRIVATE)
        )
    }

    override val coolRepository: CoolRepository by lazy {
        NetworkCoolRepository(
            coolApiService =  retrofitService,
            authManager = authManager
        )
    }
}