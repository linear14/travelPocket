package com.dongldh.travelpocket.profile_setting

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.R
import com.dongldh.travelpocket.RetrofitManager
import com.dongldh.travelpocket.SELECT_CURRENCY
import kotlinx.android.synthetic.main.activity_budget.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.concurrent.thread

val SELECT_MONEY_TYPE = 50

class BudgetActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var currency: String
    lateinit var code: String

    lateinit var myRetrofit: Retrofit
    var myRetrofitManager: RetrofitManager? = null
    var myCallMapList: Call<Map<String, Any>>? = null

    private var mHandler: Handler? = null

    var codeFromTo: String? = null
    var codeToFrom: String? = null
    var changeRateFromTo: Double = 1.0
    var changeRateToFrom: Double = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)

        title = "에산/화폐"
        // 화폐 단위가 없는 국가 선택 시 처리 (currency가 null일 때)
        // Caused by: java.lang.IllegalStateException: intent.getStringExtra("currency") must not be null
        currency = intent.getStringExtra("currency")
        code = intent.getStringExtra("code")

        currency_button.text = "${currency} (${code})"
        unit_change_text.text = code
        unit_my_text.text = "${code}(자국 화폐)"

        currency_button.setOnClickListener(this)
        exchange_rate_from_to_button.setOnClickListener(this)
        exchange_rate_to_from_button.setOnClickListener(this)
        back_text.setOnClickListener(this)

        setRetrofitInit()
    }

    fun setRetrofitInit() {
        myRetrofit = Retrofit.Builder()
            .baseUrl("https://earthquake.kr:23490/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        myRetrofitManager = myRetrofit.create(RetrofitManager::class.java)
    }

    fun callRateList(fromto: String, tofrom: String) {

            myCallMapList = myRetrofitManager?.getRateList(fromto, tofrom)
            myCallMapList?.enqueue(object : Callback<Map<String, Any>> {
                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        this@BudgetActivity,
                        "등록되지 않은 통화코드가 존재합니다. 환율이 1:1로 자동 설정됩니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    changeRateFromTo = 1.0
                    changeRateToFrom = 1.0
                }

                // enqueue -> 비동기방식. 따라서, 현재 실행되고 있는 코드 흐름이 끝나야 Callback 작동하기 때문에, 고려해서 코드 구성할 것
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    val rateInfo: Map<String, Any>? = response.body()
                    for (mapKey in rateInfo!!.keys) {
                        if (!mapKey.equals("update")) {
                            val mapValue = rateInfo.get(mapKey) as List<Double>
                            if(mapKey.equals(fromto)) {
                                changeRateFromTo = mapValue[0]
                            } else {
                                changeRateToFrom = mapValue[0]
                            }
                        }
                    }
                }
            })
    }


    override fun onClick(v: View?) {
        when (v) {
            currency_button -> {
                val intent = Intent(this, CountryActivity::class.java)
                intent.putExtra("request", SELECT_MONEY_TYPE)
                startActivityForResult(intent, SELECT_MONEY_TYPE)
            }

            unit_change_text -> {
                // 화폐 정보로 이동 (intent) (currency_button)과 같은..
            }

            // 서버에서 환율 받기 (자국 통화 고정)
            exchange_rate_from_to_button -> {
                if(from_edit.text.isNullOrEmpty()) {
                    Toast.makeText(applicationContext, "자국 통화량을 입력하세요", Toast.LENGTH_SHORT).show()
                } else {
                    to_edit.setText(String.format("%.2f", from_edit.text.toString().toDouble().times(changeRateFromTo)))
                }

            }

            // 서버에서 환율 받기 (여행지 통화 고정)
            exchange_rate_to_from_button -> {
                if(to_edit.text.isNullOrEmpty()) {
                    Toast.makeText(applicationContext, "여행지 통화량을 입력하세요", Toast.LENGTH_SHORT).show()
                } else {
                    from_edit.setText(String.format("%.2f", to_edit.text.toString().toDouble().times(changeRateToFrom)))
                }
            }

            back_text -> {
                finish()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SELECT_MONEY_TYPE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // CountryActivity에서 넘어온 intent
                    currency = data!!.getStringExtra("currency")!!
                    code = data!!.getStringExtra("code")
                    currency_button.text = "${currency} (${code})"
                    unit_change_text.text = code

                    codeFromTo = "${App.pref.myCode}${code}"
                    codeToFrom = "${code}${App.pref.myCode}"
                    callRateList(codeFromTo!!, codeToFrom!!)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.budget_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                val selectedIntent = Intent()
                // 화폐정보 가져오기
                selectedIntent.putExtra("currency", currency)
                selectedIntent.putExtra("code", code)
                selectedIntent.putExtra("budget", budget_edit.text.toString())
                selectedIntent.putExtra("rate_fromto", changeRateFromTo.toString())
                selectedIntent.putExtra("rate_tofrom", changeRateToFrom.toString())

                Log.d("Budget", changeRateFromTo.toString())
                Log.d("Budget", changeRateToFrom.toString())

                setResult(Activity.RESULT_OK, selectedIntent)
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }


}

