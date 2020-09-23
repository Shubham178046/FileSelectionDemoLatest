package com.example.fileselectiondemo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.StrictMode
import android.util.DisplayMetrics
import android.view.*
import kotlinx.android.synthetic.main.bottom_sheet_content_dialog.*

class CustomDialog(context: MultipleSelection, var onAttachmentClick: OnAttachmentClick) :
    Dialog(context) {
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.bottom_sheet_content_dialog)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        val displayMetrics = DisplayMetrics()
        context.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.MATCH_PARENT
        lp.gravity = Gravity.CENTER
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window!!.attributes = lp
        window!!.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        )
        window!!.setFlags(
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        )
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        show()
        initView()
        actionListner()

    }

    private fun actionListner() {
        txtGallery.setText(context.getString(R.string.text_gallery))
        txtCamera.setText(context.getString(R.string.text_camera))
        txtFile.setText(context.getString(R.string.text_file))
        txtVideo.setText(context.getString(R.string.text_video))
        txtCancel.setText(context.getString(R.string.text_cancel))


        llImageSource.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                dismiss()
                return true
            }

        })

        txtCamera.setOnClickListener {
            onAttachmentClick.onAttachmentClick(1)
            dismiss()
        }
        txtGallery.setOnClickListener {
            onAttachmentClick.onAttachmentClick(2)
            dismiss()
        }
        txtFile.setOnClickListener {
            onAttachmentClick.onAttachmentClick(3)
            dismiss()
        }
        txtVideo.setOnClickListener {
            onAttachmentClick.onAttachmentClick(4)
            dismiss()
        }
        txtCancel.setOnClickListener {
            onAttachmentClick.onAttachmentClick(5)
            dismiss()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (hasFocus) {
        } else {
            dismiss()
        }
    }

    private fun initView() {

    }

    interface OnAttachmentClick {
        fun onAttachmentClick(flag: Int)
    }


}