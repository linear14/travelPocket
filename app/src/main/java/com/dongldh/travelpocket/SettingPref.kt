package com.dongldh.travelpocket

import android.content.Context
import android.content.SharedPreferences

class SettingPref(context: Context) {
    val pref: SharedPreferences = context.getSharedPreferences("pref", 0)

    var myCountry: String?
        get() = pref.getString("country", "대한민국")
        set(value) = pref.edit().putString("country", value).apply()

    var myCurrency: String?
        get() = pref.getString("currency", "₩")
        set(value) = pref.edit().putString("currency", value).apply()

    var myFlagId: Int?
        get() = pref.getInt("flag", R.drawable.flag_kr)
        set(value) = pref.edit().putInt("flag", value!!).apply()
}