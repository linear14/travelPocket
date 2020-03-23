package com.dongldh.travelpocket.profile_setting

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.dongldh.travelpocket.R
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.dialog_cover.*
import kotlinx.android.synthetic.main.dialog_cover.view.*
import java.io.ByteArrayOutputStream
import java.io.File

// fragment에서 startActivityForResult를 사용할 때, 그냥 사용하면 activity에서 받는 requestcode가 이상해진다.
// 따라서, activity?.startActivityForResult를 사용하여 마치 액티비티가 intent를 넘겨준 것 처럼 해야한다.
    val FROM_ALBUM = 1
    val FROM_CAMERA = 2

class CoverDialog: DialogFragment(), View.OnClickListener {
    lateinit var imgUri: Uri
    var photoUri: Uri? = null
    lateinit var albumUri: Uri
    var currentPhotoPath: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_cover, container, false)
        view.from_photo_text.setOnClickListener(this)
        view.from_gallery_text.setOnClickListener(this)
        view.back_text.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View?) {
        when(v) {
            from_photo_text -> {
                takePhoto()
                dismiss()
            }
            from_gallery_text -> {
                selectFromGallery()
                dismiss()
            }
            back_text -> {
                dismiss()
            }
        }
    }

    fun takePhoto() {
        val state = Environment.getExternalStorageState()

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // image capture 기능이 null이 아니면 이라고 일단 해석했음
            if(intent.resolveActivity(activity!!.packageManager) != null) {
                var photoFile: File? = null
                try {
                    // 찍은 사진을 imageFile로 만들어주자
                    photoFile = createImageFile()
                } catch(e: Exception) {
                    e.printStackTrace()
                }
                if(photoFile != null) {
                    // 만든 이미지파일의 URI 를 가져온다
                    // FileProvider오류
                    // (두번째 파라미터로 들어간 package name과 manifests에서의 applicationId 가 같아야함)
                    val providerUri: Uri = FileProvider.getUriForFile(this.context!!, "com.dongldh.travelpocket.file", photoFile)
                    imgUri = providerUri
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, providerUri)
                    activity?.startActivityForResult(intent, FROM_CAMERA)
                }
            }
        } else {
            Toast.makeText(this.context!!, "저장 공간에 접근 불가능", Toast.LENGTH_SHORT).show()
            return
        }
    }

    fun selectFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        intent.type="image/*"

        //log
        // Log.d("CoverDialog", "requestCode : ${FROM_ALBUM}")

        activity?.startActivityForResult(intent, FROM_ALBUM)
    }


    // 카메라로 촬영한 이미지를 이미지파일로 생성
    // 혹은 앨범에서 선택한 이미지를 이미지파일로 생성
    fun createImageFile(): File {
        val imageFileName: String = "${System.currentTimeMillis()}.jpg"
        var imageFile: File? = null

        // 이미지를 저장할 공간을 만듬
        val storageDir = File("${Environment.getExternalStorageDirectory()}/Pictures")

        // 폴더가 없으면 생성
        if(!storageDir.exists()) {
            storageDir.mkdirs()
        }

        // 이미지 파일을 설정한 경로에 만들고, 그 주소를 전역변수에 넣어준다.
        imageFile = File(storageDir, imageFileName)
        currentPhotoPath = imageFile.absolutePath

        return imageFile
    }

    // 회전되어 있는 사진 처리
    // 상수를 받아 각도로 변환시켜주는 메서드
    fun exifOrientationToDegrees(exifOrientation: Int): Int {
        when (exifOrientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return 90
            ExifInterface.ORIENTATION_ROTATE_180 -> return 180
            ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            else -> return 0
        }
    }
    // 각도대로 회전시켜 결과를 반환시켜주는 메서드
    fun rotate(bitmap: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // Bitmap to Uri
    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }
}