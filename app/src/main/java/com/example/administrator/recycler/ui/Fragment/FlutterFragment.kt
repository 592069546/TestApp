package com.example.administrator.recycler.ui.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.administrator.recycler.R
import io.flutter.facade.Flutter

import kotlinx.android.synthetic.main.fragment_flutter.*

class FlutterFragment: LazyFragment(){
    override val layoutId = R.layout.fragment_flutter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        showFlutter()
    }

    private fun showFlutter(){
        val flutterView =  Flutter.createView(activity!!, lifecycle, "/")
        fragment_flutter_layout.addView(flutterView)
    }

    override fun onInvisible() {}

    override fun onVisible() {}
}