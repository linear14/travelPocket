package com.dongldh.travelpocket

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import com.dongldh.travelpocket.profile_setting.CountryActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*


class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    // 클래스의 멤버변수로 선언한 것 들은 나중에 정보 보낼 때 사용 됨
    val cal_start: Calendar = Calendar.getInstance()
    val cal_end: Calendar = Calendar.getInstance()

    var title: String? = null
    var country = "대한민국"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 앱바 제목
        setTitle("여행 프로필")

        // 여행 제목 설정
        title = intent.getStringExtra("title")
        profile_title_edit.setText(title)

        // 날짜 설정
        // 클릭하면 setOnClickListener에 의해 DatePickerDialog를 불러온다. 여기서 현재 저장 되어있는 날짜가 지정된 상태로 열리도록 함. (get)
        // 캘린더를 종료할 시 등록된 OnDateSetListener를 통해 start, end day의 text를 수정해준다.
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
        start_day_text.text = sdf.format(cal_start.time)
        end_day_text.text = sdf.format(cal_end.time)

        val start_day_set_listener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal_start.set(Calendar.YEAR, year)
            cal_start.set(Calendar.MONTH, month)
            cal_start.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            start_day_text.text = sdf.format(cal_start.time)
        }

        val end_day_set_listener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal_end.set(Calendar.YEAR, year)
            cal_end.set(Calendar.MONTH, month)
            cal_end.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            end_day_text.text = sdf.format(cal_end.time)
        }

        start_day_layout.setOnClickListener {
            DatePickerDialog(this, start_day_set_listener,
                cal_start.get(Calendar.YEAR),
                cal_start.get(Calendar.MONTH),
                cal_start.get(Calendar.DAY_OF_MONTH)).show()
        }

        end_day_layout.setOnClickListener {
            DatePickerDialog(this, end_day_set_listener,
                cal_end.get(Calendar.YEAR),
                cal_end.get(Calendar.MONTH),
                cal_end.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 여행 지역 설정
        country_layout.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when(v) {
            country_layout -> {
                val intent = Intent(this, CountryActivity::class.java)
                startActivity(intent)
            }
            add_money_button -> {
                // 예산 설정 액티비티 이동 (환율 api)
            }
            cover_image_layout -> {
                // 사진기능 넘어가기
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.profile_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_save -> {
                // 각종 항목들 저장 및 액티비티 이동
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
