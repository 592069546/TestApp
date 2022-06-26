package com.example.pagingtest.view

import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.abs

class ItemSwipeTouchHelper(@NonNull callback: Callback, @NonNull recyclerView: RecyclerView) :
    ItemTouchHelper(callback) {

    companion object {
        private val TAG = ItemSwipeTouchHelper::class.java.simpleName
    }

    private val rect = Rect()
    private val touchSlop = ViewConfiguration.get(recyclerView.context).scaledTouchSlop

    private var initMotionX = FloatArray(1)
    private var initMotionY = FloatArray(1)
    private var cancelUp = false
    private var pointersDown = 0
    private var pastSlopPointerId = MotionEvent.INVALID_POINTER_ID

    private var selected: RecyclerView.ViewHolder? = null

//    private val itemTouchListener = object : RecyclerView.OnItemTouchListener {
//        override fun onInterceptTouchEvent(rv: RecyclerView, ev: MotionEvent): Boolean {
//            when (ev.actionMasked) {
//                MotionEvent.ACTION_DOWN -> {
//                    pastSlopPointerId = MotionEvent.INVALID_POINTER_ID
//                    val pointerId = ev.getPointerId(0)
//                    saveInitialMotion(ev.x, ev.y, pointerId)
//                    val select = findTouchViewHolder(rv, ev.x.toInt(), ev.y.toInt())
//                    select(select)
//                }
//                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
//                    clearMotionHistory()
//                    if (cancelUp) {
//                        ev.action = MotionEvent.ACTION_CANCEL
//                        cancelUp = false
//                    }
//                    select(null)
//                }
//                MotionEvent.ACTION_POINTER_UP -> {
//                    clearMotionHistory(ev.getPointerId(ev.actionIndex))
//                }
//                MotionEvent.ACTION_POINTER_DOWN -> {
//                    val x = ev.getX(ev.actionIndex)
//                    val y = ev.getY(ev.actionIndex)
//                    saveInitialMotion(x, y, ev.getPointerId(ev.actionIndex))
//                    if (findOpenItems(rv).isNotEmpty()) {
//                        return false
//                    }
//                }
//            }
//            return selected != null
//        }
//
//        override fun onTouchEvent(rv: RecyclerView, ev: MotionEvent) {
//            var swipeLayout: SwipeLayout? = null
//            when (ev.actionMasked) {
//                MotionEvent.ACTION_MOVE -> {
//                    if (initMotionX.isNotEmpty()) {
//                        for (i in 0 until ev.pointerCount) {
//                            val pointerId = ev.getPointerId(i)
//                            if (!isValidPointerForActionMove(pointerId)) continue
//
//                            val x = ev.getX(i)
//                            val y = ev.getY(i)
//                            val dx = x - initMotionX[pointerId]
//                            val dy = y - initMotionY[pointerId]
//
//                            if (dx * dx + dy * dy > touchSlop * touchSlop) {
//                                cancelUp = false
//                            }
//
//                            if (pastSlopPointerId == MotionEvent.INVALID_POINTER_ID) {
//                                val touchItem = getTouchItem(rv, x.toInt(), y.toInt())
//                                swipeLayout = if (touchItem != null) {
//                                    findSwipeLayout(touchItem)
//                                } else null
//
//                                if (swipeLayout != null && swipeLayout.swipeEnable
//                                    && abs(dx) > touchSlop && abs(dx) > abs(dy)
//                                ) {
//                                    pastSlopPointerId = pointerId
//                                }
//                            } else if (pastSlopPointerId != MotionEvent.INVALID_POINTER_ID
//                                && pastSlopPointerId != pointerId
//                            ) {
//                                ev.action = MotionEvent.ACTION_CANCEL
//                            }
//                        }
//                    }
//                }
//            }
//            swipeLayout?.onTouchEvent(ev)
//        }
//
//        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
//
//        }
//    }
    private val itemTouchListener = object : RecyclerView.OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView, ev: MotionEvent): Boolean = true

    override fun onTouchEvent(rv: RecyclerView, ev: MotionEvent) {
        var swipeLayout: SwipeLayout? = null
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pastSlopPointerId = MotionEvent.INVALID_POINTER_ID
                val pointerId = ev.getPointerId(0)
                saveInitialMotion(ev.x, ev.y, pointerId)
                val touchItem = getTouchItem(rv, ev.x.toInt(), ev.y.toInt())
                if (touchItem != null)
                    swipeLayout = findSwipeLayout(touchItem)
                for (openItem in findOpenItems(rv)) {
                    if (openItem != touchItem) {
                        findSwipeLayout(openItem)?.closeMenu(true)
                        cancelUp = true
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val x = ev.getX(ev.actionIndex)
                val y = ev.getY(ev.actionIndex)
                saveInitialMotion(x, y, ev.getPointerId(ev.actionIndex))
                if (findOpenItems(rv).isNotEmpty()) {
                    return
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (initMotionX.isNotEmpty()) {
                    for (i in 0 until ev.pointerCount) {
                        val pointerId = ev.getPointerId(i)
                        if (!isValidPointerForActionMove(pointerId)) continue

                        val x = ev.getX(i)
                        val y = ev.getY(i)
                        val dx = x - initMotionX[pointerId]
                        val dy = y - initMotionY[pointerId]

                        if (dx * dx + dy * dy > touchSlop * touchSlop) {
                            cancelUp = false
                        }

                        if (pastSlopPointerId == MotionEvent.INVALID_POINTER_ID) {
                            val touchItem = getTouchItem(rv, x.toInt(), y.toInt())
                            swipeLayout = if (touchItem != null) {
                                findSwipeLayout(touchItem)
                            } else null

                            if (swipeLayout != null && swipeLayout.swipeEnable
                                && abs(dx) > touchSlop && abs(dx) > abs(dy)
                            ) {
                                pastSlopPointerId = pointerId
                            }
                        } else if (pastSlopPointerId != MotionEvent.INVALID_POINTER_ID
                            && pastSlopPointerId != pointerId
                        ) {
                            ev.action = MotionEvent.ACTION_CANCEL
                        }
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                clearMotionHistory(ev.getPointerId(ev.actionIndex))
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                clearMotionHistory()
                if (cancelUp) {
                    ev.action = MotionEvent.ACTION_CANCEL
                    cancelUp = false
                }
            }
        }
        swipeLayout?.onTouchEvent(ev)
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}

    init {
        attachToRecyclerView(recyclerView)
        recyclerView.addOnItemTouchListener(itemTouchListener)
    }

    private fun select(select: RecyclerView.ViewHolder?) {
        if (Objects.equals(selected, select)) {
            return
        }
//                    val touchItem = getTouchItem(rv, ev.x.toInt(), ev.y.toInt())
//                    for (openItem in findOpenItems(rv)) {
//                        if (openItem != touchItem) {
//                            findSwipeLayout(openItem)?.closeMenu(true)
//                            cancelUp = true
//                        }
//                    }
    }

    private fun saveInitialMotion(x: Float, y: Float, pointerId: Int) {
        ensureMotionHistorySizeForId(pointerId)
        initMotionX[pointerId] = x
        initMotionY[pointerId] = y
        pointersDown = pointersDown or (1 shl pointerId)
    }

    private fun ensureMotionHistorySizeForId(pointerId: Int) {
        if (initMotionX.size <= pointerId) {
            val imx = FloatArray(pointerId + 1)
            val imy = FloatArray(pointerId + 1)
            initMotionX = initMotionX.copyInto(imx)
            initMotionY = initMotionY.copyInto(imy)
        }
    }

    private fun isValidPointerForActionMove(pointerId: Int): Boolean {
        if (!isPointerDown(pointerId)) {
            Log.e(
                TAG, "Ignoring pointerId=$pointerId because ACTION_DOWN was not received "
                        + "for this pointer before ACTION_MOVE. It likely happened because "
                        + " SwipeMenuRecyclerView did not receive all the events in the event stream."
            )
            return false
        }
        return true
    }

    private fun isPointerDown(pointerId: Int): Boolean {
        return (pointersDown and (1 shl pointerId)) != 0
    }

    private fun clearMotionHistory(pointerId: Int) {
        if (initMotionX.isEmpty() || !isPointerDown(pointerId)) {
            return
        }
        initMotionX[pointerId] = 0f
        initMotionY[pointerId] = 0f
        pointersDown = pointersDown and (1 shl pointerId).inv()
    }

    private fun clearMotionHistory() {
        if (initMotionX.isEmpty()) {
            return
        }
        initMotionX.fill(0f)
        initMotionY.fill(0f)
        pointersDown = 0
    }

    private fun findTouchViewHolder(rv: RecyclerView, x: Int, y: Int): RecyclerView.ViewHolder? {
        val touchView = getTouchItem(rv, x, y)
        touchView ?: return null
        return rv.getChildViewHolder(touchView)
    }

    private fun getTouchItem(rv: RecyclerView, x: Int, y: Int): View? {
        for (i in 0 until rv.childCount) {
            val child = rv.getChildAt(i)
            if (child.visibility == RecyclerView.VISIBLE) {
                child.getHitRect(rect)
                if (rect.contains(x, y)) {
                    return child
                }
            }
        }
        return null
    }

    private fun findOpenItems(rv: RecyclerView): List<View> {
        val openItems = arrayListOf<View>()
        for (i in 0 until rv.childCount) {
            val child = rv.getChildAt(i)
            val swipeLayout = findSwipeLayout(child)
            if (swipeLayout != null && swipeLayout.onScreen > 0f) {
                openItems.add(child)
            }
        }
        return openItems
    }

    private fun findSwipeLayout(view: View): SwipeLayout? {
        if (view is SwipeLayout) {
            return view
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                return findSwipeLayout(view.getChildAt(i))
            }
        }
        return null
    }
}