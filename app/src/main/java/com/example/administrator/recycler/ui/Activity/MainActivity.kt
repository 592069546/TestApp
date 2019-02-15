package com.example.administrator.recycler.ui.Activity

import android.os.Bundle
import com.example.administrator.recycler.Adapter.MainViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*

import com.example.administrator.recycler.R

class MainActivity : BaseActivity(){
    val mAdapter by lazy{MainViewPagerAdapter( supportFragmentManager)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_viewpager!!.adapter = mAdapter
        main_viewpager!!.offscreenPageLimit = 2
        main_viewpager!!.seItIntercept(true)
        tab_layout.setupWithViewPager(main_viewpager)

    }

}
