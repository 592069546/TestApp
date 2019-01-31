package com.example.administrator.recycler

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import io.flutter.facade.Flutter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() , View.OnClickListener{
    var mdata = ArrayList<Person>(5)
    val adapter by lazy { Adapter(this@MainActivity,mdata) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        sortData()
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
        recycler.layoutManager = LinearLayoutManager(this)
        add.setOnClickListener(this)
        add.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
        del.setOnClickListener(this)
        flutter_activity.setOnClickListener(this)
    }

    override fun onClick(view: View){
        when(view.id){
            R.id.add -> addItem()
            R.id.del -> delItem()
            R.id.flutter_activity -> toFlutter()
        }
    }

    private fun addItem(){
        adapter.addItem()
    }

    private fun delItem(){
        adapter.delItem()
    }

    private fun toFlutter(){
        val flutterView =  Flutter.createView(this, lifecycle, "/")
        setContentView(flutterView)
    }

    fun initData(){
        mdata.add(Person("sss",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("aaa",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("ddd",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("www",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("qqq",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("dsa",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("asd",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("sad",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("das",Random().nextInt(30),Random().nextInt(2)))
        mdata.add(Person("sda",Random().nextInt(30),Random().nextInt(2)))
    }

    fun sortData(){
        var j = 0
        for(i in 0 until mdata.size ){
            if(mdata[i].type == 1){
                mdata.add(j,mdata.removeAt(i))
                j++
            }
        }
    }
}
