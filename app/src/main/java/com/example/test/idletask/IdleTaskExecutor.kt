package com.example.test.idletask

import android.os.Looper
import android.os.MessageQueue
import android.os.SystemClock
import android.util.Log
import java.util.ArrayDeque

/**
 * 空闲任务执行器
 * 
 * 利用 Android 的 IdleHandler 机制，在主线程空闲时执行任务
 * 主要特点：
 * 1. 支持普通任务和优先级任务
 * 2. 每次空闲时间片最多执行 5ms，避免阻塞主线程
 * 3. 自动管理 IdleHandler 的注册和注销
 * 4. 提供任务执行完成的回调
 * 
 * 使用场景：
 * - 延迟初始化非关键组件
 * - 预加载数据
 * - 执行不影响用户交互的后台任务
 * - 优化应用启动速度
 */
class IdleTaskExecutor {
    // 普通任务队列，使用双端队列实现，支持高效的头尾操作
    private val taskQueue = ArrayDeque<() -> Unit>()
    
    // 高优先级任务队列，会优先于普通任务执行
    private val priorityTaskQueue = ArrayDeque<() -> Unit>()
    
    // 标记 IdleHandler 是否已注册，避免重复注册
    private var isRegistered = false
    
    // 每次空闲时最多执行的时间（毫秒）
    // 设置为 5ms 是为了不影响 60fps（16.67ms per frame）的流畅度
    private var maxIdleTimeMs = 5L
    
    // 所有任务执行完成后的回调
    private var onQueueEmpty: (() -> Unit)? = null
    
    /**
     * IdleHandler 实现
     * 当主线程消息队列空闲时会被调用
     * 
     * 返回值：
     * - true: 保持 IdleHandler 继续监听下次空闲
     * - false: 移除 IdleHandler，不再监听
     */
    private val idleHandler = MessageQueue.IdleHandler {
        val startTime = SystemClock.uptimeMillis()
        var executedTasks = 0
        
        // 第一步：优先执行高优先级任务
        // 在时间限制内尽可能多地执行优先级任务
        while (priorityTaskQueue.isNotEmpty() && 
               SystemClock.uptimeMillis() - startTime < maxIdleTimeMs) {
            try {
                // 取出并执行队首任务
                priorityTaskQueue.removeFirst().invoke()
                executedTasks++
            } catch (e: Exception) {
                // 捕获异常避免崩溃，记录错误日志
                Log.e("IdleTaskExecutor", "Priority task failed", e)
            }
        }
        
        // 第二步：执行普通任务
        // 如果还有剩余时间，执行普通任务
        while (taskQueue.isNotEmpty() && 
               SystemClock.uptimeMillis() - startTime < maxIdleTimeMs) {
            try {
                taskQueue.removeFirst().invoke()
                executedTasks++
            } catch (e: Exception) {
                Log.e("IdleTaskExecutor", "Task failed", e)
            }
        }
        
        // 记录本次执行情况，便于性能分析
        val elapsedTime = SystemClock.uptimeMillis() - startTime
        Log.d("IdleTaskExecutor", "Executed $executedTasks tasks in ${elapsedTime}ms")
        
        // 检查是否还有待执行的任务
        val hasMoreTasks = taskQueue.isNotEmpty() || priorityTaskQueue.isNotEmpty()
        
        if (!hasMoreTasks) {
            // 所有任务执行完毕，标记为未注册状态
            isRegistered = false
            // 触发完成回调
            onQueueEmpty?.invoke()
        }
        
        // 返回值决定是否继续监听空闲事件
        hasMoreTasks
    }
    
    /**
     * 添加普通任务到队列
     * 
     * @param task 要执行的任务
     */
    fun addTask(task: () -> Unit) {
        taskQueue.offer(task)
        registerIfNeeded()
    }
    
    /**
     * 添加高优先级任务到队列
     * 高优先级任务会优先于普通任务执行
     * 
     * @param task 要执行的任务
     */
    fun addPriorityTask(task: () -> Unit) {
        priorityTaskQueue.offer(task)
        registerIfNeeded()
    }
    
    /**
     * 批量添加普通任务
     * 
     * @param tasks 任务列表
     */
    fun addTasks(tasks: List<() -> Unit>) {
        tasks.forEach { taskQueue.offer(it) }
        registerIfNeeded()
    }
    
    /**
     * 必要时注册 IdleHandler
     * 只在第一次添加任务时注册，避免重复注册
     */
    private fun registerIfNeeded() {
        if (!isRegistered) {
            Looper.myQueue().addIdleHandler(idleHandler)
            isRegistered = true
            Log.d("IdleTaskExecutor", "IdleHandler registered")
        }
    }
    
    /**
     * 设置所有任务完成后的回调
     * 
     * @param callback 完成回调
     */
    fun setOnQueueEmpty(callback: () -> Unit) {
        onQueueEmpty = callback
    }
    
    /**
     * 设置每次空闲时最多执行的时间
     * 
     * @param timeMs 时间限制（毫秒）
     */
    fun setMaxIdleTime(timeMs: Long) {
        maxIdleTimeMs = timeMs
    }
    
    /**
     * 清空所有待执行的任务
     * 同时注销 IdleHandler
     */
    fun clear() {
        taskQueue.clear()
        priorityTaskQueue.clear()
        if (isRegistered) {
            Looper.myQueue().removeIdleHandler(idleHandler)
            isRegistered = false
            Log.d("IdleTaskExecutor", "IdleHandler unregistered and tasks cleared")
        }
    }
    
    /**
     * 获取队列中待执行的任务总数
     * 
     * @return 普通任务和优先级任务的总数
     */
    fun getQueueSize(): Int = taskQueue.size + priorityTaskQueue.size
    
    /**
     * 获取普通任务队列大小
     */
    fun getNormalQueueSize(): Int = taskQueue.size
    
    /**
     * 获取优先级任务队列大小
     */
    fun getPriorityQueueSize(): Int = priorityTaskQueue.size
    
    /**
     * 检查是否有待执行的任务
     */
    fun hasPendingTasks(): Boolean = taskQueue.isNotEmpty() || priorityTaskQueue.isNotEmpty()
}