package com.example.test.mvp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.mvp.model.User

/**
 * MVP架构演示 - 用户列表适配器
 * 用于在RecyclerView中显示用户列表数据
 */
class UserListAdapter : RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {
    
    // 用户列表数据
    private var userList: List<User> = emptyList()
    
    // 点击事件监听器
    private var onItemClickListener: ((User) -> Unit)? = null
    
    /**
     * 更新用户列表数据
     * @param users 新的用户列表
     */
    fun updateUsers(users: List<User>) {
        this.userList = users
        notifyDataSetChanged() // 通知适配器数据已更改
    }
    
    /**
     * 设置条目点击监听器
     * @param listener 点击事件监听器
     */
    fun setOnItemClickListener(listener: (User) -> Unit) {
        this.onItemClickListener = listener
    }
    
    /**
     * 创建ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    /**
     * 绑定数据到ViewHolder
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(user)
        }
    }
    
    /**
     * 获取列表项数量
     */
    override fun getItemCount(): Int = userList.size
    
    /**
     * 用户信息ViewHolder
     * 持有并管理每个列表项的视图
     */
    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        // 获取视图组件
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        private val tvAgePhone: TextView = itemView.findViewById(R.id.tv_age_phone)
        private val ivAvatar: View = itemView.findViewById(R.id.iv_avatar)
        
        /**
         * 绑定用户数据到视图
         * @param user 用户数据
         */
        fun bind(user: User) {
            // 设置用户姓名
            tvName.text = user.name
            
            // 设置用户邮箱
            tvEmail.text = user.email
            
            // 设置年龄和手机号（隐藏部分手机号）
            val phoneDisplay = user.phone?.let { phone ->
                if (phone.length >= 11) {
                    "${phone.substring(0, 3)}****${phone.substring(7)}"
                } else {
                    phone
                }
            } ?: "未提供"
            
            tvAgePhone.text = "年龄：${user.age} | 手机：$phoneDisplay"
            
            // 设置头像背景色（根据用户ID生成不同颜色）
            val colors = listOf(
                "#FF5722", "#E91E63", "#9C27B0", "#673AB7",
                "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
                "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
                "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
            )
            
            val colorIndex = user.id.hashCode().absoluteValue % colors.size
            val colorString = colors[colorIndex]
            ivAvatar.setBackgroundColor(android.graphics.Color.parseColor(colorString))
        }
        
        /**
         * 获取绝对值（处理负数）
         */
        private val Int.absoluteValue: Int
            get() = if (this < 0) -this else this
    }
}