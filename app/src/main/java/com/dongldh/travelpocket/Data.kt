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

abstract class DetailType {
    abstract val type: Int
    companion object {
        val DAY_TYPE = 1
        val DETAIL_TYPE = 2
    }
}

data class DataDay(
    var day: Long? = null
): DetailType() {
    override val type: Int
        get() = DetailType.DAY_TYPE
}

data class DataDetail(
    var moneyUsed: Float? = null,
    var type_used: String? = null
): DetailType() {
    override val type: Int
        get() = DetailType.DETAIL_TYPE
}

