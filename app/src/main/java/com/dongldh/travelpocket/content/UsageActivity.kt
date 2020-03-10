package com.dongldh.travelpocket.content

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_usage.*

class UsageActivity : AppCompatActivity(), View.OnClickListener {
    // ContentActivity -> UsageActivity 받는 데이터
    var num: Int? = null
    var selected_day: Long? = null

    // 사용 될 핵심 값 (0은 false 1은 true)
    var isPlus: Int = 0
    var isCash: Int = 1
    var type: String? = "식비"
    var used: String? = null
    var detail_content: String? = null
    var imageUri: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage)

        num = intent.getIntExtra("num", 0)
        selected_day = intent.getLongExtra("selected_day", 0)

        // usage_currency.text = ;
        usage_plus.setOnClickListener(this)
        usage_minus.setOnClickListener(this)
        usage_cash.setOnClickListener(this)
        usage_card.setOnClickListener(this)
        usage_by_eat.setOnClickListener(this)
        usage_by_shop.setOnClickListener(this)
        usage_by_travel.setOnClickListener(this)
        usage_by_traffic.setOnClickListener(this)
        usage_by_sleep.setOnClickListener(this)
        usage_by_extra.setOnClickListener(this)

        used = usage_used.text.toString()

    }

    override fun onClick(v: View?) {
        when(v) {
            usage_plus -> {
                isPlus = 1
                usage_plus.setImageResource(R.drawable.ic_plus_checked)
                usage_minus.setImageResource(R.drawable.ic_minus_unchecked)
            }
            usage_minus -> {
                isPlus = 0
                usage_plus.setImageResource(R.drawable.ic_plus_unchecked)
                usage_minus.setImageResource(R.drawable.ic_minus_checked)
            }
            usage_cash -> {
                isCash = 1
                usage_cash.setImageResource(R.drawable.ic_money)
                usage_card.setImageResource(R.drawable.ic_card_unchecked)
            }
            usage_card -> {
                isCash = 0
                usage_cash.setImageResource(R.drawable.ic_money_unchecked)
                usage_card.setImageResource(R.drawable.ic_pay_method)
            }
            usage_by_eat -> {
                type = "식비"
                usage_eat.setImageResource(R.drawable.ic_eat_checked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_shop -> {
                type = "쇼핑"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_checked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_travel -> {
                type = "관광"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_checked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_traffic -> {
                type = "교통"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_checked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_sleep -> {
                type = "숙박"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_checked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_shop -> {
                type = "Etc"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_checked)
            }
        }
    }
}
