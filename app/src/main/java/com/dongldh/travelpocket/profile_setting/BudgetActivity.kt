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
import androidx.appcompat.app.AlertDialog
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
    var budget: Float = 0.0F
    var flag: Int = 0

    lateinit var myRetrofit: Retrofit
    var myRetrofitManager: RetrofitManager? = null
    var myCallMapList: Call<Map<String, Any>>? = null

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
        budget = intent.getFloatExtra("budget", 0.0F)
        flag = intent.getIntExtra("flag", 0)

        currency_button.text = "${currency} (${code})"
        unit_change_text.text = code
        unit_my_text.text = "${code}(자국 화폐)"

        budget_edit.setText(budget.toString())

        codeFromTo = "${App.pref.myCode}${code}"
        codeToFrom = "${code}${App.pref.myCode}"

        currency_button.setOnClickListener(this)
        exchange_rate_from_to_button.setOnClickListener(this)
        exchange_rate_to_from_button.setOnClickListener(this)
        back_text.setOnClickListener(this)

        setRetrofitInit()
        callRateList(codeFromTo!!, codeToFrom!!)
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
                    if(t is IOException) {
                        Toast.makeText(
                            this@BudgetActivity,
                            "서버와의 연결에 실패 했습니다. 환율이 1:1로 자동 설정됩니다. 서버 안정화 이후 '환율정보 업데이트'를 통해 정보를 얻으세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        // 이 상황에서 어떤 Exception이 들어가는지 서버 정상동작하면 확인해보기
                        Toast.makeText(
                            this@BudgetActivity,
                            "등록되지 않은 통화코드가 존재합니다. 환율이 1:1로 자동 설정됩니다.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    changeRateFromTo = 1.0
                    changeRateToFrom = 1.0
                }

                // enqueue -> 비동기방식. 따라서, 현재 실행되고 있는 코드 흐름이 끝나야 Callback 작동하기 때문에, 고려해서 코드 구성할 것
                override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                    val rateInfo: Map<String, Any>? = response.body()
                    if(rateInfo?.size == 1 || rateInfo.isNullOrEmpty()){
                        Toast.makeText(
                            this@BudgetActivity,
                            "등록되지 않은 통화코드가 존재합니다. 환율이 1:1로 자동 설정됩니다.",
                            Toast.LENGTH_LONG
                        ).show()
                        changeRateFromTo = 1.0
                        changeRateToFrom = 1.0
                    }

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
                    code = data.getStringExtra("code")
                    flag = data.getIntExtra("flag", 0)
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
                if(intent.getStringExtra("requestCode") == "EntireUpdate") {
                    if(currency != intent.getStringExtra("currency")) {

                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("경고")
                    builder.setMessage("화폐의 종류가 변경 되었습니다.\n진행을 누를 시 ${intent.getStringExtra("currency")}로 설정했던 모든 수입/지출 내역이 삭제됩니다.\n" +
                            "이를 원치 않으실 경우, 취소를 눌러주세요.")

                    builder.setNegativeButton("취소") { dialog, which ->
                        dialog.dismiss()
                    }

                    builder.setPositiveButton("진행"){ dialog, which ->
                        dialog.dismiss()
                        val selectedIntent = Intent()
                        selectedIntent.putExtra("currency", currency)
                        selectedIntent.putExtra("code", code)
                        selectedIntent.putExtra("budget", budget_edit.text.toString())
                        selectedIntent.putExtra("rate_fromto", changeRateFromTo.toString())
                        selectedIntent.putExtra("rate_tofrom", changeRateToFrom.toString())
                        selectedIntent.putExtra("position", intent.getIntExtra("position", -1))
                        selectedIntent.putExtra("flag", flag)
                        selectedIntent.putExtra("isCurrencyChanged", "true")
                        selectedIntent.putExtra("originalCurrency", intent.getStringExtra("currency"))
                        selectedIntent.putExtra("originalRate", intent.getDoubleExtra("originalRate", 1.0))
                        selectedIntent.putExtra("originalBudget", intent.getFloatExtra("budget", 0.0f))

                        setResult(Activity.RESULT_OK, selectedIntent)
                        finish()
                    }
                    val dialog = builder.create()
                    dialog.show()
                    } else {
                        val selectedIntent = Intent()
                        selectedIntent.putExtra("currency", currency)
                        selectedIntent.putExtra("code", code)
                        selectedIntent.putExtra("budget", budget_edit.text.toString())
                        selectedIntent.putExtra("rate_fromto", changeRateFromTo.toString())
                        selectedIntent.putExtra("rate_tofrom", changeRateToFrom.toString())
                        selectedIntent.putExtra("position", intent.getIntExtra("position", -1))
                        selectedIntent.putExtra("flag", flag)
                        selectedIntent.putExtra("isCurrencyChanged", "false")
                        selectedIntent.putExtra("originalCurrency", intent.getStringExtra("currency"))
                        selectedIntent.putExtra("originalRate", intent.getDoubleExtra("originalRate", 1.0))
                        selectedIntent.putExtra("originalBudget", intent.getFloatExtra("budget", 0.0f))

                        setResult(Activity.RESULT_OK, selectedIntent)
                        finish()
                    }
                } else {
                    val selectedIntent = Intent()
                    // 화폐정보 가져오기
                    selectedIntent.putExtra("currency", currency)
                    selectedIntent.putExtra("code", code)
                    selectedIntent.putExtra("budget", budget_edit.text.toString())
                    selectedIntent.putExtra("rate_fromto", changeRateFromTo.toString())
                    selectedIntent.putExtra("rate_tofrom", changeRateToFrom.toString())
                    selectedIntent.putExtra("position", intent.getIntExtra("position", -1))
                    selectedIntent.putExtra("flag", flag)

                    setResult(Activity.RESULT_OK, selectedIntent)
                    finish()
                }
            }

        }
        return super.onOptionsItemSelected(item)
    }


}

