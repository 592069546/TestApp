package com.example.administrator.recycler.Adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.*

import com.example.administrator.recycler.R
import com.example.administrator.recycler.data.Person


class PersonAdapter(val context: Context, val mData: ArrayList<Person>): RecyclerView.Adapter<PersonAdapter.Holder>(){
    val mLayoutInflater = LayoutInflater.from(context)

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(p0: Holder, p1: Int) {
        p0.name.text = mData[p1].name
        p0.age.text = mData[p1].age.toString()
        p0.type.text = mData[p1].type.toString()
        if(mData[p1].type == 1){
            change(p0)
        }
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int) = Holder(mLayoutInflater.inflate(R.layout.item_view, p0, false))

    private fun change(holder: Holder){   //动态添加按钮
        val button = Button(this.context)
        button.text = "点一下"
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        //val lp = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        //lp.addRule(RelativeLayout.CENTER_HORIZONTAL)
        button.layoutParams = lp
        holder.layout.addView(button)
        button.setOnClickListener {
            Toast.makeText(this.context,"点了",Toast.LENGTH_LONG).show()
        }
    }

    fun addItem(){
        mData.add(Person(Random().nextInt(200).toString(), Random().nextInt(30), Random().nextInt(2)))
        notifyItemInserted(mData.size)
    }

    fun delItem(){
        if(mData.size != 0){
            val position = Random().nextInt(mData.size)
            mData.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    class Holder(val view: View): RecyclerView.ViewHolder(view){
        var name = view.findViewById<TextView>(R.id.item_name)
        var age = view.findViewById<TextView>(R.id.item_age)
        var type = view.findViewById<TextView>(R.id.item_type)
        var layout = view.findViewById<LinearLayout>(R.id.item_linear)
    }
}