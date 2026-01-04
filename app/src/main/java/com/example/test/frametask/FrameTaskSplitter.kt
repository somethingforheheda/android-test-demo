package com.example.test.frametask

import android.util.Log
import android.view.Choreographer

/**
 * 帧任务分割器
 * 
 * 利用 Choreographer 机制，将大量任务分散到多个帧中执行
 * 主要特点：
 * 1. 动态调整每帧执行的任务数量
 * 2. 监控帧执行时间，避免掉帧
 * 3. 提供暂停、恢复、取消功能
 * 4. 实时反馈执行进度和性能指标
 * 
 * 使用场景：
 * - 批量图片处理
 * - 大量数据解析
 * - 复杂计算任务
 * - UI 元素批量更新
 */
class FrameTaskSplitter {
    private val choreographer = Choreographer.getInstance()
    private val tasks = mutableListOf<() -> Unit>()
    private var currentIndex = 0
    private var isExecuting = false
    private var frameTargetTimeMs = 16L // 目标帧时间
    private var maxTasksPerFrame = 50 // 每帧最多执行的任务数
    
    private var onProgress: ((Int, Int) -> Unit)? = null
    private var onComplete: (() -> Unit)? = null
    private var onFrameInfo: ((Long, Int) -> Unit)? = null // 帧信息回调
    
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isExecuting) return
            
            val frameStartTime = System.nanoTime()
            val frameDeadlineNanos = frameStartTime + frameTargetTimeMs * 1_000_000L
            var executedInFrame = 0
            
            // 在帧时间内尽可能多执行任务
            while (currentIndex < tasks.size && 
                   executedInFrame < maxTasksPerFrame) {
                
                val currentTime = System.nanoTime()
                
                // 检查是否超过帧预算时间
                if (currentTime >= frameDeadlineNanos) {
                    break
                }
                
                try {
                    tasks[currentIndex]()
                    executedInFrame++
                } catch (e: Exception) {
                    Log.e("FrameTaskSplitter", "Task execution failed at index $currentIndex", e)
                }
                
                currentIndex++
            }
            
            val frameEndTime = System.nanoTime()
            val frameExecutionTimeMs = (frameEndTime - frameStartTime) / 1_000_000L
            
            // 帧信息回调
            onFrameInfo?.invoke(frameExecutionTimeMs, executedInFrame)
            
            // 进度回调
            onProgress?.invoke(currentIndex, tasks.size)
            
            // 动态调整策略
            adjustExecutionStrategy(frameExecutionTimeMs, executedInFrame)
            
            if (currentIndex < tasks.size) {
                // 继续下一帧
                choreographer.postFrameCallback(this)
            } else {
                // 任务完成
                isExecuting = false
                onComplete?.invoke()
            }
        }
    }
    
    /**
     * 动态调整执行策略
     * 根据实际帧执行时间调整每帧任务数
     */
    private fun adjustExecutionStrategy(frameTimeMs: Long, executedTasks: Int) {
        when {
            frameTimeMs > frameTargetTimeMs * 0.8 -> {
                // 帧时间超过80%，减少每帧任务数
                maxTasksPerFrame = maxOf(1, (maxTasksPerFrame * 0.8).toInt())
                Log.d("FrameTaskSplitter", "Decreasing tasks per frame to $maxTasksPerFrame")
            }
            frameTimeMs < frameTargetTimeMs * 0.5 && executedTasks == maxTasksPerFrame -> {
                // 帧时间很短且任务执行完毕，可以增加每帧任务数
                maxTasksPerFrame = minOf(100, (maxTasksPerFrame * 1.2).toInt())
                Log.d("FrameTaskSplitter", "Increasing tasks per frame to $maxTasksPerFrame")
            }
        }
    }
    
    /**
     * 执行任务列表
     * 
     * @param taskList 要执行的任务列表
     * @param onProgress 进度回调 (已完成数, 总数)
     * @param onComplete 完成回调
     * @param onFrameInfo 帧信息回调 (帧时间ms, 本帧执行任务数)
     */
    fun executeTasks(
        taskList: List<() -> Unit>,
        onProgress: ((Int, Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onFrameInfo: ((frameTimeMs: Long, tasksExecuted: Int) -> Unit)? = null
    ) {
        if (isExecuting) {
            cancel() // 取消之前的执行
        }
        
        tasks.clear()
        tasks.addAll(taskList)
        currentIndex = 0
        isExecuting = true
        maxTasksPerFrame = 50 // 重置
        
        this.onProgress = onProgress
        this.onComplete = onComplete
        this.onFrameInfo = onFrameInfo
        
        choreographer.postFrameCallback(frameCallback)
    }
    
    /**
     * 取消执行
     */
    fun cancel() {
        isExecuting = false
        choreographer.removeFrameCallback(frameCallback)
        tasks.clear()
        currentIndex = 0
        Log.d("FrameTaskSplitter", "Task execution cancelled")
    }
    
    /**
     * 暂停执行
     */
    fun pause() {
        if (isExecuting) {
            choreographer.removeFrameCallback(frameCallback)
            Log.d("FrameTaskSplitter", "Task execution paused at index $currentIndex")
        }
    }
    
    /**
     * 恢复执行
     */
    fun resume() {
        if (isExecuting && currentIndex < tasks.size) {
            choreographer.postFrameCallback(frameCallback)
            Log.d("FrameTaskSplitter", "Task execution resumed from index $currentIndex")
        }
    }
    
    /**
     * 获取当前每帧最大任务数
     */
    fun getMaxTasksPerFrame() = maxTasksPerFrame
    
    /**
     * 设置目标帧时间
     * 
     * @param targetMs 目标帧时间（毫秒）
     */
    fun setFrameTargetTime(targetMs: Long) {
        frameTargetTimeMs = targetMs
    }
}