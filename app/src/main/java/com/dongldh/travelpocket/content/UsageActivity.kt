package com.dongldh.travelpocket.content

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.R
import com.dongldh.travelpocket.profile_setting.CoverDialog
import com.dongldh.travelpocket.profile_setting.FROM_ALBUM
import com.dongldh.travelpocket.profile_setting.FROM_CAMERA
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_usage.*
import java.io.File
import java.text.SimpleDateFormat

val FROM_MEMO = 250
val FROM_PHOTO = 260

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
    var detail_content_temp: String? = null
    var imageUri: Uri? = null

    // 계산기 관련 변수
    var number = ""     // 숫자 버튼 누르면 문자열에 계속 추가됨
    var input = 0.0       // 그 문자열을 숫자로 변환 (계산될 값)
    var resultNum = 0.0   // 결과 값 저장
    var sign = ""
    var isLastNumber: Boolean = false
    var numberList = mutableListOf<String>()

    val coverDialog = CoverDialog()

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
        val selected_day = intent.getLongExtra("selected_day", 0)
        if(selected_day == 1L) {
            datecode = "1"
            usage_time_text.text = "Prepare"
        } else {
            datecode = SimpleDateFormat("yyMMdd").format(selected_day)
            usage_time_text.text = SimpleDateFormat("yyyy.MM.dd").format(selected_day)
        }
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
        usage_input.text = "0"
        usage_total.text = "0.0"

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
        cal_div.setOnClickListener(this)

        usage_photo.setOnClickListener(this)
        usage_memo.setOnClickListener(this)
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
                type = "etc"
                usage_eat.setImageResource(R.drawable.ic_eat_unchecked)
                usage_shop.setImageResource(R.drawable.ic_shopping_unchecked)
                usage_travel.setImageResource(R.drawable.ic_travel_unchecked)
                usage_traffic.setImageResource(R.drawable.ic_traffic_unchecked)
                usage_sleep.setImageResource(R.drawable.ic_sleep_unchecked)
                usage_extra.setImageResource(R.drawable.ic_etc_checked)
            }

            // 계산기 기능 관련
            cal_0 -> {
                if (!number.isBlank()) {
                    number += "0"
                    numberList.add("0")
                    isLastNumber = true
                    calculate()
                }
            }
            cal_1 -> {
                number += "1"
                numberList.add("1")
                isLastNumber = true
                calculate()
            }
            cal_2 -> {
                number += "2"
                numberList.add("2")
                isLastNumber = true
                calculate()
            }
            cal_3 -> {
                number += "3"
                numberList.add("3")
                isLastNumber = true
                calculate()
            }
            cal_4 -> {
                number += "4"
                numberList.add("4")
                isLastNumber = true
                calculate()
            }
            cal_5 -> {
                number += "5"
                numberList.add("5")
                isLastNumber = true
                calculate()
            }
            cal_6 -> {
                number += "6"
                numberList.add("6")
                isLastNumber = true
                calculate()
            }
            cal_7 -> {
                number += "7"
                numberList.add("7")
                isLastNumber = true
                calculate()
            }
            cal_8 -> {
                number += "8"
                numberList.add("8")
                isLastNumber = true
                calculate()
            }
            cal_9 -> {
                number += "9"
                numberList.add("9")
                isLastNumber = true
                calculate()
            }
            cal_dot -> {
                if(isLastNumber) {
                    number += "."
                    numberList.add(".")
                    isLastNumber = false
                    calculate()
                }
            }
            cal_plus -> {
                if(isLastNumber) {
                    number += "+"
                    numberList.add("+")
                    isLastNumber = false
                    calculate()
                }
                sign = "+"
            }
            cal_minus -> {
                if(isLastNumber) {
                    number += "-"
                    numberList.add("-")
                    isLastNumber = false
                    calculate()
                }
                sign = "-"
            }
            cal_mul -> {
                if(isLastNumber) {
                    number += "*"
                    numberList.add("*")
                    isLastNumber = false
                    calculate()
                }
                sign = "*"
            }
            cal_div -> {
                if(isLastNumber) {
                    number += "/"
                    numberList.add("/")
                    isLastNumber = false
                    calculate()
                }
                sign = "/"
            }
            cal_del -> {
                when {
                    number.length == 1 -> {
                        number = ""
                        usage_input.text = "0"
                        numberList.clear()
                        isLastNumber = false

                        usage_input.text = "0"
                        usage_total.text = "0.0"
                }

                    !number.isBlank() -> {
                        number = number.substring(0, number.length - 1)
                        isLastNumber = !(numberList[numberList.size - 2].equals("+") || numberList[numberList.size - 2].equals("+") ||
                                numberList[numberList.size - 2].equals("*") || numberList[numberList.size - 2].equals("/") ||
                                numberList[numberList.size - 2].equals("."))
                        numberList.removeAt(numberList.size - 1)
                        calculate()
                    }
                }
            }
            usage_photo -> {
                // 사진 기능 구현
                coverDialog.show(supportFragmentManager, "dialog_event")
            }

            usage_memo -> {
                // 메모 기능 구현
                val intent = Intent(this@UsageActivity, MemoActivity::class.java)
                intent.putExtra("detail_content", detail_content_temp)
                startActivityForResult(intent, FROM_MEMO)
            }

            usage_save -> {
                val helper = DBHelper(this)
                val db = helper.writableDatabase
                used = usage_used.text.toString()
                // (num integer, datecode, type, isPlus, isCash, moneyUsed, used, detail_content, image)

                var image: String? = null
                image = imageUri.toString()

                val contentValues = ContentValues()
                contentValues.put("num", num)
                contentValues.put("datecode", datecode)
                contentValues.put("currency", usage_currency.text.toString())
                contentValues.put("type", type)
                contentValues.put("isPlus", isPlus)
                contentValues.put("isCash", isCash)
                contentValues.put("moneyUsed", usage_total.text.toString().toDouble())
                contentValues.put("used", used)
                contentValues.put("detail_content", detail_content)
                contentValues.put("image", image)
                db.insert("t_content", null, contentValues)

                val cursorUsedMoney = db.rawQuery("select used_money_mycountry from t_travel where num=?", arrayOf(num.toString()))
                val cursorRateToFrom = db.rawQuery("select rate_tofrom from t_budget where num=? and currency=?", arrayOf(num.toString(), usage_currency.text.toString()))
                cursorUsedMoney.moveToNext()
                cursorRateToFrom.moveToNext()

                var usedMoney: Double? = cursorUsedMoney.getDouble(0)?:0.0
                if(isPlus == 0) {
                    usedMoney = usedMoney!! + (usage_total.text.toString().toDouble() * cursorRateToFrom.getDouble(0))
                } else {
                    usedMoney = usedMoney!! - (usage_total.text.toString().toDouble() * cursorRateToFrom.getDouble(0))
                }
                val contentValuesUsedMoney = ContentValues()
                contentValuesUsedMoney.put("used_money_mycountry", usedMoney)
                db.update("t_travel", contentValuesUsedMoney, "num=?", arrayOf(num.toString()))

                db.close()

                setResult(Activity.RESULT_OK)
                finish()
            }

            usage_back -> {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            FROM_MEMO -> {
                if(resultCode == Activity.RESULT_OK) {
                    detail_content_temp = data!!.getStringExtra("detail_content")
                    detail_content = detail_content_temp
                } else {
                    detail_content = detail_content_temp
                }
            }

            FROM_ALBUM -> {
                if (data?.data != null) {
                    try {
                        var albumFile: File? = null
                        albumFile = coverDialog.createImageFile()

                        coverDialog.photoUri = data.data
                        coverDialog.albumUri = Uri.fromFile(albumFile)

                        // log
                        Toast.makeText(this, "앨범사진 선택", Toast.LENGTH_SHORT).show()

                        imageUri = coverDialog.photoUri
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            FROM_CAMERA -> {
                val bitmap = BitmapFactory.decodeFile(coverDialog.currentPhotoPath)
                var exif: ExifInterface? = null

                try {
                    exif = ExifInterface(coverDialog.currentPhotoPath!!)
                } catch(e: Exception) {
                    e.printStackTrace()
                }

                var exifOrientation: Int? = null
                var exifDegree: Float? = null

                if(exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    exifDegree = coverDialog.exifOrientationToDegrees(exifOrientation).toFloat()
                } else {
                    exifDegree = 0.0F
                }

                // 비트맵을 우선 uri로 바꿔주기 (Uri 값을 데이터로 넘겨야해서..)
                imageUri = coverDialog.getImageUriFromBitmap(this, coverDialog.rotate(bitmap, exifDegree))
            }
        }
    }


    fun calculate() {
        // Logging
        for(i in numberList) {
            Log.d("Calculator: numberList", i)
        }

        usage_input.text = number
        var result = 0.0
        var realNumberList = mutableListOf<String>()
        var realNumberMaker: String = ""

        // 여기까지 realNumberList에 숫자와 부호가 분리 됨 + 마지막이 . 혹은 부호임을 인지함
        for(i in numberList.indices) {
            val input = numberList[i]
            if(input.equals("+") || input.equals("-") || input.equals("*") || input.equals("/")) {
                // 마지막이 + or - : 그 뒤에 0과 연산 ,,,,,,  마지막이 * or / : 그 뒤에 1과 연산
                if(i == numberList.size - 1){
                    if(input.equals("+") || input.equals("-")){
                        realNumberList.add(realNumberMaker)
                        realNumberList.add(input)
                        realNumberList.add("0")
                        continue
                    } else {
                        realNumberList.add(realNumberMaker)
                        realNumberList.add(input)
                        realNumberList.add("1")
                        continue
                    }
                }
                realNumberList.add(realNumberMaker)
                realNumberMaker = ""
                realNumberList.add(input)
                continue
            }
            if(i == numberList.size - 1) {
                if(input.equals(".")) {
                    realNumberMaker += input
                    realNumberMaker += "0"
                    realNumberList.add(realNumberMaker)
                    continue
                }
                realNumberMaker += input
                realNumberList.add(realNumberMaker)
            }
            realNumberMaker += input
        }

        // Logging
        for(i in realNumberList) {
            Log.d("Calculator", i)
        }

        while(realNumberList.size > 1) {
            while(realNumberList.indexOf("*") != -1 || realNumberList.indexOf("/") != -1) {
                val mulIndex = realNumberList.indexOf("*")
                val divIndex = realNumberList.indexOf("/")
                // 순서대로.. 곱하기만 남아있는 경우, 나누기만 남아있는 경우, 모두 남아있는 경우
                if(realNumberList.indexOf("/") == -1 && realNumberList.indexOf("*") != -1) {
                    realNumberList[mulIndex - 1] = (realNumberList[mulIndex - 1].toDouble() * realNumberList[mulIndex + 1].toDouble()).toString()
                    realNumberList.removeAt(mulIndex)
                    realNumberList.removeAt(mulIndex)
                } else if (realNumberList.indexOf("*") == -1 && realNumberList.indexOf("/") != -1) {
                    realNumberList[divIndex - 1] = String.format("%.4f",(realNumberList[divIndex - 1].toDouble() / realNumberList[divIndex + 1].toDouble()))
                    realNumberList.removeAt(divIndex)
                    realNumberList.removeAt(divIndex)
                } else {
                    if(mulIndex > divIndex){
                        realNumberList[divIndex - 1] = String.format("%.4f",(realNumberList[divIndex - 1].toDouble() / realNumberList[divIndex + 1].toDouble()))
                        realNumberList.removeAt(divIndex)
                        realNumberList.removeAt(divIndex)
                    } else {
                        realNumberList[mulIndex - 1] = (realNumberList[mulIndex - 1].toDouble() * realNumberList[mulIndex + 1].toDouble()).toString()
                        realNumberList.removeAt(mulIndex)
                        realNumberList.removeAt(mulIndex)
                    }
                }
            }

            while(realNumberList.indexOf("+") != -1 || realNumberList.indexOf("-") != -1) {
                val plusIndex = realNumberList.indexOf("+")
                val minusIndex = realNumberList.indexOf("-")
                // 순서대로.. 더하기만 남아있는 경우, 빼기만 남아있는 경우, 모두 남아있는 경우
                if(realNumberList.indexOf("-") == -1 && realNumberList.indexOf("+") != -1) {
                    realNumberList[plusIndex - 1] = (realNumberList[plusIndex - 1].toDouble() + realNumberList[plusIndex + 1].toDouble()).toString()
                    realNumberList.removeAt(plusIndex)
                    realNumberList.removeAt(plusIndex)
                } else if (realNumberList.indexOf("+") == -1 && realNumberList.indexOf("-") != -1) {
                    realNumberList[minusIndex - 1] = (realNumberList[minusIndex - 1].toDouble() - realNumberList[minusIndex + 1].toDouble()).toString()
                    realNumberList.removeAt(minusIndex)
                    realNumberList.removeAt(minusIndex)
                } else {
                    if(plusIndex > minusIndex){
                        realNumberList[minusIndex - 1] = (realNumberList[minusIndex - 1].toDouble() - realNumberList[minusIndex + 1].toDouble()).toString()
                        realNumberList.removeAt(minusIndex)
                        realNumberList.removeAt(minusIndex)
                    } else {
                        realNumberList[plusIndex - 1] = (realNumberList[plusIndex - 1].toDouble() + realNumberList[plusIndex + 1].toDouble()).toString()
                        realNumberList.removeAt(plusIndex)
                        realNumberList.removeAt(plusIndex)
                    }
                }
            }
        }
        result = realNumberList[0].toDouble()
        usage_total.text = result.toString()
    }

}
