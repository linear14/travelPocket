package com.dongldh.travelpocket

import android.app.backup.BackupManager
import android.content.Context
import android.graphics.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_backup.*
import kotlinx.android.synthetic.main.dialog_backup.view.*
import kotlinx.android.synthetic.main.dialog_cover.view.*

class BackupDialog : DialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_backup, container, false)
        view.data_backup.setOnClickListener(this)
        view.data_restore.setOnClickListener(this)
        view.back.setOnClickListener(this)
        isCancelable = false
        return view
    }

    override fun onClick(v: View?) {
        when(v) {
            data_backup -> {
                val backupManager = BackupManager(activity)
                backupManager.dataChanged()
                dismiss()
            }

            data_restore -> {
                dismiss()
            }

            back -> {
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val windowManager = this.context!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val params = dialog?.window?.attributes
        val deviceWidth = size.x
        params?.width = (deviceWidth * 0.7).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
}
