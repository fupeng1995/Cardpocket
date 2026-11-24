package com.example.cardpocket

import android.app.Application
import com.example.cardpocket.data.AppData

class CardPocketApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppData.init(this)
    }
}
