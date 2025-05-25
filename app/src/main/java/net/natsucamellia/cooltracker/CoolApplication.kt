package net.natsucamellia.cooltracker

import android.app.Application
import net.natsucamellia.cooltracker.data.AppContainer
import net.natsucamellia.cooltracker.data.DefaultAppContainer

class CoolApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}