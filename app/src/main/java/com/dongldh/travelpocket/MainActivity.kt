package com.dongldh.travelpocket

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var drawerToggle: ActionBarDrawerToggle
    var currentTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ToolBar에 햄버거 표시 설정.
        // https://androidhuman.tistory.com/524
        // 며칠만에 드디어~~~!
        // 4, 5번째 인자는 접근성지원용 스트링(strings.xml 에서 설정)
        drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,0,0)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        fab.setOnClickListener(this)
        add_travel_button.setOnClickListener(this)
        close_textview.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v) {
            fab -> {
                home_layout.visibility = View.GONE
                add_travel_layout.visibility = View.VISIBLE
            }

            add_travel_button -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            }

            close_textview -> {
                home_layout.visibility = View.VISIBLE
                add_travel_layout.visibility = View.GONE
            }

        }
    }

    override fun onBackPressed() {
        if(add_travel_layout.visibility == View.VISIBLE) {
            home_layout.visibility = View.VISIBLE
            add_travel_layout.visibility = View.GONE
        } else if(drawer.isDrawerOpen(GravityCompat.START)) {
            // drawer가 시작 지점부터 밀려있다면 back 버튼을 눌렀을 때 drawer를 닫자
            drawer.closeDrawer(navigation)
            return
        } else if(System.currentTimeMillis() - currentTime > 2000) {
            currentTime = System.currentTimeMillis()
            Toast.makeText(this, "'뒤로'버튼 한 번 더 누르면 종료", Toast.LENGTH_SHORT).show()
        } else {
            super.onBackPressed()
        }

    }



}
