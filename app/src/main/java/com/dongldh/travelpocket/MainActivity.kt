package com.dongldh.travelpocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
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

    }

    override fun onBackPressed() {

        // drawer가 시작 지점부터 밀려있다면 back 버튼을 눌렀을 때 drawer를 닫자
        if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(navigation)
        } else {
            // 뒤로 버튼 두 번 눌러 종료
            if(System.currentTimeMillis() - currentTime > 2000) {
                currentTime = System.currentTimeMillis()
                Toast.makeText(this, "'뒤로'버튼 한 번 더 누르면 종료", Toast.LENGTH_SHORT).show()
            } else {
                super.onBackPressed()
            }
        }


    }

}
