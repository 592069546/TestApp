package com.example.pagingtest.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.pagingtest.R
import com.example.pagingtest.room.User

/*TODO Paging*/
class RoomAdapter : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid

        override fun areContentsTheSame(oldItem: User, newItem: User) =
            oldItem.firstName == newItem.firstName && oldItem.lastName == newItem.lastName

    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RoomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_db, parent, false))

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val user = getList()[position]
        holder.tv_id.text = user.uid.toString()
        holder.tv_name.text = "${user.firstName} ${user.lastName}"
    }

    override fun getItemCount(): Int = getList().size

    fun submitList(userList: List<User>) {
        differ.submitList(userList)
    }

    private fun getList() = differ.currentList

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