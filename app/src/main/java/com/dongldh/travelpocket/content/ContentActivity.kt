package com.dongldh.travelpocket.content

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.ContentView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.*
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.item_content.view.*
import kotlinx.android.synthetic.main.item_content_day.view.*
import kotlinx.android.synthetic.main.item_content_detail.view.*
import java.text.SimpleDateFormat

class ContentActivity : AppCompatActivity(), View.OnClickListener {
    val helper = DBHelper(this)

    var list: MutableList<Long> = mutableListOf()
    var list_detail: MutableList<DetailType> = mutableListOf()

    var selected_day: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        selectContentDayDB()
        fab.setOnClickListener(this)
    }

    fun selectContentDayDB() {
        list = mutableListOf()
        val db = helper.writableDatabase

        val num = intent.getIntExtra("num", 0)
        val cursor = db.rawQuery("select start_day, end_day from t_travel where num=?", arrayOf(num.toString()))

        cursor.moveToNext()
        val start = cursor.getLong(0)
        val end = cursor.getLong(1) + 1000

        for(i in start .. end step 24*60*60*1000) {
            // long을 월과 일로 만들기
            list.add(i)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ContentAdapter(list)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.content_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    inner class ContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_day = view.content_day
        val content_month = view.content_month
    }

    inner class DateViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_day_text = view.content_day_text
    }

    inner class DetailViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_detail_text = view.content_detail_text
        val content_type_text = view.content_type_text
        val content_image = view.content_image
    }

    inner class ContentAdapter(val list: MutableList<Long>): RecyclerView.Adapter<ContentViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return ContentViewHolder(layoutInflater.inflate(R.layout.item_content, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
            val item = list[position]

            // 아래는 수정이 좀 필요함
            holder.content_day.text = SimpleDateFormat("d").format(item).toString()
            holder.content_month.text = "${SimpleDateFormat("M").format(item)}월"

            holder.itemView.setOnClickListener {
                Toast.makeText(applicationContext, "동현아 일하자", Toast.LENGTH_SHORT).show()
                // 날짜정보 들어간 리사이클러뷰 만들기
                // 아이템의 시간정보 보내주기
                list_detail = mutableListOf()
                list_detail.add(DataDay(item))
                selected_day = item
                selectDetailDB(item)
            }
        }

    }

    fun selectDetailDB(item: Long) {

        val db = helper.writableDatabase
        val num = intent.getIntExtra("num", 0)
        val datecode = SimpleDateFormat("yyMMdd").format(item)
        val isExistSQL = db.rawQuery("select count(*) from t_content where num=? and datecode=?",
            arrayOf(num.toString(), datecode))
        isExistSQL.moveToNext()
        val isExist = isExistSQL.getInt(0)
        if(isExist != 0){
            val cursor = db.rawQuery("select type, moneyUsed from t_content where num=? and datecode=?",
                arrayOf(num.toString(), datecode))

            while(cursor.moveToNext()) {
                val moneyUsed = cursor.getFloat(1)
                val type = cursor.getString(0)

                list_detail.add(DataDetail(moneyUsed, type))
            }
        }

        recycler_content.layoutManager = LinearLayoutManager(this@ContentActivity)
        recycler_content.adapter = DetailAdapter(list_detail)
    }


    inner class DetailAdapter(val list: MutableList<DetailType>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return list[position].type
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
           when(viewType) {
                DetailType.DAY_TYPE -> {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    return DateViewHolder(layoutInflater.inflate(R.layout.item_content_day, parent, false))
                }
                else -> {
                   val layoutInflater = LayoutInflater.from(parent.context)
                   return DetailViewHolder(layoutInflater.inflate(R.layout.item_content_detail, parent, false))
                }
           }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemType = list[position]

            when{
                itemType.type == DetailType.DAY_TYPE -> {
                    val viewHolder = holder as DateViewHolder
                    val item = itemType as DataDay

                    viewHolder.content_day_text.text = SimpleDateFormat("yyyy년 MM월 dd일").format(item.day)
                }
                else -> {
                    val viewHolder = holder as DetailViewHolder
                    val item = itemType as DataDetail

                    viewHolder.content_detail_text.text = item.moneyUsed.toString()
                    viewHolder.content_type_text.text = item.type_used
                    when(viewHolder.content_type_text.text) {
                        "식비" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_eat_checked)
                        }
                        "쇼핑" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_shopping_checked)
                        }
                        "관광" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_travel_checked)
                        }
                        "교통" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_traffic_checked)
                        }
                        "숙박" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_sleep_checked)
                        }
                        "etc" -> {
                            viewHolder.content_image.setImageResource(R.drawable.ic_etc_checked)
                        }
                    }
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            fab -> {
                val intent = Intent(this, UsageActivity::class.java)
                intent.putExtra("num", intent.getIntExtra("num", 0))
                intent.putExtra("selected_day", selected_day)
                startActivity(intent)
            }
        }
    }

}
