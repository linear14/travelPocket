package com.dongldh.travelpocket.content

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat

class DetailActivity : AppCompatActivity(), View.OnClickListener {

    var itemNumber: Int? = null
    var helper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        itemNumber = intent.getIntExtra("itemNumber", 0)
        helper = DBHelper(this)
        val db = helper!!.writableDatabase

        val cursorContent = db.rawQuery("select * from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
        cursorContent.moveToNext()
        val num = cursorContent.getString(0)
        val datecode = cursorContent.getString(1)
        val currency = cursorContent.getString(2)
        val type = cursorContent.getString(3)
        val isCash = cursorContent.getInt(5)
        val moneyUsed = cursorContent.getDouble(6)
        val used = cursorContent.getString(7)?:""
        val content = cursorContent.getString(8)
        // val image = cursorContent.getString(9)

        val cursorBudget = db.rawQuery("select rate_tofrom from t_budget where num=?", arrayOf(num))

        cursorBudget.moveToNext()
        val rate_tofrom = cursorBudget.getDouble(0)

        when(isCash) {
            0 -> {
                detail_cash_card.setImageResource(R.drawable.ic_pay_method)
            }
            1 -> {
                detail_cash_card.setImageResource(R.drawable.ic_money)
            }
        }

        when(type) {
            "식비" -> {
                detail_type.setImageResource(R.drawable.ic_eat_checked)
            }
            "쇼핑" -> {
                detail_type.setImageResource(R.drawable.ic_shopping_checked)
            }
            "관광" -> {
                detail_type.setImageResource(R.drawable.ic_travel_checked)
            }
            "교통" -> {
                detail_type.setImageResource(R.drawable.ic_traffic_checked)
            }
            "숙박" -> {
                detail_type.setImageResource(R.drawable.ic_sleep_checked)
            }
            "etc" -> {
                detail_type.setImageResource(R.drawable.ic_etc_checked)
            }
        }

        if(used.equals("")) {
            detail_title.text = type
        } else {
            detail_title.text = "${type} : ${used}"
        }

        val date = SimpleDateFormat("yyMMdd").parse(datecode)
        detail_date.text = SimpleDateFormat("yyyy년 MM월 dd일").format(date)

        detail_money.text = "${currency} ${String.format("%,d", moneyUsed.toInt())}"
        detail_money_mycountry.text = "${App.pref.myCurrency} ${String.format("%,d", (moneyUsed * rate_tofrom).toInt())}"

        if(!content.isNullOrEmpty()) {
            detail_content.text = content
        }
        // detail_image = "" 이미지가 존재한다면 동시에 detail_image_photo 역시 제거해줘야 함

        detail_close.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            detail_content -> {

            }

            detail_image_layout -> {

            }

            detail_delete -> {

            }

            detail_close -> {
                finish()
            }
         }
    }
}
