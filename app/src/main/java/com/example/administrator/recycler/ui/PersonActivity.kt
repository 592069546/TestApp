package com.example.administrator.recycler.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View

import java.util.ArrayList

import com.example.administrator.recycler.Adapter.PersonAdapter
import com.example.administrator.recycler.R
import com.example.administrator.recycler.data.Person
import kotlinx.android.synthetic.main.activity_person.*

class PersonActivity: AppCompatActivity(), View.OnClickListener{
    var mdata = ArrayList<Person>()
    val adapter by lazy { PersonAdapter(this@PersonActivity,mdata) }
    val person = Person()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person)

        mdata = person.initData(10)
        person.sortData(mdata)

        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
        recycler.layoutManager = LinearLayoutManager(this)
        add.setOnClickListener(this)
        add.setTextColor(ContextCompat.getColor(this, R.color.colorAccent))
        add.visibility = View.INVISIBLE
        del.setOnClickListener(this)
    }

    override fun onClick(view: View){
        when(view.id){
            R.id.add -> addItem()
            R.id.del -> delItem()
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