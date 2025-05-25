package net.natsucamellia.cooltracker.data

import net.natsucamellia.cooltracker.network.CoolApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val coolRepository: CoolRepository
}

class DefaultAppContainer : AppContainer {
    private val coolApiUrl = "https://cool.ntu.edu.tw/api/v1/"
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(coolApiUrl)
        .build()
    private val retrofitService: CoolApiService by lazy {
        retrofit.create(CoolApiService::class.java)
    }

    override val coolRepository: CoolRepository by lazy {
        NetworkCoolRepository(retrofitService)
    }
}