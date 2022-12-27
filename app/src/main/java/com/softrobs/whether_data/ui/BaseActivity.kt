package com.softrobs.whether_data.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

abstract class BaseActivity : AppCompatActivity() {
    protected val baseActivityName = "Base Activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutResourceId())
        initUI()
    }

    protected abstract fun getLayoutResourceId():Int

    protected abstract fun initUI()
}