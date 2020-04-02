package com.dongldh.travelpocket.profile_setting

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.*
import kotlinx.android.synthetic.main.activity_entire_budget.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_entire_budget.view.*


const val ENTIRE_UPDATE = 2225
const val ENTIRE_INSERT = 2227
class EntireBudgetActivity : AppCompatActivity(), View.OnClickListener {

    val helper = DBHelper(this)
    val list = mutableListOf<DataEntire>()
    lateinit var num : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entire_budget)
        title = "에산/화폐"

        num = intent.getStringExtra("num")
        selectDB()
        entire_add_button.setOnClickListener(this)
    }

    fun selectDB() {
        val db = helper.writableDatabase
        val cursor = db.rawQuery("select * from t_budget where num=?", arrayOf(num))

        while(cursor.moveToNext()) {
            val code = cursor.getString(3)
            val flag = cursor.getInt(6)
            val currency = cursor.getString(1)
            val money = cursor.getFloat(2)
            val rate_fromto = cursor.getDouble(4)

            list.add(DataEntire(code, currency, money, flag, rate_fromto))
        }
        entire_recycler.layoutManager = LinearLayoutManager(this)
        entire_recycler.adapter = EntireAdapter(list)
    }

    inner class EntireViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val code = view.entire_code
        val flag = view.entire_flag
        val currency_money = view.entire_currency_money
    }

    inner class EntireAdapter(val list: MutableList<DataEntire>): RecyclerView.Adapter<EntireViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntireViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return EntireViewHolder(layoutInflater.inflate(R.layout.item_entire_budget, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: EntireViewHolder, position: Int) {
            val data = list[position]

            val code = data.code
            val currency = data.currency
            val flag = data.flag
            val money = data.money
            val rate_fromto = data.rate_fromto

            holder.flag.setImageResource(flag)
            holder.code.text = code
            holder.currency_money.text = "${currency} ${String.format("%.1f", money)}"

            holder.itemView.setOnClickListener {
                val intent = Intent(this@EntireBudgetActivity, BudgetActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("requestCode", "EntireUpdate")

                intent.putExtra("currency", currency)
                intent.putExtra("code", code)
                intent.putExtra("budget", money)
                intent.putExtra("flag", flag)
                intent.putExtra("position", position)
                intent.putExtra("originalRate", rate_fromto)

                startActivityForResult(intent, ENTIRE_UPDATE)
            }
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            entire_add_button -> {
                val intent = Intent(this@EntireBudgetActivity, BudgetActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("requestCode", "Entire")

                intent.putExtra("currency", App.pref.myCurrency)
                intent.putExtra("code", App.pref.myCode)
                intent.putExtra("budget", 0.0F)
                intent.putExtra("flag", App.pref.myFlagId)

                startActivityForResult(intent, ENTIRE_INSERT)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            ENTIRE_INSERT -> {
                if(resultCode == Activity.RESULT_OK) {
                    val db = helper.writableDatabase

                    val currency = data!!.getStringExtra("currency")
                    val code = data.getStringExtra("code")

                    val rate_fromto_input = data.getStringExtra("rate_fromto")
                    val rate_tofrom_input = data.getStringExtra("rate_tofrom")
                    val flag = data.getIntExtra("flag", 0)

                    val rate_fromto = rate_fromto_input.toDouble()
                    val rate_tofrom = rate_tofrom_input.toDouble()

                    val input = data!!.getStringExtra("budget")
                    val budget = input.toFloat()

                    list.add(DataEntire(code, currency, budget, flag))
                    entire_recycler.layoutManager = LinearLayoutManager(this)
                    entire_recycler.adapter = EntireAdapter(list)

                    // db에 추가 하는 작업 진행하자
                    val contentValues_budget = ContentValues()
                    contentValues_budget.put("num", num)
                    contentValues_budget.put("currency", currency)
                    contentValues_budget.put("money", budget)
                    contentValues_budget.put("code", code)
                    contentValues_budget.put("rate_fromto", rate_fromto)
                    contentValues_budget.put("rate_tofrom", rate_tofrom)
                    contentValues_budget.put("flag", flag)

                    db.insert("t_budget", null, contentValues_budget)

                    val cursor = db.rawQuery("select total_money_mycountry from t_travel where num=?", arrayOf(num))
                    cursor.moveToNext()
                    val total_money_mycountry = cursor.getDouble(0) + (budget * rate_tofrom)
                    val contentValuesTotalMoney = ContentValues()
                    contentValuesTotalMoney.put("total_money_mycountry", total_money_mycountry)
                    db.update("t_travel", contentValuesTotalMoney, "num=?", arrayOf(num))
                    db.close()
                }
            }

            ENTIRE_UPDATE -> {
                if(resultCode == Activity.RESULT_OK) {
                    val db = helper.writableDatabase

                    val currency = data!!.getStringExtra("currency")
                    val code = data.getStringExtra("code")

                    val rate_fromto_input = data.getStringExtra("rate_fromto")
                    val rate_tofrom_input = data.getStringExtra("rate_tofrom")
                    val flag = data.getIntExtra("flag", 0)

                    val rate_fromto = rate_fromto_input.toDouble()
                    val rate_tofrom = rate_tofrom_input.toDouble()

                    val input = data!!.getStringExtra("budget")
                    val budget = input.toFloat()

                    list.set(data.getIntExtra("position", -1), DataEntire(code, currency, budget, flag))
                    entire_recycler.layoutManager = LinearLayoutManager(this)
                    entire_recycler.adapter = EntireAdapter(list)

                    // db 업데이트를 하던, 혹은 화폐가 바뀌었을 경우면 db 몽땅 삭제 후 업데이트
                    if(data.getStringExtra("isCurrencyChanged") == "true") {
                        db.delete("t_content", "num=? and currency=?", arrayOf(num, data.getStringExtra("originalCurrency")))
                    }
                    val contentValues = ContentValues()
                    contentValues.put("currency", currency)
                    contentValues.put("code", code)
                    contentValues.put("rate_fromto", rate_fromto)
                    contentValues.put("rate_tofrom", rate_tofrom)
                    contentValues.put("flag", flag)
                    contentValues.put("money", budget)

                    db.update("t_budget", contentValues, "num=? and currency=?", arrayOf(num, data.getStringExtra("originalCurrency")))

                    val cursor = db.rawQuery("select total_money_mycountry from t_travel where num=?", arrayOf(num))
                    cursor.moveToNext()
                    val total_money_mycountry = cursor.getDouble(0) -
                            (data.getFloatExtra("originalBudget", 0.0f) * data.getDoubleExtra("originalRate", 1.0)) + (budget * rate_tofrom)
                    val contentValuesTotalMoney = ContentValues()
                    contentValuesTotalMoney.put("total_money_mycountry", total_money_mycountry)
                    db.update("t_travel", contentValuesTotalMoney, "num=?", arrayOf(num))
                    db.close()

                }
            }
        }
    }

}
