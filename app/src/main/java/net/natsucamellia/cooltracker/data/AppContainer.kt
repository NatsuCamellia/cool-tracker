package net.natsucamellia.cooltracker.data

import android.app.Application
import androidx.room.Room
import net.natsucamellia.cooltracker.auth.AuthManager
import net.natsucamellia.cooltracker.data.local.LocalCoolDataProviderImpl
import net.natsucamellia.cooltracker.data.local.db.CoolDatabase
import net.natsucamellia.cooltracker.data.remote.RemoteCoolDataProviderImpl
import net.natsucamellia.cooltracker.data.remote.api.CoolApiService
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

    private val coolDatabase by lazy {
        Room.databaseBuilder(
            application.applicationContext,
            CoolDatabase::class.java,
            "profile"
        ).build()
    }

    private val localCoolDataProvider by lazy {
        LocalCoolDataProviderImpl(
            profileDao = coolDatabase.profileDao(),
            courseWithAssignmentsDao = coolDatabase.courseWithAssignmentsDao()
        )
    }

    private val remoteCoolDataProvider by lazy {
        RemoteCoolDataProviderImpl(
            coolApiService = retrofitService
        )
    }

    override val coolRepository: CoolRepository by lazy {
        NetworkCoolRepository(
            localCoolDataProvider = localCoolDataProvider,
            remoteCoolDataProvider = remoteCoolDataProvider,
            authManager = authManager
        )
    }
}