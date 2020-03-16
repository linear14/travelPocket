package com.dongldh.travelpocket.content

import android.app.Activity
import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_usage.*
import java.text.SimpleDateFormat

class UsageActivity : AppCompatActivity(), View.OnClickListener {
    // ContentActivity -> UsageActivity 받는 데이터
    var num: Int? = null
    var datecode: String? = null

    // 사용 될 핵심 값 (0은 false 1은 true)
    var currencyList: MutableList<String> = mutableListOf()
    var currencyListIndex = 0
    var isPlus: Int = 0
    var isCash: Int = 1
    var type: String? = "식비"
    var used: String? = null
    var detail_content: String? = null
    var imageUri: Int? = null

    // 계산기 관련 변수
    /*var number = ""     // 숫자 버튼 누르면 문자열에 계속 추가됨
    var input = 0.0       // 그 문자열을 숫자로 변환 (계산될 값)
    var resultNum = 0.0   // 결과 값 저장
    var sign = ""
    var isLastNumber: Boolean = false*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_usage)
        // SharedPref 설정에 의한 조작 우선
        if(App.pref.myPayMethod == "현금") {
            isCash = 1
            usage_cash.setImageResource(R.drawable.ic_money)
            usage_card.setImageResource(R.drawable.ic_card_unchecked)
        } else {
            isCash = 0
            usage_cash.setImageResource(R.drawable.ic_money_unchecked)
            usage_card.setImageResource(R.drawable.ic_pay_method)
        }

        num = intent.getIntExtra("num", 0)
        datecode = SimpleDateFormat("yyMMdd").format(intent.getLongExtra("selected_day", 0))

        Log.d("Usage", num.toString())

        selectCurrencyDB()

        usage_currency.text = currencyList[0]
        usage_currency.setOnClickListener(this)

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

        // 계산기 작동 관련 코드 입력 //
        /*usage_input.text = 0.toString()

        cal_1.setOnClickListener(this)
        cal_2.setOnClickListener(this)
        cal_3.setOnClickListener(this)
        cal_4.setOnClickListener(this)
        cal_5.setOnClickListener(this)
        cal_6.setOnClickListener(this)
        cal_7.setOnClickListener(this)
        cal_8.setOnClickListener(this)
        cal_9.setOnClickListener(this)
        cal_0.setOnClickListener(this)
        cal_del.setOnClickListener(this)
        cal_dot.setOnClickListener(this)

        cal_plus.setOnClickListener(this)
        cal_minus.setOnClickListener(this)
        cal_mul.setOnClickListener(this)
        cal_div.setOnClickListener(this)*/

        used = usage_used.text.toString()
        usage_back.setOnClickListener(this)
        usage_save.setOnClickListener(this)
    }

    fun selectCurrencyDB() {
        val helper = DBHelper(this)
        val db = helper.writableDatabase
        val cursor = db.rawQuery("select currency from t_budget where num=?", arrayOf(num.toString()))
        while(cursor.moveToNext()) {
            currencyList.add(cursor.getString(0))
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            usage_currency -> {
                if(currencyListIndex != currencyList.size - 1) {
                    usage_currency.text = currencyList[++currencyListIndex]
                } else {
                    currencyListIndex = 0
                    usage_currency.text = currencyList[0]
                }
            }
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
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_shop -> {
                type = "쇼핑"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_checked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_travel -> {
                type = "관광"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_checked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_traffic -> {
                type = "교통"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_checked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_sleep -> {
                type = "숙박"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_checked)
                usage_extra.setImageResource(R.drawable.ic_etc_unchecked)
            }
            usage_by_extra -> {
                type = "Etc"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_checked)
            }

            // 계산기 기능 관련
            /*cal_0 -> {
                if (!number.isBlank()) {
                    number += "0"
                    updateInput()
                }
            }
            cal_1 -> {
                number += "1"
                updateInput()
            }
            cal_2 -> {
                number += "2"
                updateInput()
            }
            cal_3 -> {
                number += "3"
                updateInput()
            }
            cal_4 -> {
                number += "4"
                updateInput()
            }
            cal_5 -> {
                number += "5"
                updateInput()
            }
            cal_6 -> {
                number += "6"
                updateInput()
            }
            cal_7 -> {
                number += "7"
                updateInput()
            }
            cal_8 -> {
                number += "8"
                updateInput()
            }
            cal_9 -> {
                number += "9"
                updateInput()
            }
            cal_dot -> {
                if(isLastNumber) number += "."
                updateInput()
            }
            cal_plus -> {
                if(isLastNumber) number += "+"
                sign = "+"
                updateInput()
            }
            cal_minus -> {
                if(isLastNumber) number += "-"
                sign = "-"
                updateInput()
            }
            cal_mul -> {
                if(isLastNumber) number += "*"
                sign = "*"
                updateInput()
            }
            cal_div -> {
                if(isLastNumber) number += "/"
                sign = "/"
                updateInput()
            }
            cal_del -> {
                when {
                    number.length == 1 -> {
                        number = ""
                        usage_input.text = "0"
                    }

                    !number.isBlank() -> {
                        number = number.substring(0, number.length - 1)
                        updateInput()
                    }
                }
            }*/

            usage_save -> {
                val helper = DBHelper(this)
                val db = helper.writableDatabase

                // (num integer, datecode, type, isPlus, isCash, moneyUsed, used, detail_content, image)

                val contentValues = ContentValues()
                contentValues.put("num", num)
                contentValues.put("datecode", datecode)
                contentValues.put("currency", usage_currency.text.toString())
                contentValues.put("type", type)
                contentValues.put("isPlus", isPlus)
                contentValues.put("isCash", isCash)
                contentValues.put("moneyUsed", usage_total_temp.text.toString().toDouble())
                contentValues.put("used", used)

                // 나중에 넣어주기 (혹은 해당 액티비티에서 db생성해서 넣어주는 방법도 있음)
                /*
                contentValues.put("detail_content", "null")
                contentValues.put("image", "null")*/
                db.insert("t_content", null, contentValues)
                db.close()

                setResult(Activity.RESULT_OK)
                finish()
            }

            usage_back -> {
                finish()
            }
        }
    }

    /*fun updateInput() {
        usage_input.text = number
        if(!number.isBlank()) {
            isLastNumber = !(number.substring(number.length - 1).equals(".")
                    || number.substring(number.length - 1).equals("+")
                    || number.substring(number.length - 1).equals("-")
                    || number.substring(number.length - 1).equals("*")
                    || number.substring(number.length - 1).equals("/"))
        }
    }

    fun calculate(number: String): Double {
        var result = 0.0
        if("+" in number) {
            val list = number.split("+")
            for(i in list) {
                result += calculate(i)
            }
        }
        if("-" in number) {
            val list = number.split("-")
            for(i in list) {
                result +- calculate(i)
            }
        }
        return result
    }*/
}
