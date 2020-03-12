package com.dongldh.travelpocket.profile_setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dongldh.travelpocket.App
import com.dongldh.travelpocket.R
import com.dongldh.travelpocket.SELECT_COUNTRY
import com.dongldh.travelpocket.SELECT_CURRENCY
import com.scrounger.countrycurrencypicker.library.Country
import com.scrounger.countrycurrencypicker.library.CountryCurrencyPicker
import com.scrounger.countrycurrencypicker.library.Currency
import com.scrounger.countrycurrencypicker.library.Listener.CountryCurrencyPickerListener
import com.scrounger.countrycurrencypicker.library.PickerType


class CountryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country)

        val requestCode = intent.getIntExtra("request", 10)
        var pickerFragment: CountryCurrencyPicker? = null


        when(requestCode) {
            SELECT_COUNTRY -> {
                title = "여행 국가"

                // 나라만 보이게 할 수 없나?
                pickerFragment = CountryCurrencyPicker.newInstance(PickerType.COUNTRYandCURRENCY,
                    object : CountryCurrencyPickerListener {
                        override fun onSelectCountry(country: Country) {
                            val selectedIntent = Intent()
                            /*if (country.currency == null) {
                                Toast.makeText(this@CountryActivity, String.format("name: %s\ncode: %s", country.name, country.code), Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@CountryActivity, String.format("name: %s\ncurrencySymbol: %s", country.name, country.currency!!.symbol), Toast.LENGTH_SHORT).show()
                            }*/
                            selectedIntent.putExtra("flag", country.flagId)
                            selectedIntent.putExtra("country", country.name)
                            if(country.currency == null) {
                                Toast.makeText(this@CountryActivity, "화폐 단위가 없는 국가입니다. 화폐 단위는 변경되지 않습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                selectedIntent.putExtra("currency", country.currency?.symbol)
                            }
                            setResult(Activity.RESULT_OK, selectedIntent)
                            finish()
                        }
                        override fun onSelectCurrency(currency: Currency?) {

                        }
                    })
            }
            SELECT_CURRENCY -> {
                title = "자국 화폐"

                pickerFragment = CountryCurrencyPicker.newInstance(PickerType.COUNTRYandCURRENCY,
                    object : CountryCurrencyPickerListener {
                        override fun onSelectCountry(country: Country) {
                            App.pref.myCountry = country.name
                            App.pref.myFlagId = country.flagId
                            if(country.currency == null) {
                                Toast.makeText(this@CountryActivity, "화폐 단위가 없는 국가입니다. 화폐 단위는 변경되지 않습니다.", Toast.LENGTH_SHORT).show()
                            } else {
                                App.pref.myCurrency = country.currency?.symbol
                                App.pref.myCode = country.currency?.code
                            }
                            finish()
                        }
                        override fun onSelectCurrency(currency: Currency?) {
                        }
                    })
            }

            SELECT_MONEY_TYPE -> {
                title = "통화(화폐)"
                pickerFragment = CountryCurrencyPicker.newInstance(PickerType.COUNTRYandCURRENCY,
                    object : CountryCurrencyPickerListener {
                        override fun onSelectCountry(country: Country) {
                            val selectedIntent = Intent()
                            // log
                            //Log.d("CountryAc: Currency", country.currency?.symbol!!)
                            //Log.d("CountryAc: Name", country.currency?.name!!)

                            selectedIntent.putExtra("currency", country.currency?.symbol)
                            selectedIntent.putExtra("code", country.currency?.code)
                            setResult(Activity.RESULT_OK, selectedIntent)
                            finish()
                        }
                        override fun onSelectCurrency(currency: Currency?) {
                        }
                    })
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment, pickerFragment!!).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.country_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
