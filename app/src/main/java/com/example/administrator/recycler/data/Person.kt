package com.example.administrator.recycler.data

import java.util.*
import kotlin.collections.ArrayList

class Person(val name: String, val age: Int, val type: Int){
    companion object {
        fun initData(): ArrayList<Person>{
            val mdata = ArrayList<Person>()
            mdata.add(Person("sss", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("aaa", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("ddd", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("www", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("qqq", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("dsa", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("asd", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("sad", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("das", Random().nextInt(30), Random().nextInt(2)))
            mdata.add(Person("sda", Random().nextInt(30), Random().nextInt(2)))
            return mdata
        }

        fun sortData(mdata: ArrayList<Person>): ArrayList<Person>{
            /*var j = 0
            for(i in 0 until mdata.size ){
                if(mdata[i].type == 1){
                    mdata.add(j,mdata.removeAt(i))
                    j++
                }
            }*/
            mdata.sortBy { it.type != 1}
            return mdata
        }
    }
}