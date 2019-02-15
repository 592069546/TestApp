package com.example.administrator.recycler.Adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.PagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup

abstract class FragmentViewPagerAdapter(protected val fm: FragmentManager): FragmentPagerAdapter(fm){
    companion object {
        private val TAG = "FragmentViewPagerAdapter"

        //fun makeFragmentName(viewID: Int,id: Long) = "android:switcher:" + viewID + ":" + id

        fun foreUpdateFragment() = false
    }

    /*protected var mCurTransaction = fm.beginTransaction()

    abstract fun getItem(positon: Int): Fragment

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemID = getItemID(position)
        //判断是否存在fragment
        val name = makeFragmentName(container.id, itemID)
        var fragment = fm.findFragmentByTag(name)
        //强制调用getItem(position)方法获取新的fragment
        if (fragment != null && !foreUpdateFragment()){
            mCurTransaction.attach(fragment)
        }else {
            fragment = getItem(position)
            mCurTransaction.add(container.id, fragment, makeFragmentName(container.id, itemID))
        }
        Log.v(TAG, fragment.toString())
        return fragment
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return (p1 as Fragment).view == p0
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        mCurTransaction.detach(`object` as Fragment)
        //notifyDataChange时强制更新删除fragmentTransction中的fragment
        if (foreUpdateFragment()) {
            mCurTransaction.remove(`object`)
        }
    }

    override fun startUpdate(container: ViewGroup) {
        if (container.id == View.NO_ID) {
            throw IllegalStateException("ViewPager with adapter " + this
                    + " requires a view id")
        }
    }

    fun getItemID(position: Int) = position.toLong()*/


}