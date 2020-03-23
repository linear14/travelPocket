package com.dongldh.travelpocket.content

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dongldh.travelpocket.DBHelper
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_budget.*
import kotlinx.android.synthetic.main.activity_memo.*

class MemoActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memo)

        memo_edit.setText(intent.getStringExtra("detail_content")?:null)
        memo_back.setOnClickListener(this)
        memo_save.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v) {
            memo_back -> {
                finish()
            }

            memo_save -> {
                if(intent.getStringExtra("requestCode").equals("DetailActivity")){
                    val itemNumber = intent.getStringExtra("itemNumber")
                    val helper = DBHelper(this)
                    val db = helper.writableDatabase
                    val contentValues = ContentValues()
                    contentValues.put("detail_content", memo_edit.text.toString())

                    db.update("t_content", contentValues, "itemNumber=?", arrayOf(itemNumber))
                    db.close()
                    finish()
                } else {
                    val selectedIntent = Intent()
                    selectedIntent.putExtra("detail_content", memo_edit.text.toString())

                    setResult(Activity.RESULT_OK, selectedIntent)
                    finish()
                }
            }
        }
    }
}
