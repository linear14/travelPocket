package com.dongldh.travelpocket.profile_setting

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.DataEntire
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_entire_budget.*
import kotlinx.android.synthetic.main.item_entire_budget.view.*


val ENTIRE = 2225
class EntireBudgetActivity : AppCompatActivity() {

    val helper = DBHelper(this)
    val list = mutableListOf<DataEntire>()
    lateinit var num : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entire_budget)
        title = "에산/화폐"

        num = intent.getStringExtra("num")
        selectDB()
    }

    fun selectDB() {
        val db = helper.writableDatabase
        val cursor = db.rawQuery("select * from t_budget where num=?", arrayOf(num))

        while(cursor.moveToNext()) {
            val code = cursor.getString(3)
            val flag = cursor.getInt(6)
            val currency = cursor.getString(1)
            val money = cursor.getFloat(2)

            list.add(DataEntire(code, currency, money, flag))
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

            holder.flag.setImageResource(flag)
            holder.code.text = code
            holder.currency_money.text = "${currency} ${String.format("%.1f", money)}"

            holder.itemView.setOnClickListener {
                val intent = Intent(this@EntireBudgetActivity, BudgetActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("reqeustCode", "Entire")
                startActivityForResult(intent, ENTIRE)
            }
        }
    }
    

}
