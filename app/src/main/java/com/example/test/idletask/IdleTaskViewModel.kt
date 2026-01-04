package com.example.test.idletask

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * IdleTask 演示的 ViewModel
 * 
 * 负责管理任务列表和执行状态，提供以下功能：
 * 1. 维护任务列表
 * 2. 跟踪任务执行状态
 * 3. 统计执行完成的任务数
 * 4. 监控队列大小
 * 
 * 使用 LiveData 确保 UI 能够自动响应数据变化
 */
class IdleTaskViewModel : ViewModel() {
    
    // 任务列表，使用 LiveData 以便 UI 能够观察变化
    private val _taskList = MutableLiveData<List<TaskInfo>>(emptyList())
    val taskList: LiveData<List<TaskInfo>> = _taskList
    
    // 已执行完成的任务数量
    private val _executedCount = MutableLiveData(0)
    val executedCount: LiveData<Int> = _executedCount
    
    // 当前队列中的任务数量
    private val _queueSize = MutableLiveData(0)
    val queueSize: LiveData<Int> = _queueSize
    
    // 用于延迟移除任务的 Handler
    private val handler = Handler(Looper.getMainLooper())
    private val removeTaskRunnables = mutableMapOf<Int, Runnable>()
    
    /**
     * 添加新任务到列表
     * 
     * @param task 任务信息
     */
    fun addTask(task: TaskInfo) {
        val currentList = _taskList.value ?: emptyList()
        _taskList.value = currentList + task
    }
    
    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 新的状态
     */
    fun updateTaskStatus(taskId: Int, status: TaskStatus) {
        val currentList = _taskList.value ?: return
        _taskList.value = currentList.map { task ->
            if (task.id == taskId) {
                val updatedTask = task.copy(status = status)
                
                // 如果任务完成，5秒后自动移除
                if (status == TaskStatus.COMPLETED) {
                    scheduleTaskRemoval(taskId)
                }
                
                updatedTask
            } else {
                task
            }
        }
    }
    
    /**
     * 更新任务进度（用于复杂任务）
     * 
     * @param taskId 任务ID
     * @param progress 进度值（0-100）
     */
    fun updateTaskProgress(taskId: Int, progress: Int) {
        val currentList = _taskList.value ?: return
        _taskList.value = currentList.map { task ->
            if (task.id == taskId) {
                task.copy(progress = progress)
            } else {
                task
            }
        }
    }
    
    /**
     * 增加已执行任务计数
     */
    fun incrementExecutedCount() {
        _executedCount.value = (_executedCount.value ?: 0) + 1
    }
    
    /**
     * 更新队列大小
     * 
     * @param size 新的队列大小
     */
    fun updateQueueSize(size: Int) {
        _queueSize.value = size
    }
    
    /**
     * 安排任务移除
     * 在任务完成后5秒自动移除
     * 
     * @param taskId 要移除的任务ID
     */
    private fun scheduleTaskRemoval(taskId: Int) {
        // 先取消之前的移除计划（如果有）
        removeTaskRunnables[taskId]?.let {
            handler.removeCallbacks(it)
        }
        
        // 创建新的移除任务
        val removeRunnable = Runnable {
            removeTask(taskId)
            removeTaskRunnables.remove(taskId)
        }
        
        removeTaskRunnables[taskId] = removeRunnable
        // 5秒后执行移除
        handler.postDelayed(removeRunnable, 5000)
    }
    
    /**
     * 移除指定任务
     * 
     * @param taskId 要移除的任务ID
     */
    private fun removeTask(taskId: Int) {
        val currentList = _taskList.value ?: return
        _taskList.value = currentList.filter { it.id != taskId }
    }
    
    /**
     * 清空所有任务
     */
    fun clearAllTasks() {
        // 取消所有待执行的移除任务
        removeTaskRunnables.values.forEach { handler.removeCallbacks(it) }
        removeTaskRunnables.clear()
        
        _taskList.value = emptyList()
        _executedCount.value = 0
        _queueSize.value = 0
    }
    
    /**
     * 获取指定状态的任务数量
     * 
     * @param status 任务状态
     * @return 该状态的任务数量
     */
    fun getTaskCountByStatus(status: TaskStatus): Int {
        return _taskList.value?.count { it.status == status } ?: 0
    }
    
    /**
     * 获取指定类型的任务数量
     * 
     * @param type 任务类型
     * @return 该类型的任务数量
     */
    fun getTaskCountByType(type: TaskType): Int {
        return _taskList.value?.count { it.type == type } ?: 0
    }
    
    override fun onCleared() {
        super.onCleared()
        // 清理 Handler 任务
        removeTaskRunnables.values.forEach { handler.removeCallbacks(it) }
        removeTaskRunnables.clear()
    }
}

/**
 * 任务信息数据类
 * 
 * @property id 任务唯一标识
 * @property name 任务名称
 * @property type 任务类型
 * @property status 任务状态
 * @property progress 任务进度（0-100），主要用于复杂任务
 * @property startTime 任务开始时间
 * @property endTime 任务结束时间
 */
data class TaskInfo(
    val id: Int,
    val name: String,
    val type: TaskType,
    val status: TaskStatus,
    val progress: Int = 0,
    val startTime: Long? = null,
    val endTime: Long? = null
) {
    /**
     * 获取任务执行时长（毫秒）
     * 
     * @return 执行时长，如果任务未完成则返回 null
     */
    fun getDuration(): Long? {
        return if (startTime != null && endTime != null) {
            endTime - startTime
        } else {
            null
        }
    }
    
    /**
     * 获取状态的显示文本
     */
    fun getStatusText(): String {
        return when (status) {
            TaskStatus.PENDING -> "等待中"
            TaskStatus.RUNNING -> "执行中"
            TaskStatus.COMPLETED -> "已完成"
            TaskStatus.FAILED -> "执行失败"
            TaskStatus.CANCELLED -> "已取消"
        }
    }
    
    /**
     * 获取类型的显示图标
     */
    fun getTypeIcon(): String {
        return when (type) {
            TaskType.NORMAL -> "📋"
            TaskType.PRIORITY -> "⭐"
            TaskType.BATCH -> "📦"
            TaskType.COMPLEX -> "🔧"
        }
    }
}

/**
 * 任务类型枚举
 */
enum class TaskType {
    NORMAL,     // 普通任务
    PRIORITY,   // 优先任务
    BATCH,      // 批量任务
    COMPLEX     // 复杂任务
}

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,    // 等待执行
    RUNNING,    // 正在执行
    COMPLETED,  // 执行完成
    FAILED,     // 执行失败
    CANCELLED   // 已取消
}