package com.example.administrator.recycler.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.administrator.recycler.R
import io.flutter.facade.Flutter
import kotlinx.android.synthetic.main.activity_flutter.*

class FlutterActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flutter)
        showFlutter()
    }

    private fun showFlutter(){
        val flutterView =  Flutter.createView(this, lifecycle, "/")
        flutter_layout.addView(flutterView)
    }
}