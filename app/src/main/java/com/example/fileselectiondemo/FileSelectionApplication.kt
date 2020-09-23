package com.example.fileselectiondemo

import android.app.Application
import com.miguelbcr.ui.rx_paparazzo2.RxPaparazzo

class FileSelectionApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RxPaparazzo.register(this)
    }
}