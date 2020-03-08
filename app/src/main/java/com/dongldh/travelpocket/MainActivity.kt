package com.dongldh.travelpocket

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.dongldh.travelpocket.fragment.IntroFragment
import com.dongldh.travelpocket.fragment.MainFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var drawerToggle: ActionBarDrawerToggle
    var currentTime: Long = 0

    val FROM_PROFILE = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // deleteDatabase("travelDB")

        val permissionListener: PermissionListener = object: PermissionListener {
            override fun onPermissionGranted() {
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                Toast.makeText(this@MainActivity, "[설정] -> [권한] 에서 카메라 사용 권한, 앨범 사용 권한을 허용하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setRationaleMessage("카메라 및 앨범 접근 권한을 허용 하셔야 모든 기능을 이용할 수 있어요.")
            .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있어요.")
            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()

        // ToolBar에 햄버거 표시 설정.
        // https://androidhuman.tistory.com/524
        // 며칠만에 드디어~~~!
        // 4, 5번째 인자는 접근성지원용 스트링(strings.xml 에서 설정)
        drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar,0,0)
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        initMainActivity()
        fab.setOnClickListener(this)
        add_travel_button.setOnClickListener(this)
        close_textview.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v) {
            fab -> {
                home_layout.visibility = View.GONE
                fab.visibility = View.GONE
                add_travel_layout.visibility = View.VISIBLE
            }

            add_travel_button -> {
                if(travel_title_edit.text.isNullOrEmpty()) {
                    Toast.makeText(this, "여행 제목을 입력해 주세요", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("title", travel_title_edit.text.toString())
                    startActivityForResult(intent, FROM_PROFILE)
                }
            }

            close_textview -> {
                home_layout.visibility = View.VISIBLE
                fab.visibility = View.VISIBLE
                add_travel_layout.visibility = View.GONE
            }

        }
    }

    override fun onBackPressed() {
        if(add_travel_layout.visibility == View.VISIBLE) {
            home_layout.visibility = View.VISIBLE
            fab.visibility = View.VISIBLE
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            // ProfileActivity에서 정보 저장 후 확인 버튼을 눌렀다면 MainActivity를 recreate()하자
            FROM_PROFILE -> {
                recreate()
            }
        }
    }

    fun initMainActivity() {
        val helper = DBHelper(this)
        val db = helper.writableDatabase
        val cursor = db.rawQuery("select count(*) from t_travel", null)

        cursor.moveToNext()
        val count = cursor.getInt(0)

        when(count) {
            0 -> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, IntroFragment()).commit()
            }

            else-> {
                supportFragmentManager.beginTransaction().replace(R.id.main_content, MainFragment()).commit()
            }
        }

        db.close()
    }

}
