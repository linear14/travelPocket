package com.dongldh.travelpocket.content

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.dongldh.travelpocket.*
import com.dongldh.travelpocket.profile_setting.CoverDialog
import com.dongldh.travelpocket.profile_setting.FROM_ALBUM
import com.dongldh.travelpocket.profile_setting.FROM_CAMERA
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.text.SimpleDateFormat

val FROM_DETAIL = 114
class DetailActivity : AppCompatActivity(), View.OnClickListener {
    val coverDialog = CoverDialog()

    var itemNumber: Int? = null
    var helper: DBHelper? = null

    var num: String? = null
    var isPlus: Int? = null
    var moneyUsed: Double? = null
    var rate_tofrom: Double? = null
    var detail_image_uri: Uri? = null
    var image: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        selectDB()

        detail_content.setOnClickListener(this)
        detail_image_layout.setOnClickListener(this)
        detail_delete.setOnClickListener(this)
        detail_close.setOnClickListener(this)
    }

    fun selectDB() {
        itemNumber = intent.getIntExtra("itemNumber", 0)
        helper = DBHelper(this)
        val db = helper!!.writableDatabase

        val cursorContent = db.rawQuery("select * from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
        cursorContent.moveToNext()
        num = cursorContent.getString(0)
        val datecode = cursorContent.getString(1)
        val currency = cursorContent.getString(2)
        val type = cursorContent.getString(3)
        isPlus = cursorContent.getInt(4)
        val isCash = cursorContent.getInt(5)
        moneyUsed = cursorContent.getDouble(6)
        val used = cursorContent.getString(7)?:""
        val content = cursorContent.getString(8)
        image = cursorContent.getString(9)

        val cursorBudget = db.rawQuery("select rate_tofrom from t_budget where num=?", arrayOf(num))

        cursorBudget.moveToNext()
        rate_tofrom = cursorBudget.getDouble(0)

        when(isCash) {
            0 -> {
                detail_cash_card.setImageResource(R.drawable.ic_pay_method)
            }
            1 -> {
                detail_cash_card.setImageResource(R.drawable.ic_money)
            }
        }

        when(type) {
            "식비" -> {
                detail_type.setImageResource(R.drawable.ic_eat_checked)
            }
            "쇼핑" -> {
                detail_type.setImageResource(R.drawable.ic_shopping_checked)
            }
            "관광" -> {
                detail_type.setImageResource(R.drawable.ic_travel_checked)
            }
            "교통" -> {
                detail_type.setImageResource(R.drawable.ic_traffic_checked)
            }
            "숙박" -> {
                detail_type.setImageResource(R.drawable.ic_sleep_checked)
            }
            "etc" -> {
                detail_type.setImageResource(R.drawable.ic_etc_checked)
            }
        }

        if(used.equals("")) {
            detail_title.text = type
        } else {
            detail_title.text = "${type} : ${used}"
        }

        val date = SimpleDateFormat("yyMMdd").parse(datecode)
        detail_date.text = SimpleDateFormat("yyyy년 MM월 dd일").format(date)

        detail_money.text = "${currency} ${String.format("%,d", moneyUsed!!.toInt())}"
        detail_money_mycountry.text = "${App.pref.myCurrency} ${String.format("%,d", (moneyUsed!! * rate_tofrom!!).toInt())}"

        if(!content.isNullOrEmpty()) {
            detail_content.text = content
        }

        detail_image_uri = Uri.parse(image)
        if(!image.isNullOrEmpty()) {
            detail_image.setImageURI(detail_image_uri)
            detail_image_photo.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when(v) {
            detail_content -> {
                val db = helper!!.writableDatabase
                val cursor = db.rawQuery("select detail_content from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
                cursor.moveToNext()

                val content = cursor.getString(0)

                val intent = Intent(this@DetailActivity, MemoActivity::class.java)
                intent.putExtra("detail_content", content)
                intent.putExtra("itemNumber", itemNumber.toString())
                intent.putExtra("requestCode", "DetailActivity")
                Log.d("ItemNumber", itemNumber.toString())
                startActivityForResult(intent, FROM_DETAIL)
            }

            detail_image_layout -> {
                val bundle = Bundle()
                bundle.putString("uri", detail_image_uri.toString())
                bundle.putString("itemNumber", itemNumber.toString())
                coverDialog.arguments = bundle
                coverDialog.show(supportFragmentManager, "dialog_event")
            }

            detail_delete -> {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("항목 삭제하기")
                    .setMessage("정말 이 항목을 삭제하시겠습니까?\n삭제 후 복구가 불가능합니다.")

                builder.setPositiveButton("확인") { dialog, which ->
                    val db = helper!!.writableDatabase
                    db.delete("t_content", "itemNumber=?", arrayOf(itemNumber.toString()))

                    val cursor = db.rawQuery("select used_money_mycountry from t_travel where num=?", arrayOf(num.toString()))
                    cursor.moveToNext()
                    var usedMoney = cursor.getDouble(0)

                    if(isPlus == 0) {
                        usedMoney = usedMoney - (moneyUsed!! * rate_tofrom!!)
                    } else {
                        usedMoney = usedMoney + (moneyUsed!! * rate_tofrom!!)
                    }

                    val contentValuesUsedMoney = ContentValues()
                    contentValuesUsedMoney.put("used_money_mycountry", usedMoney)
                    db.update("t_travel", contentValuesUsedMoney, "num=?", arrayOf(num.toString()))

                    db.close()

                    dialog.dismiss()
                    finish()
                }

                builder.setNegativeButton("취소") { dialog, which ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            }

            detail_close -> {
                finish()
            }
         }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            FROM_DETAIL -> {
                val db = helper!!.writableDatabase
                val cursorContent = db.rawQuery("select detail_content from t_content where itemNumber=?", arrayOf(itemNumber.toString()))
                cursorContent.moveToNext()
                val content = cursorContent.getString(0)

                if(!content.isNullOrEmpty()) {
                    detail_content.text = content
                } else {
                    detail_content.text = "지출에 대해 노트해보세요."
                }

                db.close()
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

                        detail_image_uri = coverDialog.photoUri
                        detail_image?.setImageURI(detail_image_uri)
                        detail_image_photo.visibility = View.GONE

                        val db = helper!!.writableDatabase
                        val contentValues = ContentValues()
                        contentValues.put("image", detail_image_uri.toString())

                        db.update("t_content", contentValues, "itemNumber=?", arrayOf(itemNumber.toString()))
                        db.close()
                        selectDB()
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

                detail_image_uri = coverDialog.getImageUriFromBitmap(this, coverDialog.rotate(bitmap, exifDegree))
                cover_image.setImageURI(detail_image_uri)

                val db = helper!!.writableDatabase
                val contentValues = ContentValues()
                contentValues.put("image", detail_image_uri.toString())

                db.update("t_content", contentValues, "itemNumber=?", arrayOf(itemNumber.toString()))
                db.close()
                selectDB()
            }
        }
    }

}
