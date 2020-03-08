package com.dongldh.travelpocket

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import com.dongldh.travelpocket.profile_setting.CountryActivity
import com.dongldh.travelpocket.profile_setting.CoverDialog
import com.dongldh.travelpocket.profile_setting.FROM_ALBUM
import com.dongldh.travelpocket.profile_setting.FROM_CAMERA
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// https://freakycoder.com/android-notes-72-how-to-convert-bitmap-to-uri-e535391ebdac (Bitmap to Uri)
// https://stackoverflow.com/questions/6602417/get-the-uri-of-an-image-stored-in-drawable (Drawable에 저장되어 있는 이미지의 Uri 값 가져오기)

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    // 클래스의 멤버변수로 선언한 것 들은 나중에 정보 보낼 때 사용 됨
    val cal_start: Calendar = Calendar.getInstance()
    val cal_end: Calendar = Calendar.getInstance()

    var title: String? = null
    var flag = R.drawable.flag_kr
    var country = "대한민국"
    var cover_image_uri: Uri? = null

    val SELECT_COUNTRY = 10

    // CoverDialog()의 메서드를 사용하려고 선언하긴 했는데, 더 좋은 방법이 있찌 않을까?
    val coverDialog = CoverDialog()

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
        flag_image.setImageResource(flag)
        country_layout.setOnClickListener(this)
        cover_image_layout.setOnClickListener(this)
    }



    override fun onClick(v: View?) {
        when(v) {
            country_layout -> {
                val intent = Intent(this, CountryActivity::class.java)
                startActivityForResult(intent, SELECT_COUNTRY)
            }
            add_money_button -> {
                // 예산 설정 액티비티 이동 (환율 api)
            }
            cover_image_layout -> {
               // 사진 설정. Cover Dialog랑 연결 하고 싶은데..
                coverDialog.show(supportFragmentManager, "dialog_event")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // log
        // Log.d("ProfileActivity", "requestCode : ${requestCode}")

        when(requestCode) {
            SELECT_COUNTRY -> {
                if(resultCode == Activity.RESULT_OK) {
                    flag = data!!.getIntExtra("flag", 0)
                    country = data!!.getStringExtra("country")
                    flag_image.setImageResource(flag)
                    country_text.text = country
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
                    exif = ExifInterface(coverDialog.currentPhotoPath)
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
                cover_image_uri = getImageUriFromBitmap(this, coverDialog.rotate(bitmap, exifDegree))
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
                // 각종 항목들 저장 및 액티비티 이동
                val helper = DBHelper(this)
                val db = helper.writableDatabase

                // 이미지 정보의 Uri를 String 꼴로 저장.
                var cover: String? = null
                if(cover_image_uri == null) {
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
                contentValues.put("flag", flag)
                contentValues.put("cover_image", cover)

                db.insert("t_travel", null, contentValues)
                db.close()
                finish()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Bitmap to Uri
    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }
}
