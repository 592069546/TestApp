package com.example.pagingtest.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.pagingtest.R
import com.example.pagingtest.room.User

/*TODO Paging*/
class RoomAdapter : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    private val userList = ArrayList<User>();

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RoomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_db, parent, false))

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val user = getList()[position]
        holder.tv_id.text = user.uid.toString()
        holder.tv_name.text = "${user.firstName} ${user.lastName}"
    }

    override fun getItemCount(): Int = userList.size

    fun submitList(userList: List<User>) {
        this.userList.clear()
        this.userList.addAll(userList)
        notifyDataSetChanged()
    }

    private fun getList() = userList

    class RoomViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tv_id: TextView by lazy { view.findViewById(R.id.tv_id) }
        val tv_name: TextView by lazy { view.findViewById(R.id.tv_name) }
        val tv_left_menu: TextView by lazy { view.findViewById(R.id.tv_left_menu) }

        init {
            tv_left_menu.setOnClickListener {
                Toast.makeText(it.context, "点击左边", Toast.LENGTH_SHORT).show()
            }
        }
    }
}