package com.example.administrator.recycler.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

import com.example.administrator.recycler.R

class MainActivity : AppCompatActivity() , View.OnClickListener{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bt_flutter.setOnClickListener(this)
        bt_person.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.bt_flutter -> {startActivity(Intent(this, FlutterActivity::class.java))}
            R.id.bt_person -> {startActivity(Intent(this, PersonActivity::class.java))}
        }
    }

}
