package com.dongldh.travelpocket.profile_setting

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dongldh.travelpocket.R
import com.scrounger.countrycurrencypicker.library.Country
import com.scrounger.countrycurrencypicker.library.CountryCurrencyPicker
import com.scrounger.countrycurrencypicker.library.Currency
import com.scrounger.countrycurrencypicker.library.Listener.CountryCurrencyPickerListener
import com.scrounger.countrycurrencypicker.library.PickerType


class CountryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_country)

        title = "여행 국가"

        val pickerFragment = CountryCurrencyPicker.newInstance(PickerType.COUNTRY,
            object : CountryCurrencyPickerListener {
                override fun onSelectCountry(country: Country) {
                    if (country.currency == null) {
                        Toast.makeText(this@CountryActivity, String.format("name: %s\ncode: %s", country.name, country.code), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@CountryActivity, String.format("name: %s\ncurrencySymbol: %s", country.name, country.currency!!.symbol), Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onSelectCurrency(currency: Currency?) {

                }
            })

        supportFragmentManager.beginTransaction().replace(R.id.fragment, pickerFragment).commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.country_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
