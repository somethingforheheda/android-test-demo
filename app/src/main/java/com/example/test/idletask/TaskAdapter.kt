package com.example.test.idletask

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R

/**
 * 任务列表适配器
 * 
 * 用于在 RecyclerView 中展示任务列表，包括：
 * 1. 显示任务名称、类型、状态
 * 2. 根据状态显示不同的颜色
 * 3. 显示任务进度（针对复杂任务）
 * 4. 使用 DiffUtil 优化列表更新性能
 */
class TaskAdapter : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    
    // 任务列表数据
    private var tasks = listOf<TaskInfo>()
    
    /**
     * 更新任务列表
     * 使用 DiffUtil 计算差异，只更新变化的部分
     * 
     * @param newTasks 新的任务列表
     */
    fun updateTasks(newTasks: List<TaskInfo>) {
        val diffCallback = TaskDiffCallback(tasks, newTasks)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tasks = newTasks
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }
    
    override fun getItemCount(): Int = tasks.size
    
    /**
     * ViewHolder 类
     * 负责绑定和显示单个任务项
     */
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardTask)
        private val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        private val tvTaskStatus: TextView = itemView.findViewById(R.id.tvTaskStatus)
        private val tvTaskType: TextView = itemView.findViewById(R.id.tvTaskType)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBarTask)
        
        /**
         * 绑定任务数据到视图
         * 
         * @param task 任务信息
         */
        fun bind(task: TaskInfo) {
            // 设置任务名称（包含类型图标）
            tvTaskName.text = "${task.getTypeIcon()} ${task.name}"
            
            // 设置任务状态
            tvTaskStatus.text = task.getStatusText()
            tvTaskStatus.setTextColor(getStatusColor(task.status))
            
            // 设置任务类型标签
            tvTaskType.text = getTypeLabel(task.type)
            tvTaskType.setBackgroundColor(getTypeColor(task.type))
            
            // 设置进度条（仅在执行中的复杂任务时显示）
            if (task.type == TaskType.COMPLEX && task.status == TaskStatus.RUNNING) {
                progressBar.visibility = View.VISIBLE
                progressBar.progress = task.progress
            } else {
                progressBar.visibility = View.GONE
            }
            
            // 根据状态设置卡片背景色
            cardView.setCardBackgroundColor(getCardBackgroundColor(task.status))
            
            // 如果任务已完成，添加淡出动画提示
            if (task.status == TaskStatus.COMPLETED) {
                // 显示完成状态的3秒后开始淡出
                cardView.postDelayed({
                    cardView.animate()
                        .alpha(0.3f)
                        .setDuration(1500)
                        .start()
                }, 3000)
            } else {
                // 确保非完成状态的任务不透明
                cardView.alpha = 1.0f
            }
        }
        
        /**
         * 获取状态对应的文字颜色
         */
        private fun getStatusColor(status: TaskStatus): Int {
            return when (status) {
                TaskStatus.PENDING -> Color.parseColor("#FFA726")    // 橙色
                TaskStatus.RUNNING -> Color.parseColor("#42A5F5")    // 蓝色
                TaskStatus.COMPLETED -> Color.parseColor("#66BB6A")  // 绿色
                TaskStatus.FAILED -> Color.parseColor("#EF5350")     // 红色
                TaskStatus.CANCELLED -> Color.parseColor("#BDBDBD")  // 灰色
            }
        }
        
        /**
         * 获取任务类型的标签文本
         */
        private fun getTypeLabel(type: TaskType): String {
            return when (type) {
                TaskType.NORMAL -> "普通"
                TaskType.PRIORITY -> "优先"
                TaskType.BATCH -> "批量"
                TaskType.COMPLEX -> "复杂"
            }
        }
        
        /**
         * 获取任务类型对应的背景色
         */
        private fun getTypeColor(type: TaskType): Int {
            return when (type) {
                TaskType.NORMAL -> Color.parseColor("#E0E0E0")     // 浅灰
                TaskType.PRIORITY -> Color.parseColor("#FFE082")   // 金色
                TaskType.BATCH -> Color.parseColor("#B39DDB")      // 紫色
                TaskType.COMPLEX -> Color.parseColor("#FFAB91")    // 深橙
            }
        }
        
        /**
         * 获取卡片背景色（根据状态）
         */
        private fun getCardBackgroundColor(status: TaskStatus): Int {
            return when (status) {
                TaskStatus.PENDING -> Color.parseColor("#FFF8E1")     // 浅黄
                TaskStatus.RUNNING -> Color.parseColor("#E3F2FD")     // 浅蓝
                TaskStatus.COMPLETED -> Color.parseColor("#E8F5E9")   // 浅绿
                TaskStatus.FAILED -> Color.parseColor("#FFEBEE")      // 浅红
                TaskStatus.CANCELLED -> Color.parseColor("#FAFAFA")   // 浅灰
            }
        }
    }
    
    /**
     * DiffUtil 回调类
     * 用于高效计算列表差异
     */
    private class TaskDiffCallback(
        private val oldList: List<TaskInfo>,
        private val newList: List<TaskInfo>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 通过 ID 判断是否为同一个任务
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // 判断任务内容是否相同
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}