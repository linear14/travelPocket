package com.dongldh.travelpocket.content

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat

val FROM_DETAIL = 114
class DetailActivity : AppCompatActivity(), View.OnClickListener {

    var itemNumber: Int? = null
    var helper: DBHelper? = null

    var num: String? = null
    var isPlus: Int? = null
    var moneyUsed: Double? = null
    var rate_tofrom: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        itemNumber = intent.getIntExtra("itemNumber", 0)
        helper = DBHelper(this)
        val db = helper!!.writableDatabase

        val cursorContent = db.rawQuery("select * from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
        cursorContent.moveToNext()
        num = cursorContent.getString(0)
        val datecode = cursorContent.getString(1)
        val currency = cursorContent.getString(2)
        val type = cursorContent.getString(3)
        isPlus = cursorContent.getInt(4)
        val isCash = cursorContent.getInt(5)
        moneyUsed = cursorContent.getDouble(6)
        val used = cursorContent.getString(7)?:""
        val content = cursorContent.getString(8)
        val image = cursorContent.getString(9)

        val cursorBudget = db.rawQuery("select rate_tofrom from t_budget where num=?", arrayOf(num))

        cursorBudget.moveToNext()
        rate_tofrom = cursorBudget.getDouble(0)

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

        detail_money.text = "${currency} ${String.format("%,d", moneyUsed!!.toInt())}"
        detail_money_mycountry.text = "${App.pref.myCurrency} ${String.format("%,d", (moneyUsed!! * rate_tofrom!!).toInt())}"

        if(!content.isNullOrEmpty()) {
            detail_content.text = content
        }

        if(!image.isNullOrEmpty()) {
            detail_image.setImageURI(Uri.parse(image))
            detail_image_photo.visibility = View.GONE
        }


        detail_content.setOnClickListener(this)
        detail_image_layout.setOnClickListener(this)
        detail_delete.setOnClickListener(this)
        detail_close.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            detail_content -> {
                val db = helper!!.writableDatabase
                val cursor = db.rawQuery("select detail_content from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
                cursor.moveToNext()

                val content = cursor.getString(0)

                val intent = Intent(this@DetailActivity, MemoActivity::class.java)
                intent.putExtra("detail_content", content)
                intent.putExtra("itemNumber", itemNumber.toString())
                intent.putExtra("requestCode", "DetailActivity")
                Log.d("ItemNumber", itemNumber.toString())
                startActivityForResult(intent, FROM_DETAIL)
            }

            detail_image_layout -> {

            }

            detail_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("항목 삭제하기")
                    .setMessage("정말 이 항목을 삭제하시겠습니까?\n삭제 후 복구가 불가능합니다.")

                builder.setPositiveButton("확인") { dialog, which ->
                    val db = helper!!.writableDatabase
                    db.delete("t_content", "itemNumber=?", arrayOf(itemNumber.toString()))

                    val cursor = db.rawQuery("select used_money_mycountry from t_travel where num=?", arrayOf(num.toString()))
                    cursor.moveToNext()
                    var usedMoney = cursor.getDouble(0)

                    if(isPlus == 0) {
                        usedMoney = usedMoney - (moneyUsed!! * rate_tofrom!!)
                    } else {
                        usedMoney = usedMoney + (moneyUsed!! * rate_tofrom!!)
                    }

                    val contentValuesUsedMoney = ContentValues()
                    contentValuesUsedMoney.put("used_money_mycountry", usedMoney)
                    db.update("t_travel", contentValuesUsedMoney, "num=?", arrayOf(num.toString()))

                    db.close()

                    dialog.dismiss()
                    finish()
                }

                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

            detail_close -> {
                finish()
            }
         }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            FROM_DETAIL -> {
                val db = helper!!.writableDatabase
                val cursorContent = db.rawQuery("select detail_content from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
                cursorContent.moveToNext()
                val content = cursorContent.getString(0)

                if(!content.isNullOrEmpty()) {
                    detail_content.text = content
                } else {
                    detail_content.text = "지출에 대해 노트해보세요."
                }

                db.close()
            }
        }
    }
}
