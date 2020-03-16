package com.dongldh.travelpocket.content

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    var num = 0
    var datecode = ""
    val helper = DBHelper(this)

    // 날짜 정보를 담는 리스트(1 ~ 31일 까지의 여행이었다면 1,2,3...31일까지의 날짜정보를 Long으로 담은 리스트)
    var list: MutableList<Long> = mutableListOf()
    // 날짜 및 구매 내역을 list로 보여줌. (moneyused, typeused)
    var list_detail: MutableList<DetailType> = mutableListOf()
    // DataMoney(currency, budget).. 가진 통화의 합을 자국 돈으로 바꾼 것이 0번 인덱스, 가진 통화 각각의 정보를 1번 인덱스로 둠
    var list_money_info: MutableList<DataMoney> = mutableListOf()
    var list_money_info_index = 0

    var selected_day: Long? = null

    // seeMoneyInfo 관련 클래스변수
    var currencyList: MutableList<String> = mutableListOf()
    var nowSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        selectContentDayDB()
        fab.setOnClickListener(this)
        money_info_layout.setOnClickListener(this)
    }

    fun selectContentDayDB() {
        list = mutableListOf()
        val db = helper.writableDatabase

        num = intent.getIntExtra("num", 0)
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

    class ContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_day = view.content_day
        val content_month = view.content_month
    }

    class DateViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_day_text = view.content_day_text
    }

    class DetailViewHolder(view: View): RecyclerView.ViewHolder(view) {
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
                selected_day = item
                selectDetailDB(item)
                makeMoneyCard()
            }
        }

    }

    fun selectDetailDB(item: Long) {
        list_detail = mutableListOf()
        list_detail.add(DataDay(selected_day))
        val db = helper.writableDatabase
        datecode = SimpleDateFormat("yyMMdd").format(item)
        val isExistSQL = db.rawQuery("select count(*) from t_content where num=? and datecode=?",
            arrayOf(num.toString(), datecode))
        isExistSQL.moveToNext()
        val isExist = isExistSQL.getInt(0)
        if(isExist != 0){
            val cursor = db.rawQuery("select type, currency, moneyUsed from t_content where num=? and datecode=?",
                arrayOf(num.toString(), datecode))

            while(cursor.moveToNext()) {
                val type = cursor.getString(0)
                val currency: String = cursor.getString(1)
                val moneyUsed = cursor.getDouble(2)

                list_detail.add(DataDetail(moneyUsed, currency, type))
                Log.d("ContentActivity", list_detail[1].type.toString())
            }
        }

        recycler_content.layoutManager = LinearLayoutManager(this)
        recycler_content.adapter = DetailAdapter(list_detail)
    }


    inner class DetailAdapter(val list: MutableList<DetailType>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemViewType(position: Int): Int {
            return list[position].type
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
           when{
                viewType == DetailType.DAY_TYPE -> {
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
            when(itemType.type){
                DetailType.DAY_TYPE -> {
                    val viewHolder = holder as DateViewHolder
                    val item = itemType as DataDay

                    viewHolder.content_day_text.text = SimpleDateFormat("yyyy년 MM월 dd일").format(item.day)
                }
                DetailType.DETAIL_TYPE -> {
                    val viewHolder = holder as DetailViewHolder
                    val item = itemType as DataDetail

                    viewHolder.content_detail_text.text = "${item.currency} ${item.moneyUsed?.toInt().toString()}"
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


    // DataMoney 정보를 리스트에 담기. (currency, 그에 해당하는 돈.) (자국 머니 총 합은 0번 index에 존재하게끔 설정)
    fun makeMoneyCard() {
        // num 정해져있음.
        // datecode도 정해짐.
        currencyList = mutableListOf()

        val db = helper.writableDatabase
        val cursorCurrency =
            db.rawQuery("select currency from t_budget where num=?", arrayOf(num.toString()))
        while (cursorCurrency.moveToNext()) {
            currencyList.add(cursorCurrency.getString(0))
        }
        db.close()

        seeMoneyInfo()
    }

    
    // money_info_layout 의 값을 계속 바꿔주는 역할
    fun seeMoneyInfo() {
        var firstMoney = 0.0
        var usedMoney = 0.0  // 첫 번째 카드
        var remainMoney = 0.0 // 두 번째 카드
        var currency: String? = null

        val db = helper.writableDatabase

        when(nowSelected) {
            // 초기화 값과 동일. 전체 내역
            0 -> {
                // 각 화폐마다 사용한 돈이 얼마인지를 자국화폐 단위로 환산하여 나타낸 리스트
                var usedMoneyListTotal = mutableListOf<Double>()

                for(i in currencyList) {
                    var usedMoneyEach = 0.0
                    var usedMoneyList = mutableListOf<Double>()

                    val cursorBudget = db.rawQuery("select rate_tofrom from t_budget where num=? and currency=?", arrayOf(num.toString(), i))
                    cursorBudget.moveToNext()

                    val cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and datecode=? and currency=?",
                        arrayOf(num.toString(), datecode, i))

                    while(cursorByCurrency.moveToNext()) {
                        // 돈 사용인 경우
                        if(cursorByCurrency.getInt(1) == 0){
                            usedMoneyList.add(cursorByCurrency.getDouble(0))
                        } else {
                            //돈 추가인 경우
                            usedMoneyList.add(cursorByCurrency.getDouble(0) * (-1))
                        }
                    }

                    for(i in usedMoneyList) {
                        usedMoneyEach += i
                    }
                    usedMoneyListTotal.add(usedMoneyEach * cursorBudget.getDouble(0))
                }

                val cursorTravelTotal = db.rawQuery("select total_money_mycountry from t_travel where num=?", arrayOf(num.toString()))
                cursorTravelTotal.moveToNext()
                firstMoney = cursorTravelTotal.getDouble(0)
                remainMoney = firstMoney - usedMoney

                used_money_title_text.text = "쓴 돈(자국 화폐)"
                remain_money_title_text.text = "남은 돈(자국 화폐)"
                used_money_text.text = "${App.pref.myCurrency} ${usedMoney?.toInt()}"
                remain_money_text.text = "${App.pref.myCurrency} ${remainMoney?.toInt()}"
            }
            else -> {
                var usedMoneyList = mutableListOf<Double>()
                val db = helper.writableDatabase
                currency = currencyList[nowSelected - 1]

                val cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and datecode=? and currency=?",
                    arrayOf(num.toString(), datecode, currency))

                while(cursorByCurrency.moveToNext()) {
                    // 돈 사용인 경우
                    if(cursorByCurrency.getInt(1) == 0){
                        usedMoneyList.add(cursorByCurrency.getDouble(0))
                    } else {
                        //돈 추가인 경우
                        usedMoneyList.add(cursorByCurrency.getDouble(0) * (-1))
                    }
                }

                for(i in usedMoneyList) {
                    usedMoney += i
                }

                // firstMoney 얻어내고, remainMoney 까지 정해주기
                val cursorBudget = db.rawQuery("select money from t_budget where num=? and currency=?", arrayOf(num.toString(), currency))
                cursorBudget.moveToNext()
                firstMoney = cursorBudget.getDouble(0)
                remainMoney = firstMoney - usedMoney

                used_money_title_text.text = "쓴 돈"
                remain_money_title_text.text = "남은 돈"
                used_money_text.text = "${currency} ${usedMoney?.toInt()}"
                remain_money_text.text = "${currency} ${remainMoney?.toInt()}"
            }
        }
        db.close()
    }

    override fun onClick(v: View?) {
        when(v) {
            fab -> {
                val intent = Intent(this, UsageActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("selected_day", selected_day)
                startActivity(intent)
            }

            money_info_layout -> {
                when(nowSelected) {
                    currencyList.size -> {
                        nowSelected = 0
                        seeMoneyInfo()
                    }
                    else -> {
                        nowSelected++
                        seeMoneyInfo()
                    }
                }
            }

        }
    }



}
