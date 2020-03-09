package com.dongldh.travelpocket.profile_setting

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.dongldh.travelpocket.R
import com.dongldh.travelpocket.SELECT_CURRENCY
import kotlinx.android.synthetic.main.activity_budget.*
val SELECT_MONEY_TYPE = 50
class BudgetActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var currency: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget)
        
        title = "에산/화폐"
        // 화폐 단위가 없는 국가 선택 시 처리 (currency가 null일 때)
        // Caused by: java.lang.IllegalStateException: intent.getStringExtra("currency") must not be null
        currency = intent.getStringExtra("currency")

        currency_button.text = currency
        currency_button.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v) {
            currency_button -> {
                val intent = Intent(this, CountryActivity::class.java)
                intent.putExtra("request", SELECT_MONEY_TYPE)
                startActivityForResult(intent, SELECT_MONEY_TYPE)
            }

            unit_change_text -> {
                // 화폐 정보로 이동 (intent) (currency_button)과 같은..
            }

            exchange_rate_server_button -> {
                // 서버에서 환율 받기

            }

            back_text -> {
                finish()
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            SELECT_MONEY_TYPE -> {
                if(resultCode == Activity.RESULT_OK) {
                    // CountryActivity에서 넘어온 intent
                    currency = data!!.getStringExtra("currency")!!
                    currency_button.text = "${currency} (${data!!.getStringExtra("code")})"
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.budget_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_save -> {
                val selectedIntent = Intent()
                // 화폐정보 가져오기
                selectedIntent.putExtra("currency", currency)
                selectedIntent.putExtra("budget", budget_edit.text.toString())
                setResult(Activity.RESULT_OK, selectedIntent)
                finish()
            }

        }
        return super.onOptionsItemSelected(item)
    }


}
