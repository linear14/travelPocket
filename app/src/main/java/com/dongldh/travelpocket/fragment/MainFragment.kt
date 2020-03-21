package com.dongldh.travelpocket.fragment


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.*
import com.dongldh.travelpocket.content.ContentActivity
import kotlinx.android.synthetic.main.activity_profile.view.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import kotlinx.android.synthetic.main.item_main.view.*
import kotlinx.android.synthetic.main.item_main.view.flag_image
import java.text.SimpleDateFormat

class MainFragment : Fragment() {
    lateinit var recyclerView: RecyclerView
    var list: MutableList<DataTravel> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        recyclerView = view.recycler_view

        // 여행 목록 조회 기능
        selectDB()

        return view
    }

    fun selectDB() {
        list = mutableListOf()
        val helper = DBHelper(this.context!!)
        val db = helper.writableDatabase
        val cursor = db.rawQuery("select * from t_travel", null)

        while(cursor.moveToNext()) {
            val num = cursor.getInt(0)
            val title = cursor.getString(1)
            val start_day = cursor.getLong(2)
            val end_day = cursor.getLong(3)
            val country = cursor.getString(4)
            val currency = cursor.getString(5)
            val flag = cursor.getInt(6)
            val cover_image = cursor.getString(7)
            val used_money_mycountry = cursor.getDouble(10)

            // log
            Log.d("MainFragment", "cover_image : ${cover_image}")

            val dataTravel = DataTravel(num, title, start_day, end_day, country, currency, flag, cover_image, used_money_mycountry)
            list.add(dataTravel)
        }

        recyclerView.layoutManager = GridLayoutManager(this.context, 2)
        recyclerView.adapter = MainAdapter(list)

        cursor.close()
        db.close()
    }

    inner class MainViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val title = v.title_text
        val duration = v.duration_text
        val flag = v.flag_image
        val used_money = v.used_money_text
        val background_image = v.background_image
    }

    inner class MainAdapter(val list: MutableList<DataTravel>): RecyclerView.Adapter<MainViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return MainViewHolder(layoutInflater.inflate(R.layout.item_main, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
            val data: DataTravel = list[position]

            val num = data.num
            val title = data.title
            val start_day = data.start_day
            val end_day = data.end_day
            val flag = data.flag
            val country = data.country
            val currency = data.currency
            val cover_image = data.cover_image
            val used_money_mycountry = data.used_money_mycountry

            val sdf = SimpleDateFormat("yyyy.MM.dd")
            val duration = "${sdf.format(start_day)} ~ ${sdf.format(end_day)}"

            holder.title.text = title
            holder.duration.text = duration
            holder.flag.setImageResource(flag!!)
            holder.used_money.text = "${App.pref.myCurrency} ${String.format("%,d", used_money_mycountry?.toInt())}"      // 나중에 실제 값으로 바꿔야 함

            val uri = Uri.parse(cover_image)
            // log
            Log.d("MainFragment", cover_image!!)

            holder.background_image.setImageURI(uri)

            holder.itemView.setOnClickListener() {
                val intent = Intent(activity?.applicationContext, ContentActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("title", title)

                startActivityForResult(intent, FROM_CONTENT)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            FROM_CONTENT -> {
                activity?.recreate()
            }
        }
    }

}


