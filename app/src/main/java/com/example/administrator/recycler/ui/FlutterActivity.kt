package com.example.administrator.recycler.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.example.administrator.recycler.R
import io.flutter.facade.Flutter
import kotlinx.android.synthetic.main.activity_flutter.*

class FlutterActivity: AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flutter)
        show.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.show -> showFlutter()
        }
    }

    private fun showFlutter(){
        val flutterView =  Flutter.createView(this, lifecycle, "/")
        flutter_linear.addView(flutterView)
    }
}