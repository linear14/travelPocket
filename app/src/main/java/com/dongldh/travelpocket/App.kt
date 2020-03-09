package com.dongldh.travelpocket

import android.app.Application

class App : Application() {

    companion object {
        lateinit var pref: SettingPref
    }

    override fun onCreate() {
        pref = SettingPref(applicationContext)
        super.onCreate()
    }

}