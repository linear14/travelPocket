package com.dongldh.travelpocket

import android.net.Uri

data class DataTravel(
    var title: String? = null,
    var start_day: Long? = null,
    var end_day: Long? = null,
    var country: String? = null,
    var flag: Int? = null,
    var cover_image: String? = null
)