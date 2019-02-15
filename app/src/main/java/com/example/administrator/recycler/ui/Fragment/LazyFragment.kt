package com.example.administrator.recycler.ui.Fragment

abstract class LazyFragment: BaseFragment(){
    var isVisibled: Boolean = false //当前Fragment是否是可见状态
    var isStarted: Boolean = false //当前Fragment是否是Started状态
    var isCurrent: Boolean = false //当前Fragment是否是Adapter选中Item
    var isParentCurrent: Boolean = false //当前Fragment的父Fragment是否是其Adapter选中Item.
    var mVisibleListener: VisibleListener? = null

    fun update(isParentCurrent: Boolean) {
        this.isParentCurrent = isParentCurrent
        update()
    }

    fun setUserVisibleHint(isVisibleToUser: Boolean, isParentCurrent: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isCurrent = userVisibleHint
        this.isParentCurrent = isParentCurrent
        update()
    }

    private fun update() {
        if (isStarted && isCurrent && isParentCurrent) {
            if (!isVisibled) {
                isVisibled = true
                onVisible()
                if (mVisibleListener != null) {
                    mVisibleListener!!.onVisibleStateChanged(isVisibled)
                }
            }
        } else if (isVisibled) {
            isVisibled = false
            onInvisible()
            if (mVisibleListener != null) {
                mVisibleListener!!.onVisibleStateChanged(isVisibled)
            }

        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        setUserVisibleHint(isVisibleToUser, true)
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        update()
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
        update()
    }

    fun isFragmentVisible(): Boolean {
        return isVisibled
    }

    fun setVisibleListener(visibleListener: VisibleListener) {
        mVisibleListener = visibleListener
    }

    interface VisibleListener {
        fun onVisibleStateChanged(isVisibled: Boolean)
    }

    /**
     * onVisible 和 onInvisible中的 变量都应该在viewCreated时初始化好
     */
    abstract fun onVisible()

    abstract fun onInvisible()
}