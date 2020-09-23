package com.example.fileselectiondemo

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val CAMERA = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener {
            getPermission()
        }
    }
    private fun getPermission() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                CAMERA
            )
        } else {
            selectImage(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA -> {
                if ((grantResults.isNotEmpty()) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    selectImage(this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.extras!!["data"] as Bitmap?
                    my_avatar.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == RESULT_OK && data != null) {
                    val selectedImage = data.data
                    val filePathColumn =
                        arrayOf(MediaStore.Images.Media.DATA)
                    if (selectedImage != null) {
                        val cursor = contentResolver.query(
                            selectedImage,
                            filePathColumn, null, null, null
                        )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                            val picturePath = cursor.getString(columnIndex)
                            val img = File(picturePath)
                            val filelength = img.length().toInt()

                            val file_size: Int =
                                (img.length() / 1024).toInt()
                            Log.d("TAG", "onActivityResult: " + file_size + " " + filelength)
                            if (file_size <= 2024) {
                                my_avatar.visibility = View.VISIBLE
                                my_avatar.setImageBitmap(BitmapFactory.decodeFile(picturePath))
                            } else {
                                Toast.makeText(this, "File is larger", Toast.LENGTH_LONG).show()
                            }
                            cursor.close()
                        }
                    }
                }
                2 -> {
                    val fileName = getFileName(this, data!!.getData()!!)
                    generateImageFromPdf(data!!.getData()!!)
                    if (fileName!!.isEmpty()) {
                        Log.d("FileName", "onActivityResult: " + "File IS EMPTY")
                    }
                    pdfName.visibility = View.VISIBLE
                    pdfName.setText(fileName)
                }
            }
        }
    }

    fun generateImageFromPdf(pdfUri: Uri?) {
        val pageNumber = 0
        val pdfiumCore = PdfiumCore(this)
        try {
            //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
            val fd = contentResolver.openFileDescriptor(pdfUri!!, "r")
            val pdfDocument: PdfDocument? = pdfiumCore.newDocument(fd)
            pdfiumCore.openPage(pdfDocument, pageNumber)
            val width: Int = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber)
            val height: Int = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
            my_avatar.visibility = View.VISIBLE
            my_avatar.setImageBitmap(bmp)
            saveImage(bmp)
            pdfiumCore.closeDocument(pdfDocument) // important!
        } catch (e: Exception) {
            //todo with exception
        }
    }

    val FOLDER: String = android.os.Environment.getExternalStorageDirectory().toString() + "/PDF"

    private fun saveImage(bmp: Bitmap) {
        var out: FileOutputStream? = null
        try {
            val folder = File(FOLDER)
            if (!folder.exists()) folder.mkdirs()
            val file = File(folder, "PDF.png")
            out = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
        } catch (e: Exception) {
            //todo with exception
        } finally {
            try {
                if (out != null) out.close()
            } catch (e: Exception) {
                //todo with exception
            }
        }
    }

    private fun selectImage(context: Context) {
        val options =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "File", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            if (options[item] == "Take Photo") {
                val takePicture =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(takePicture, 0)
            } else if (options[item] == "Choose from Gallery") {
                val pickPhoto = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(pickPhoto, 1) //one can be replaced with any action code
            } else if (options[item] == "File") {
                val intent = Intent()
                intent.action = Intent.ACTION_GET_CONTENT
                intent.type = "application/pdf"
                startActivityForResult(intent, 2)
            } else if (options[item] == "Cancel") {
                dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri != null && context != null) {
            var returnCursor: Cursor? =
                context.getContentResolver().query(uri, null, null, null, null)

            if (returnCursor != null) {
                var nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                var sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                returnCursor.moveToFirst();
                if (nameIndex >= 0 && sizeIndex >= 0) {
                    /* Log.d("File Name : " + returnCursor.getString(nameIndex))
                     Log.d("File Size : " + returnCursor.getLong(sizeIndex))*/
                    Log.d("TAG", "File Name : " + returnCursor.getString(nameIndex))
                    Log.d("TAG", "File Size : " + returnCursor.getLong(sizeIndex))
                    // var isValidFile :  Boolean  = checkFormat(returnCursor.getString(nameIndex));
                    var isValidFile: Boolean = checkOtherFileType(returnCursor.getString(nameIndex))
                    Log.d("TAG", "getFileName: " + isValidFile)
                    if (isValidFile) {
                        return returnCursor.getString(nameIndex);
                    } else {
                        Log.d("isValidFile", "getFileName: " + isValidFile)
                    }
                }
            }
        }
        return null
    }

    fun checkOtherFileType(filePath: String): Boolean {
        if (filePath.isNotEmpty()) {
            val filePathInLowerCase = filePath.toLowerCase()
            if (filePathInLowerCase.endsWith(".pdf")) {
                return true
            }
        }
        return false
    }
}

