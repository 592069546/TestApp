package com.example.administrator.recycler.Adapter


import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.example.administrator.recycler.ui.Fragment.FlutterFragment
import com.example.administrator.recycler.ui.Fragment.PersonFragment

class MainViewPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm){

    private val mTitle = arrayOf("Flutter", "Person")

    val mFlutterFragment by lazy { FlutterFragment() }
    val mPersonFragment by lazy { PersonFragment() }


    override fun getItem(position: Int): Fragment = when (position) {
        0 -> mFlutterFragment
        1 -> mPersonFragment
        else -> mFlutterFragment //when作为表达式else为必须
    }

    override fun getCount(): Int = mTitle.size

    override fun getPageTitle(position: Int): CharSequence? = mTitle[position]
}