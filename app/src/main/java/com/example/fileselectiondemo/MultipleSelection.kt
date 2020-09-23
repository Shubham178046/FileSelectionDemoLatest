package com.example.fileselectiondemo

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.yalantis.ucrop.util.FileUtils.*
import kotlinx.android.synthetic.main.activity_multiple_selection.*
import java.io.*


class MultipleSelection : AppCompatActivity() {
    private val CAMERA = 3
    private val FILE = 2
    private val GALLERY = 1
    private val PICK_CONTACT = 101
    private val VIDEO = 4
    var totalLength = 0L
    var currentPhotoPath: String = ""
    var imagesEncodedList = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiple_selection)
        actionListner()
    }

    fun actionListner() {
        rlAttachment.setOnClickListener {
            Log.d("TAG,", "actionListner: " + "Call")
            TedPermission.with(this).setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        val viewCount = llViewAttachment.childCount
                        var totalView = 5
                        var FinalCount = totalView - viewCount
                        if (FinalCount == 0) {
                            Toast.makeText(
                                this@MultipleSelection,
                                "You Reached Your Limit",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Log.d("TAG", "onPermissionGranted: " + "Call")
                            CustomDialog(
                                this@MultipleSelection,
                                object : CustomDialog.OnAttachmentClick {
                                    override fun onAttachmentClick(flag: Int) {
                                        if (flag == 1) {
                                            takePhotoFromCamera()
                                        } else if (flag == 2) {
                                            choosePhoto()
                                        } else if (flag == 3) {

                                        } else if (flag == 4) {

                                        } else if (flag == 5) {

                                        }
                                    }

                                })
                        }
                    }

                    override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                        Log.d("TAG", "onPermissionDenied: " + "Call")
                    }

                }).check()
        }
    }

    fun checkRotationFromCamera(bitmap: Bitmap, rotate: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun getImageOrientation(imagePath: String?): Int {
        var rotate = 0
        try {
            val exif = imagePath?.let { ExifInterface(it) }
            val orientation: Int = exif!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotate
    }

    private fun takePhotoFromCamera() {
        try {
            val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file = getImageFile() // 1
            val uri: Uri
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri =
                    FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        file
                    )
            } else {
                uri = Uri.fromFile(file) // 3
            }
            //pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri) // 4
            startActivityForResult(pictureIntent, CAMERA)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun choosePhoto() {
        val galleryIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)
        galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.setType("image/*")
        startActivityForResult(galleryIntent, GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA -> {
                Log.d("TAG", "onActivityResult: " + "Call")
                if (resultCode == RESULT_OK && null != data) {
                    Log.d("TAG", "requestCode: " + "Call")
//                    Log.d("TAG", "onActivityResult: " + data.extras!!.get("data"))
                    val uri = Uri.parse(currentPhotoPath)
                    val file = getFile(this, uri)
                    val fileName = getFileName(this, uri)
                    //val imageFile: File = File(uri.path)
                     val photo = data.extras!!["data"] as Bitmap?
                     val tempUri = getImageUri(this,photo!!)
                    val imageFile: File = File(getRealPathFromURI(tempUri))
                    addAttachment(fileName!!, file.toString(), imageFile)
                }
            }
            GALLERY -> {
                if (resultCode == RESULT_OK && null != data) {
                    var picturePath = ""
                    if (null != data) {
                        if (null != data.clipData) {
                            for (i in 0 until data.clipData!!.itemCount) {
                                val uri = data.clipData!!.getItemAt(i).uri
                                picturePath = UriUtils.getPathFromUri(this , uri)!!
                                imagesEncodedList.add(picturePath)
                            }
                        } else {
                            val uri = data.data
                            picturePath = URIPathHelper().getPath(this, uri!!)!!
                            imagesEncodedList.add(picturePath)
                        }
                    }
                    val count = llViewAttachment.getChildCount();
                    if (count == 0) {
                        totalLength = 0
                    }
                    for (filePath in imagesEncodedList) {
                        val file = File(filePath)
                        if (file.exists()) {
                            val length = file.length() / 1024
                            totalLength = totalLength + length

                            if (length >= (5 * 1024)) {
                                totalLength = totalLength - length
                                Toast.makeText(this, "File Is Larger", Toast.LENGTH_LONG).show()
                            } else {
                                addAttachment(getFileName(filePath)!!, filePath, file)
                            }
                        } else {
                            Toast.makeText(this, "Invalid Path", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path: String = MediaStore.Images.Media.insertImage(
            inContext.contentResolver,
            inImage, "Title", null
        )
        return Uri.parse(path)
    }

    fun addAttachment(fileName: String, filePath: String, file: File) {
        Log.d("TAG", "addAttachment: " + "call")
        txtAddAttachment.visibility = View.GONE
        llAddAttachment.visibility = View.VISIBLE
        llViewAttachment.visibility = View.VISIBLE

        val parentlayout = findViewById<View>(R.id.llViewAttachment) as LinearLayout
        val layoutInflater = layoutInflater
        var view: View
        view = layoutInflater.inflate(R.layout.item_add_attachment, parentlayout, false)
        val llView = view.findViewById(R.id.llView) as RelativeLayout
        val imgdata = view.findViewById(R.id.imgData) as ImageView
        val txtFileName = view.findViewById<TextView>(R.id.txtFileName)
        val txtFilePath = view.findViewById<TextView>(R.id.txtFilePath)
        val imgClose = view.findViewById(R.id.imgClose) as ImageView

        txtFileName.text = fileName
        txtFilePath.text = filePath
        try {
            var mResponseFile = file
            if (mResponseFile != null && mResponseFile!!.exists()) {
                val rotation: Int =
                    getImageOrientation(mResponseFile.path)
                var outStream: OutputStream? = null
                var bitmap = BitmapFactory.decodeFile(mResponseFile.path)
                bitmap = checkRotationFromCamera(bitmap!!, rotation)
                bitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap!!.width.toFloat() * 1f).toInt(),
                    (bitmap.height.toFloat() * 1f).toInt(),
                    false
                )
                Log.d("TAG", "processSingle: '" + mResponseFile.path)
                outStream = FileOutputStream(mResponseFile.path)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
                outStream.flush()
                outStream.close()
                Log.d("mResponse", "processSingle: " + mResponseFile)
                Glide.with(this).asBitmap()
                    .load(mResponseFile)
                    .into(imgdata)
                val mBitmap = BitmapFactory.decodeFile(mResponseFile.path)

            }
        } catch (e: Exception) {
            println("image picker crash" + e.localizedMessage)
        }

        imgClose.setOnClickListener {
            (llView.parent as ViewGroup).removeView(llView)

            val count = llViewAttachment.childCount
            if (count == 0) {
                txtAddAttachment.visibility = View.VISIBLE
                llAddAttachment.visibility = View.GONE
            } else {
                txtAddAttachment.visibility = View.GONE
                llAddAttachment.visibility = View.VISIBLE
            }
        }

        val count = llViewAttachment.childCount
        if (count < 5) {
            parentlayout.addView(view)
        }
    }

    fun getFileName(str: String): String? {
        return str.substring(str.lastIndexOf("/") + 1)
    }

    fun getFile(context: Context?, uri: Uri?): File? {
        if (uri != null) {
            val path: String = getPath(context, uri)!!
            if (path != null) {
                return File(path)
            }
        }
        return null
    }

    fun isLocal(url: String?): Boolean {
        return if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            true
        } else false
    }

    fun getRealPathFromURI(uri: Uri?): String? {
        val cursor = contentResolver.query(uri!!, null, null, null, null)
        cursor!!.moveToFirst()
        val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
        return cursor.getString(idx)
    }

    private fun getImageFile(): File {
        val imageFileName = "JPEG_" + System.currentTimeMillis() + "_"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Camera"
        )
        val file = File.createTempFile(imageFileName, ".jpg", storageDir)
        currentPhotoPath = "file:" + file.getAbsolutePath()
        return file
    }

    fun getPath(context: Context?, uri: Uri): String? {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                context,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]

            } else if (isDownloadsDocument(uri)) {
                try {
                    val id: String = DocumentsContract.getDocumentId(uri)
                    Log.d("TAG", "getPath: id= $id")
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    val segments = uri.pathSegments
                    if (segments.size > 1) {
                        val rawPath = segments[1]
                        return if (!rawPath.startsWith("/")) {
                            rawPath.substring(rawPath.indexOf("/"))
                        } else {
                            rawPath
                        }
                    }
                }
            } else if (isMediaDocument(uri)) {
                val docId: String = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if (("image" == type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if (("video" == type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if (("audio" == type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf(File.separator)
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }
}
