package com.dongldh.travelpocket.content

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.ContentView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.*
import kotlinx.android.synthetic.main.activity_content.*
import kotlinx.android.synthetic.main.activity_content.view.*
import kotlinx.android.synthetic.main.activity_usage.*
import kotlinx.android.synthetic.main.item_content.*
import kotlinx.android.synthetic.main.item_content.view.*
import kotlinx.android.synthetic.main.item_content_day.view.*
import kotlinx.android.synthetic.main.item_content_detail.view.*
import java.text.SimpleDateFormat
import java.util.*

val FROM_USAGE = 111
val FROM_PROFILE_EDIT = 112
class ContentActivity : AppCompatActivity(), View.OnClickListener {
    var num = 0
    var datecode = "" // 0: all, 1: prepare
    val helper = DBHelper(this)

    var today: String = ""
    var selected_index = -1
    lateinit var textColor: ColorStateList
    var allOrPreState = false
    var isFirst = true

    // 날짜 정보를 담는 리스트(1 ~ 31일 까지의 여행이었다면 1,2,3...31일까지의 날짜정보를 Long으로 담은 리스트)
    var list: MutableList<String> = mutableListOf()
    // 날짜 및 구매 내역을 list로 보여줌. (moneyused, typeused)
    var list_detail: MutableList<DetailType> = mutableListOf()

    // 0: all, 1: prepare, 나머지: datecode
    var selected_day: Long = 0

    // seeMoneyInfo 관련 클래스변수
    var currencyList: MutableList<String> = mutableListOf()
    var nowSelected = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)

        init()

        all_card.setOnClickListener(this)
        pre_card.setOnClickListener(this)
        fab.setOnClickListener(this)
        money_info_layout.setOnClickListener(this)
    }

    fun init() {
        // SharedPreference 설정정보
        if(App.pref.myPrepare == "보여주기") {
            pre_card.visibility = View.VISIBLE
        } else {
            pre_card.visibility = View.GONE
        }

        textColor = all_card_sign.textColors

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 1)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        today = SimpleDateFormat("yyMMdd").format(cal.timeInMillis)

        title = intent.getStringExtra("title")
        selected_day = 0L
        datecode = "0"
        selectContentDayDB()
        selectDetailDB(selected_day)
        makeMoneyCard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.content_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_travel_setting -> {
                val intent = Intent(this@ContentActivity, ProfileActivity::class.java)
                intent.putExtra("requestCode", "ContentActivity")
                intent.putExtra("num", num)
                startActivityForResult(intent, FROM_PROFILE_EDIT)
            }

            R.id.action_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("이 여행 삭제하기")
                    .setMessage("정말 '${intent.getStringExtra("title")}'을(를) 삭제하시겠습니까?")

                builder.setPositiveButton("확인"){ dialog, which ->
                    val db = helper.writableDatabase
                    db.delete("t_travel","num=?",arrayOf(num.toString()))
                    db.delete("t_content","num=?",arrayOf(num.toString()))
                    db.delete("t_budget","num=?",arrayOf(num.toString()))

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
        }
        return super.onOptionsItemSelected(item)
    }

    fun selectContentDayDB() {
        list = mutableListOf()
        val db = helper.writableDatabase

        num = intent.getIntExtra("num", 0)
        val cursor = db.rawQuery("select start_day, end_day from t_travel where num=?", arrayOf(num.toString()))

        cursor.moveToNext()
        val start = cursor.getLong(0)
        val end = cursor.getLong(1)

        // 오류가 생기는 부분 : start가 1시간이 더 붙어버림 (3600000).
        for(i in start .. end step 24*60*60*1000) {
            // long을 월과 일로 만들기
            list.add(SimpleDateFormat("yyMMdd").format(i))
        }

        if(isFirst){
            if(today in list) {
                selected_index = list.indexOf(today)
                selected_day = SimpleDateFormat("yyMMdd").parse(today).time
                fab.visibility = View.VISIBLE
            } else {
                all_card_sign.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                all_card_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                fab.visibility = View.GONE
            }
            isFirst = false
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.scrollToPosition(selected_index)
        recycler.adapter = ContentAdapter(list)
    }



    class ContentViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val content_layout = view.content_layout
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

    inner class ContentAdapter(val list: MutableList<String>): RecyclerView.Adapter<ContentViewHolder>() {
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
            holder.content_day.text = SimpleDateFormat("d").format(SimpleDateFormat("yyMMdd").parse(item)).toString()
            holder.content_month.text = "${SimpleDateFormat("M").format(SimpleDateFormat("yyMMdd").parse(item))}월"

            holder.itemView.setOnClickListener {
                // 날짜정보 들어간 리사이클러뷰 만들기
                // 아이템의 시간정보 보내주기
                allOrPreState = false
                selected_day = SimpleDateFormat("yyMMdd").parse(item).time
                selected_index = position
                fab.visibility = View.VISIBLE
                selectDetailDB(SimpleDateFormat("yyMMdd").parse(item).time)
                makeMoneyCard()
                all_card_sign.setTextColor(textColor)
                all_card_text.setTextColor(textColor)
                pre_card_sign.setTextColor(textColor)
                pre_card_text.setTextColor(textColor)
                notifyDataSetChanged()
            }

            if(!allOrPreState) {
                if(selected_index == position){
                    holder.content_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                } else {
                    holder.content_layout.setBackgroundColor(ContextCompat.getColor(applicationContext, android.R.color.white))
                }
            }
        }

    }

    fun selectDetailDB(item: Long) {
        list_detail = mutableListOf()

        if(item != 0L && item != 1L) {
            datecode = SimpleDateFormat("yyMMdd").format(item)
        }

        if(item != 0L) {
            list_detail.add(DataDay(item))
        }

        val db = helper.writableDatabase

        if(item == 0L) {
            // 전체에 대한 조회
            var dateList = mutableListOf<String>()

            val isExistSQL = db.rawQuery(
                "select count(*) from t_content where num=?",
                arrayOf(num.toString())
            )
            isExistSQL.moveToNext()
            val isExist = isExistSQL.getInt(0)
            if (isExist != 0) {
                val cursorDate = db.rawQuery("select distinct datecode from t_content where num=? order by datecode", arrayOf(num.toString()))
                while(cursorDate.moveToNext()) {
                    dateList.add(cursorDate.getString(0))
                }

                // i가 Long 이어야 함.. datecode로 바뀌기 전의 시간 값.
                for(i in dateList) {
                    if(i.equals("1")) {
                        list_detail.add(DataDay(1))
                    } else {
                        val dateMill = SimpleDateFormat("yyMMdd").parse(i)
                        val cal = Calendar.getInstance()
                        cal.time = dateMill
                        list_detail.add(DataDay(cal.timeInMillis))
                    }

                    val cursor = db.rawQuery(
                        "select type, currency, moneyUsed, used, itemNumber from t_content where num=? and datecode=?",
                        arrayOf(num.toString(), i)
                    )

                    while (cursor.moveToNext()) {
                        val type = cursor.getString(0)
                        val currency: String = cursor.getString(1)
                        val moneyUsed = cursor.getDouble(2)
                        val used = cursor.getString(3)?:""
                        val itemNumber = cursor.getInt(4)

                        list_detail.add(DataDetail(moneyUsed, currency, type, used, itemNumber))
                    }
                }
            }
        } else {
            val isExistSQL = db.rawQuery(
                "select count(*) from t_content where num=? and datecode=?",
                arrayOf(num.toString(), datecode)
            )
            isExistSQL.moveToNext()
            val isExist = isExistSQL.getInt(0)
            if (isExist != 0) {
                val cursor = db.rawQuery(
                    "select type, currency, moneyUsed, used, itemNumber from t_content where num=? and datecode=?",
                    arrayOf(num.toString(), datecode)
                )

                while (cursor.moveToNext()) {
                    val type = cursor.getString(0)
                    val currency: String = cursor.getString(1)
                    val moneyUsed = cursor.getDouble(2)
                    val used = cursor.getString(3)?:""
                    val itemNumber = cursor.getInt(4)

                    list_detail.add(DataDetail(moneyUsed, currency, type, used, itemNumber))
                }
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

                    when(item.day) {
                        0L -> {

                        }
                        1L -> {
                            viewHolder.content_day_text.text = "Prepare"
                        }
                        else -> {
                            viewHolder.content_day_text.text = SimpleDateFormat("yyyy년 MM월 dd일").format(item.day)
                        }
                    }
                }
                DetailType.DETAIL_TYPE -> {
                    val viewHolder = holder as DetailViewHolder
                    val item = itemType as DataDetail

                    viewHolder.content_detail_text.text = "${item.currency} ${String.format("%,d", item.moneyUsed?.toInt())}"
                    if(item.used.equals("")) {
                        viewHolder.content_type_text.text = item.type_used
                    } else {
                        viewHolder.content_type_text.text = "${item.type_used} : ${item.used}"
                    }
                    when(item.type_used) {
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

                    viewHolder.itemView.setOnClickListener {
                        val intent = Intent(this@ContentActivity, DetailActivity::class.java)
                        intent.putExtra("itemNumber", item.itemNumber)
                        startActivity(intent)
                    }

                    viewHolder.content_detail_text.setOnClickListener {
                        val currency = viewHolder.content_detail_text.text.split(" ")[0]
                        if(currency.equals(item.currency)){
                            val helper = DBHelper(this@ContentActivity)
                            val db = helper.writableDatabase
                            val cursor = db.rawQuery("select rate_tofrom from t_budget where num=? and currency=?", arrayOf(num.toString(), currency))
                            cursor.moveToNext()
                            viewHolder.content_detail_text.text = "${App.pref.myCurrency} ${String.format("%,d", (item.moneyUsed!! * cursor.getDouble(0)).toInt())}"
                        } else {
                            viewHolder.content_detail_text.text = "${item.currency} ${String.format("%,d", item.moneyUsed?.toInt())}"
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

        // 화폐 별 사용한 금액 리스트(300달러, 200엔 .. 이런식으로)
        var usedMoneyListAll = mutableListOf<Double>()
        for(i in currencyList) {
            var totalSpentMoney = 0.0
            var usedMoneyList = mutableListOf<Double>()
            val cursorByCurrencyAll = db.rawQuery(
                "select moneyUsed, isPlus from t_content where num=? and currency=?",
                arrayOf(num.toString(), i)
            )
            while(cursorByCurrencyAll.moveToNext()) {
                if(cursorByCurrencyAll.getInt(1) == 0){
                    usedMoneyList.add(cursorByCurrencyAll.getDouble(0))
                } else {
                    //돈 추가인 경우
                    usedMoneyList.add(cursorByCurrencyAll.getDouble(0) * (-1))
                }
            }
            for(i in usedMoneyList) {
                totalSpentMoney += i
            }
            usedMoneyListAll.add(totalSpentMoney)
        }

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

                    var cursorByCurrency: Cursor? = null
                    if(datecode.equals("0")) {
                        cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and currency=?",
                            arrayOf(num.toString(), i))
                    } else {
                        cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and datecode=? and currency=?",
                            arrayOf(num.toString(), datecode, i))
                    }

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
                for(i in usedMoneyListTotal) {
                    usedMoney += i
                }
                var usedMoneyTotal = 0.0
                for(i in usedMoneyListAll) {
                    usedMoneyTotal += i
                }
                remainMoney = firstMoney - usedMoneyTotal

                used_money_title_text.text = "쓴 돈(자국 화폐)"
                remain_money_title_text.text = "남은 돈(자국 화폐)"
                used_money_text.text = "${App.pref.myCurrency} ${String.format("%,d", usedMoney?.toInt())}"
                remain_money_text.text = "${App.pref.myCurrency} ${String.format("%,d", remainMoney?.toInt())}"
            }
            else -> {
                var usedMoneyList = mutableListOf<Double>()
                val db = helper.writableDatabase
                currency = currencyList[nowSelected - 1]

                var cursorByCurrency: Cursor? = null
                if(datecode.equals("0")) {
                    cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and currency=?",
                        arrayOf(num.toString(), currency))
                } else {
                    cursorByCurrency = db.rawQuery("select moneyUsed, isPlus from t_content where num=? and datecode=? and currency=?",
                        arrayOf(num.toString(), datecode, currency))
                }

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
                var usedMoneyTotal = usedMoneyListAll[nowSelected - 1]

                // firstMoney 얻어내고, remainMoney 까지 정해주기
                val cursorBudget = db.rawQuery("select money from t_budget where num=? and currency=?", arrayOf(num.toString(), currency))
                cursorBudget.moveToNext()
                firstMoney = cursorBudget.getDouble(0)
                remainMoney = firstMoney - usedMoneyTotal

                used_money_title_text.text = "쓴 돈"
                remain_money_title_text.text = "남은 돈"
                used_money_text.text = "${currency} ${String.format("%,d", usedMoney?.toInt())}"
                remain_money_text.text = "${currency} ${String.format("%,d", remainMoney?.toInt())}"
            }
        }
        db.close()
    }

    // money_info_layout을 클릭했을 때, 어떤 seeMoneyInfo를 호출할 것인지에 대한 함수
    fun decideMoneyLayout(nowSelected: Int) {
        when(nowSelected) {
            currencyList.size -> {
                this.nowSelected = 0
                seeMoneyInfo()
            }
            else -> {
                this.nowSelected++
                seeMoneyInfo()
            }
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            all_card -> {
                all_card_sign.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                all_card_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                pre_card_sign.setTextColor(textColor)
                pre_card_text.setTextColor(textColor)
                selected_day = 0L
                selected_index = -1
                allOrPreState = true
                datecode = "0"
                fab.visibility = View.GONE
                selectContentDayDB()
                selectDetailDB(selected_day)
                makeMoneyCard()
            }

            pre_card -> {
                pre_card_sign.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                pre_card_text.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
                all_card_sign.setTextColor(textColor)
                all_card_text.setTextColor(textColor)
                selected_day = 1L
                selected_index = -1
                allOrPreState = true
                datecode = "1"
                fab.visibility = View.VISIBLE
                selectContentDayDB()
                selectDetailDB(selected_day)
                makeMoneyCard()
            }

            fab -> {
                val intent = Intent(this, UsageActivity::class.java)
                intent.putExtra("num", num)
                intent.putExtra("selected_day", selected_day)
                startActivityForResult(intent, FROM_USAGE)
            }

            money_info_layout -> {
                decideMoneyLayout(nowSelected)
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            FROM_USAGE -> {
                selectDetailDB(selected_day)
                decideMoneyLayout(nowSelected)
            }

            FROM_PROFILE_EDIT -> {
                recreate()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}