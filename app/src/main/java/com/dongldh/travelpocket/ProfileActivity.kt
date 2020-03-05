package com.dongldh.travelpocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = "여행 프로필"

        Picasso.get()
            .load(R.drawable.image_cover)
            .fit()
            .centerCrop()
            .into(cover_image)

    }
}
