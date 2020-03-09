package com.dongldh.travelpocket

import android.net.Uri

data class DataTravel(
    var title: String? = null,
    var start_day: Long? = null,
    var end_day: Long? = null,
    var country: String? = null,
    var currency: String? = null,
    var flag: Int? = null,
    var cover_image: String? = null
)

data class DataBudget(
    var currency: String? = App.pref.myCurrency,
    var budget: Float? = 0.0f
)