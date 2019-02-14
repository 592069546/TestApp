package com.example.administrator.recycler.data

import java.util.*
import kotlin.collections.ArrayList

class Person(val name: String ?= null,val age: Int ?= null,val type: Int ?= null){

    fun initData(num: Int): ArrayList<Person>{
        val mdata = ArrayList<Person>(num)
        val type = 6
        val age = 30
        /*mdata.add(Person("sss", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("aaa", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("ddd", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("www", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("qqq", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("dsa", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("asd", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("sad", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("das", Random().nextInt(30), Random().nextInt(type)))
        mdata.add(Person("sda", Random().nextInt(30), Random().nextInt(type)))*/
        for(i in 0..num){
            mdata.add(Person(Name(),Random().nextInt(age),Random().nextInt(type)))
        }
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
        //降序排序
        //mdata.sortBy { it.type != 1}
        //根据多个条件来排序，可用sortWith,根据compareBy条件顺序排序
        mdata.sortWith(compareBy({it.type != 1} , {it.age}))
        return mdata
    }

    private fun Name(): String{
        return  (Random().nextInt(26) + 97).toChar().toString() +
                (Random().nextInt(26) + 97).toChar() +
                (Random().nextInt(26) + 97).toChar()
    }

}

