package com.dongldh.travelpocket

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dongldh.travelpocket.profile_setting.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_profile.view.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// https://freakycoder.com/android-notes-72-how-to-convert-bitmap-to-uri-e535391ebdac (Bitmap to Uri)
// https://stackoverflow.com/questions/6602417/get-the-uri-of-an-image-stored-in-drawable (Drawable에 저장되어 있는 이미지의 Uri 값 가져오기)
val SELECT_COUNTRY = 10
val SELECT_BUDGET = 11
val SELECT_BUDGET_EDIT = 12

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    var requestCode: String? = null
    var num_request: Int? = null

    // 클래스의 멤버변수로 선언한 것 들은 나중에 정보 보낼 때 사용 됨
    val cal_start: Calendar = Calendar.getInstance()
    val cal_end: Calendar = Calendar.getInstance()

    var budget_list: MutableList<DataBudget> = mutableListOf()

    var title: String? = null
    var flag = App.pref.myFlagId
    var country = App.pref.myCountry
    var currency = App.pref.myCurrency
    var code = App.pref.myCode
    var budget = 0.0F
    var cover_image_uri: Uri? = null

    var dayList = mutableListOf<String>()

    // CoverDialog()의 메서드를 사용하려고 선언하긴 했는데, 더 좋은 방법이 있찌 않을까?
    val coverDialog = CoverDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        requestCode = intent.getStringExtra("requestCode")
        if(requestCode.equals("ContentActivity")) {
            num_request = intent.getIntExtra("num", 0)

            val helper = DBHelper(this)
            val db = helper.writableDatabase
            val cursor = db.rawQuery("select * from t_travel where num=?", arrayOf(num_request.toString()))
            cursor.moveToNext()

            title = cursor.getString(1)
            cal_start.time = Date(cursor.getLong(2))
            cal_end.time= Date(cursor.getLong(3))

            for(i in cursor.getLong(2) .. cursor.getLong(3) step 24*60*60*1000) {
                dayList.add(SimpleDateFormat("yyMMdd").format(i))
            }

            flag = cursor.getInt(6)
            country = cursor.getString(4)
            cover_image_uri = Uri.parse(cursor.getString(7))
            cover_image.setImageURI(cover_image_uri)

            val cursor_budget = db.rawQuery("select * from t_budget where num=?", arrayOf(num_request.toString()))
            while(cursor_budget.moveToNext()) {
                budget_list.add(DataBudget(
                    cursor_budget.getString(1),
                    cursor_budget.getFloat(2),
                    cursor_budget.getString(3),
                    cursor_budget.getDouble(4),
                    cursor_budget.getDouble(5)
                ))
            }
            db.close()

        } else {
            title = intent.getStringExtra("title")
        }

        // 앱바 제목
        setTitle("여행 프로필")

        // 여행 제목 설정
        profile_title_edit.setText(title)

        // 날짜 설정
        // 클릭하면 setOnClickListener에 의해 DatePickerDialog를 불러온다. 여기서 현재 저장 되어있는 날짜가 지정된 상태로 열리도록 함. (get)
        // 캘린더를 종료할 시 등록된 OnDateSetListener를 통해 start, end day의 text를 수정해준다.
        val sdf = SimpleDateFormat("yyyy년 MM월 dd일")
        start_day_text.text = sdf.format(cal_start.time)
        end_day_text.text = sdf.format(cal_end.time)

        // 시간 초기화 작업
        cal_start.set(Calendar.HOUR_OF_DAY, 1)
        cal_start.set(Calendar.MINUTE, 0)
        cal_start.set(Calendar.SECOND, 0)
        cal_start.set(Calendar.MILLISECOND, 0)
        cal_end.set(Calendar.HOUR_OF_DAY, 5)


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
        flag_image.setImageResource(flag!!)
        country_text.text = country
        country_layout.setOnClickListener(this)

        add_money_button.setOnClickListener(this)
        cover_image_layout.setOnClickListener(this)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ProfileAdapter(budget_list)

    }



    override fun onClick(v: View?) {
        when(v) {
            country_layout -> {
                val intent = Intent(this, CountryActivity::class.java)
                intent.putExtra("request", SELECT_COUNTRY)
                startActivityForResult(intent, SELECT_COUNTRY)
            }

            add_money_button -> {
                // 예산 설정 액티비티 이동 (환율 api)
                val intent = Intent(this, BudgetActivity::class.java)

                intent.putExtra("currency", currency)
                intent.putExtra("code", code)
                startActivityForResult(intent, SELECT_BUDGET)
            }

            cover_image_layout -> {
               // 사진 설정. Cover Dialog랑 연결 하고 싶은데..
                coverDialog.show(supportFragmentManager, "dialog_event")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            SELECT_COUNTRY -> {
                if(resultCode == Activity.RESULT_OK) {
                    flag = data!!.getIntExtra("flag", 0)
                    country = data.getStringExtra("country")
                    if(data.getStringExtra("currency") != null) {
                        currency = data.getStringExtra("currency")
                        code = data.getStringExtra("code")
                    }
                    flag_image.setImageResource(flag!!)
                    country_text.text = country
                }
            }

            // 예산 설정. 여기에 추가 된 budget값을 리사이클러뷰에 계속 넣어줘야 됨.
            SELECT_BUDGET -> {
                if(resultCode == Activity.RESULT_OK) {
                    val currency = data!!.getStringExtra("currency")
                    val code = data.getStringExtra("code")

                    val rate_fromto_input = data.getStringExtra("rate_fromto")
                    val rate_tofrom_input = data.getStringExtra("rate_tofrom")

                    val rate_fromto = rate_fromto_input.toDouble()
                    val rate_tofrom = rate_tofrom_input.toDouble()

                    // budget값이 안들어옴. -> 먼저 스트링으로 받고, 그다음에 float 해줌으로 해결 (BudgetActivity에서도 .text.toString()으로 받아야함)
                    val input = data!!.getStringExtra("budget")
                    val budget = input.toFloat()

                    budget_list.add(DataBudget(currency, budget, code, rate_fromto, rate_tofrom))
                    recycler.layoutManager = LinearLayoutManager(this)
                    recycler.adapter = ProfileAdapter(budget_list)
                }
            }

            SELECT_BUDGET_EDIT -> {
                if(resultCode == Activity.RESULT_OK) {
                    val currency = data!!.getStringExtra("currency")
                    val code = data.getStringExtra("code")

                    val rate_fromto_input = data.getStringExtra("rate_fromto")
                    val rate_tofrom_input = data.getStringExtra("rate_tofrom")

                    val rate_fromto = rate_fromto_input.toDouble()
                    val rate_tofrom = rate_tofrom_input.toDouble()

                    // budget값이 안들어옴. -> 먼저 스트링으로 받고, 그다음에 float 해줌으로 해결 (BudgetActivity에서도 .text.toString()으로 받아야함)
                    val input = data!!.getStringExtra("budget")
                    val budget = input.toFloat()

                    budget_list.set(data.getIntExtra("position", -1), DataBudget(currency, budget, code, rate_fromto, rate_tofrom))
                    recycler.layoutManager = LinearLayoutManager(this)
                    recycler.adapter = ProfileAdapter(budget_list)
                }
            }

            FROM_ALBUM -> {
                if (data?.data != null) {
                    try {
                        var albumFile: File? = null
                        albumFile = coverDialog.createImageFile()

                        coverDialog.photoUri = data.data
                        coverDialog.albumUri = Uri.fromFile(albumFile)

                        // log
                        Toast.makeText(this, "앨범사진 선택", Toast.LENGTH_SHORT).show()

                        cover_image_uri = coverDialog.photoUri
                        cover_image?.setImageURI(cover_image_uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            FROM_CAMERA -> {
                val bitmap = BitmapFactory.decodeFile(coverDialog.currentPhotoPath)
                var exif: ExifInterface? = null

                try {
                    // galleryAddPic() (갤러리에 찍은 사진 넣는건가? 이것도 함 봐야겠땅)
                    exif = ExifInterface(coverDialog.currentPhotoPath!!)
                } catch(e: Exception) {
                    e.printStackTrace()
                }

                var exifOrientation: Int? = null
                var exifDegree: Float? = null

                if(exif != null) {
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    exifDegree = coverDialog.exifOrientationToDegrees(exifOrientation).toFloat()
                } else {
                    exifDegree = 0.0F
                }

                // 비트맵을 우선 uri로 바꿔주기 (Uri 값을 데이터로 넘겨야해서..)
                cover_image_uri = coverDialog.getImageUriFromBitmap(this, coverDialog.rotate(bitmap, exifDegree))
                cover_image.setImageURI(cover_image_uri)
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
                if(profile_title_edit.text.isNullOrEmpty()) {
                    Toast.makeText(this@ProfileActivity, "여행 타이틀을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else if(cal_end.time < cal_start.time) {
                    Toast.makeText(this@ProfileActivity, "여행 종료 날짜는 여행 시작 날보다 빠를 수 없습니다. 다시 설정하세요.", Toast.LENGTH_SHORT).show()
                } else {
                title = profile_title_edit.text.toString()
                // 수정 작업
                if(requestCode.equals("ContentActivity")) {
                    val helper = DBHelper(this)
                    val db = helper.writableDatabase

                    var cover: String? = null
                    cover = cover_image_uri.toString()

                    val newStartDayIndex = dayList.indexOf(SimpleDateFormat("yyMMdd").format(cal_start.timeInMillis))
                    val newEndDayIndex = dayList.indexOf(SimpleDateFormat("yyMMdd").format(cal_end.timeInMillis))

                    for(i in 0 until newStartDayIndex) {
                        db.delete("t_content", "num=? and datecode=?", arrayOf(num_request.toString(), dayList[i]))
                    }

                    if(newEndDayIndex != -1) {
                        for (i in newEndDayIndex + 1 until dayList.size) {
                            db.delete("t_content", "num=? and datecode=?", arrayOf(num_request.toString(), dayList[i]))
                        }
                    }
                    val contentValues = ContentValues()
                    contentValues.put("title", title)
                    contentValues.put("start_day", cal_start.timeInMillis)
                    contentValues.put("end_day", cal_end.timeInMillis)
                    contentValues.put("country", country)
                    contentValues.put("currency", currency)
                    contentValues.put("flag", flag)
                    contentValues.put("cover_image", cover)

                    db.update("t_travel", contentValues, "num=?", arrayOf(num_request.toString()))
                    updateBudget(db, num_request!!)

                    db.close()
                    finish()
                } else {
                    // 최초 저장 작업
                    // 각종 항목들 저장 및 액티비티 이동
                    val helper = DBHelper(this)
                    val db = helper.writableDatabase
                    val timestamp: String = SimpleDateFormat("yyMMddhhmmss").format(System.currentTimeMillis())

                    // 이미지 정보의 Uri를 String 꼴로 저장.
                    var cover: String? = null
                    if (cover_image_uri == null) {
                        var basicCoverUri: Uri = Uri.Builder()
                            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                            .authority(resources.getResourcePackageName(R.drawable.image_cover))
                            .appendPath(resources.getResourceTypeName(R.drawable.image_cover))
                            .appendPath(resources.getResourceEntryName(R.drawable.image_cover))
                            .build()
                        cover = basicCoverUri.toString()
                    } else {
                        cover = cover_image_uri.toString()
                    }

                    val contentValues = ContentValues()
                    contentValues.put("title", title)
                    contentValues.put("start_day", cal_start.timeInMillis)
                    contentValues.put("end_day", cal_end.timeInMillis)
                    contentValues.put("country", country)
                    contentValues.put("currency", currency)
                    contentValues.put("flag", flag)
                    contentValues.put("cover_image", cover)
                    contentValues.put("made_time", timestamp)

                    db.insert("t_travel", null, contentValues)


                    val numMadeQuery = db.rawQuery("select num from t_travel where made_time=?",
                        arrayOf(timestamp))
                    numMadeQuery.moveToNext()
                    val num = numMadeQuery.getInt(0)
                    updateBudget(db, num)

                    db.close()
                    finish()
                    }

                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun updateBudget(db: SQLiteDatabase , num: Int) {
        var total_money_mycountry = 0.0
        if(requestCode.equals("ContentActivity")) {
            db.delete("t_budget", "num=?", arrayOf(num.toString()))
        }
        if (budget_list.size == 0) {
            val contentValues_budget = ContentValues()
            contentValues_budget.put("num", num)
            contentValues_budget.put("currency", App.pref.myCurrency)
            contentValues_budget.put("money", 0.0)
            contentValues_budget.put("code", App.pref.myCode)
            contentValues_budget.put("rate_fromto", 1)
            contentValues_budget.put("rate_tofrom", 1)

            db.insert("t_budget", null, contentValues_budget)
        } else {
            for (i in 0 until budget_list.size) {
                val contentValues_budget = ContentValues()
                contentValues_budget.put("num", num)
                contentValues_budget.put("currency", budget_list[i].currency)
                contentValues_budget.put("money", budget_list[i].budget)
                contentValues_budget.put("code", budget_list[i].code)
                contentValues_budget.put("rate_fromto", budget_list[i].rate_fromto)
                contentValues_budget.put("rate_tofrom", budget_list[i].rate_tofrom)

                db.insert("t_budget", null, contentValues_budget)

                total_money_mycountry += ((budget_list[i].budget!!) * (budget_list[i].rate_tofrom ?: 1.0))
            }
            val contentValues_travel = ContentValues()
            contentValues_travel.put("total_money_mycountry", total_money_mycountry)
            db.update("t_travel", contentValues_travel, "num=?", arrayOf(num.toString()))
        }
    }

    inner class ProfileViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val budget = view.budget_button
    }

    inner class ProfileAdapter(val list: MutableList<DataBudget>): RecyclerView.Adapter<ProfileViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return ProfileViewHolder(layoutInflater.inflate(R.layout.item_profile, parent, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
            val data = list[position]

            holder.budget.text = "${data.currency} ${data.budget}"
            holder.budget.setOnClickListener {
                // BudgetActivity로 이동하고 데이터 수정 (현재 데이터를 넘겨줘야함)
                val intent = Intent(this@ProfileActivity, BudgetActivity::class.java)
                intent.putExtra("currency", data.currency)
                intent.putExtra("code", data.code)
                intent.putExtra("budget", data.budget)
                intent.putExtra("position", position)
                startActivityForResult(intent, SELECT_BUDGET_EDIT)
            }
        }
    }
}



