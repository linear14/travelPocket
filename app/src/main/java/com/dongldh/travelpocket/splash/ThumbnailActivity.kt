package com.dongldh.travelpocket.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dongldh.travelpocket.MainActivity

class ThumbnailActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Thread.sleep(3000)
        } catch(e: Exception){
            e.printStackTrace()
        }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
