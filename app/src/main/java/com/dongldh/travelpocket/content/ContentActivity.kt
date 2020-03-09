package com.dongldh.travelpocket.content

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.dongldh.travelpocket.R

class ContentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_content)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.content_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
