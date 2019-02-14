package com.example.administrator.recycler.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

import com.example.administrator.recycler.Adapter.PersonAdapter
import com.example.administrator.recycler.R
import com.example.administrator.recycler.data.Person

class MainActivity : AppCompatActivity() , View.OnClickListener{
    var mdata = ArrayList<Person>()
    val adapter by lazy { PersonAdapter(this@MainActivity,mdata) }
    val person = Person()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mdata = person.initData(10)
        person.sortData(mdata)

        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
        recycler.layoutManager = LinearLayoutManager(this)
        add.setOnClickListener(this)
        add.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
        add.visibility = View.INVISIBLE
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
        add.visibility = View.VISIBLE
    }

    private fun toFlutter(){
        startActivity(Intent(this, FlutterActivity::class.java))
    }

}
