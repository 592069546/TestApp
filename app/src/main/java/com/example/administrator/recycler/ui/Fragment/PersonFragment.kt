package com.example.administrator.recycler.ui.Fragment

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import java.util.ArrayList

import com.example.administrator.recycler.Adapter.PersonAdapter
import com.example.administrator.recycler.R
import com.example.administrator.recycler.data.Person
import kotlinx.android.synthetic.main.fragment_person.*

class PersonFragment: LazyFragment(), View.OnClickListener{
    var mdata = ArrayList<Person>()
    val adapter by lazy { PersonAdapter(context!!,mdata) }
    val person = Person()

    override val layoutId = R.layout.fragment_person

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mdata = person.initData(10)
        person.sortData(mdata)
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
        recycler.layoutManager = LinearLayoutManager(context!!)
        add.setOnClickListener(this)
        add.setTextColor(ContextCompat.getColor(context!!, R.color.colorAccent))
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

    override fun onInvisible() {}

    override fun onVisible() {}
}