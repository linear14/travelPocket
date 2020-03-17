package com.dongldh.travelpocket

import android.content.Context
import android.content.SharedPreferences

class SettingPref(context: Context) {
    val pref: SharedPreferences = context.getSharedPreferences("pref", 0)

    var myCountry: String?
        get() = pref.getString("country", "대한민국")
        set(value) = pref.edit().putString("country", value).apply()

    var myCode: String?
        get() = pref.getString("code", "KRW")
        set(value) = pref.edit().putString("code", value).apply()

    var myCurrency: String?
        get() = pref.getString("currency", "₩")
        set(value) = pref.edit().putString("currency", value).apply()

    var myFlagId: Int?
        get() = pref.getInt("flag", R.drawable.flag_kr)
        set(value) = pref.edit().putInt("flag", value!!).apply()

    var myPayMethod: String?
        get() = pref.getString("payMethod", "현금")
        set(value) = pref.edit().putString("payMethod", value).apply()

    var myPrepare: String?
        get() = pref.getString("prepare", "보여주기")
        set(value) = pref.edit().putString("prepare", value).apply()

}