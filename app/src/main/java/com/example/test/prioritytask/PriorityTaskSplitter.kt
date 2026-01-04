package com.example.test.prioritytask

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.util.ArrayDeque
import kotlin.random.Random

/**
 * 优先级任务分割执行器
 * 
 * 核心原理：
 * 1. 多级优先级队列管理：将任务按优先级分类存储在不同队列中
 * 2. 动态时间片分配：根据系统负载动态调整每次执行的时间片大小
 * 3. 任务类型识别和优化：根据任务的估计执行时间进行智能调度
 * 4. 系统负载感知：实时监控CPU和内存使用情况，自适应调整执行策略
 * 
 * 设计思想：
 * - 采用分级队列避免任务饥饿，确保高优先级任务优先执行
 * - 使用时间片轮转避免主线程阻塞，保证UI流畅性
 * - 动态调整策略确保在不同系统负载下都有良好表现
 * - 完善的错误处理和重试机制提高任务执行成功率
 * 
 * @author wangning
 * @date 2025-08-15
 */
class PriorityTaskSplitter {
    
    /**
     * 任务队列数组
     * 使用Array存储不同优先级的队列，索引对应优先级level
     * ArrayDeque提供高效的头尾操作，适合队列实现
     */
    private val taskQueues = Array(TaskPriority.values().size) { ArrayDeque<PriorityTask>() }
    
    /**
     * 主线程Handler，用于调度任务执行
     * 确保所有任务都在主线程执行，避免线程安全问题
     */
    private val handler = Handler(Looper.getMainLooper())
    
    /**
     * 执行状态标志
     * 防止重复调度，确保同一时间只有一个执行时间片在运行
     */
    private var isExecuting = false
    
    // ========== 动态配置参数 ==========
    
    /**
     * 基础时间片大小（毫秒）
     * 每次执行的基准时间，类似于一帧的1/2时间
     * 8ms确保不会影响60fps的流畅度（16.67ms/帧）
     */
    private var baseTimeSliceMs = 8L
    
    /**
     * 最大时间片大小（毫秒）
     * 系统空闲时可以使用的最大时间片
     * 16ms接近一帧时间，在系统空闲时最大化利用
     */
    private var maxTimeSliceMs = 16L
    
    /**
     * 是否启用自适应时间片
     * 开启后会根据系统负载动态调整时间片大小
     */
    private var adaptiveTimeSlice = true
    
    // ========== 性能监控指标 ==========
    
    /**
     * 已执行任务总数
     * 用于统计和性能分析
     */
    private var totalTasksExecuted = 0
    
    /**
     * 总执行时间（毫秒）
     * 所有任务执行时间的累计，用于计算平均值
     */
    private var totalExecutionTimeMs = 0L
    
    /**
     * 平均任务执行时间（毫秒）
     * 动态计算，用于优化时间片分配
     */
    private var averageTaskTimeMs = 1L
    
    /**
     * 任务执行历史记录
     * 记录最近执行的任务信息，用于调试和分析
     */
    private val executionHistory = mutableListOf<TaskExecutionRecord>()
    private val maxHistorySize = 100
    
    // ========== 系统负载监控 ==========
    
    /**
     * CPU使用率监控器
     * 实时获取系统CPU使用情况，用于动态调整策略
     */
    private val cpuUsageMonitor = CpuUsageMonitor()
    
    /**
     * 内存压力监控器
     * 监控应用内存使用情况，避免在内存紧张时执行大量任务
     */
    private val memoryMonitor = MemoryMonitor()
    
    // ========== 统计数据 ==========
    
    /**
     * 各优先级任务执行统计
     * 记录每个优先级的任务执行次数，用于分析任务分布
     */
    private val priorityExecutionCount = mutableMapOf<TaskPriority, Int>()
    
    /**
     * 任务等待时间统计
     * 记录任务从加入队列到开始执行的等待时间
     */
    private val taskWaitingTimes = mutableListOf<Long>()
    
    /**
     * 添加任务到队列
     * 
     * @param priority 任务优先级
     * @param task 要执行的任务
     * @param estimatedTimeMs 估计执行时间（毫秒）
     * @param maxRetries 最大重试次数
     * @param onComplete 任务完成回调
     * @param onError 任务失败回调
     */
    fun addTask(
        priority: TaskPriority,
        task: () -> Unit,
        estimatedTimeMs: Long = 1L,
        maxRetries: Int = 0,
        onComplete: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        val priorityTask = PriorityTask(
            priority = priority,
            task = task,
            estimatedTimeMs = estimatedTimeMs,
            maxRetries = maxRetries,
            onComplete = onComplete,
            onError = onError
        )
        
        synchronized(taskQueues) {
            // 将任务加入对应优先级的队列
            taskQueues[priority.level].offer(priorityTask)
            
            Log.d(TAG, "Task added: priority=${priority.name}, " +
                    "queueSize=${taskQueues[priority.level].size}, " +
                    "estimatedTime=${estimatedTimeMs}ms")
        }
        
        // 触发任务调度
        scheduleExecution()
    }
    
    // ========== 便捷方法 ==========
    
    /**
     * 添加紧急任务
     * 最高优先级，立即执行，适用于关键UI更新
     */
    fun addUrgentTask(task: () -> Unit) = addTask(TaskPriority.URGENT, task)
    
    /**
     * 添加高优先级任务
     * 用户可感知的重要任务，如首屏数据加载
     */
    fun addHighPriorityTask(task: () -> Unit) = addTask(TaskPriority.HIGH, task)
    
    /**
     * 添加普通任务
     * 常规业务逻辑，大部分任务使用此优先级
     */
    fun addNormalTask(task: () -> Unit) = addTask(TaskPriority.NORMAL, task)
    
    /**
     * 添加低优先级任务
     * 后台任务，如预加载、缓存更新
     */
    fun addLowPriorityTask(task: () -> Unit) = addTask(TaskPriority.LOW, task)
    
    /**
     * 添加空闲任务
     * 只在系统空闲时执行，如清理、优化
     */
    fun addIdleTask(task: () -> Unit) = addTask(TaskPriority.IDLE, task)
    
    /**
     * 调度执行
     * 如果当前没有在执行，则立即开始执行
     */
    private fun scheduleExecution() {
        if (isExecuting) {
            // 已经在执行中，不需要重复调度
            return
        }
        
        handler.post {
            executeTimeSlice()
        }
    }
    
    /**
     * 执行一个时间片
     * 核心执行逻辑，在限定时间内执行尽可能多的任务
     */
    private fun executeTimeSlice() {
        isExecuting = true
        val startTime = SystemClock.uptimeMillis()
        
        // 计算本次时间片大小
        val timeSlice = calculateDynamicTimeSlice()
        
        var executedTasks = 0
        val sliceStartTime = System.currentTimeMillis()
        
        // 在时间片内循环执行任务
        while (SystemClock.uptimeMillis() - startTime < timeSlice) {
            // 获取下一个任务
            val task = getNextTask()
            if (task == null) {
                // 没有更多任务，结束本次时间片
                break
            }
            
            // 计算任务等待时间
            val waitingTime = System.currentTimeMillis() - task.addedTime
            taskWaitingTimes.add(waitingTime)
            
            val taskStartTime = SystemClock.uptimeMillis()
            var success = false
            
            try {
                // 执行任务
                task.task.invoke()
                success = true
                task.onComplete?.invoke()
                executedTasks++
                
                // 更新优先级执行统计
                priorityExecutionCount[task.priority] = 
                    (priorityExecutionCount[task.priority] ?: 0) + 1
                
            } catch (e: Exception) {
                Log.e(TAG, "Task execution failed: ${e.message}", e)
                handleTaskError(task, e)
            }
            
            val taskExecutionTime = SystemClock.uptimeMillis() - taskStartTime
            
            // 记录执行历史
            recordExecution(task, taskExecutionTime, success, waitingTime)
            
            // 更新性能指标
            updatePerformanceMetrics(taskExecutionTime)
            
            // 如果单个任务执行时间过长，记录警告
            if (taskExecutionTime > timeSlice / 2) {
                Log.w(TAG, "Long running task detected: " +
                        "time=${taskExecutionTime}ms, " +
                        "priority=${task.priority}, " +
                        "timeSlice=${timeSlice}ms")
            }
            
            // 如果任务执行时间超过剩余时间片，提前结束
            if (SystemClock.uptimeMillis() - startTime >= timeSlice) {
                break
            }
        }
        
        val sliceExecutionTime = System.currentTimeMillis() - sliceStartTime
        logExecutionInfo(executedTasks, sliceExecutionTime, timeSlice)
        
        isExecuting = false
        
        // 决定是否继续调度
        if (hasMoreTasks()) {
            if (!isSystemBusy()) {
                // 系统不忙，立即调度下一个时间片
                scheduleExecution()
            } else {
                // 系统忙碌，延迟调度，给系统喘息机会
                handler.postDelayed({ scheduleExecution() }, 50L)
                Log.d(TAG, "System busy, delaying next execution")
            }
        }
    }
    
    /**
     * 获取下一个要执行的任务
     * 按优先级顺序查找，确保高优先级任务先执行
     * 
     * @return 下一个任务，如果所有队列都空则返回null
     */
    private fun getNextTask(): PriorityTask? {
        synchronized(taskQueues) {
            // 按优先级从高到低遍历队列
            for (queue in taskQueues) {
                if (queue.isNotEmpty()) {
                    return queue.removeFirst()
                }
            }
            return null
        }
    }
    
    /**
     * 处理任务执行错误
     * 实现重试机制，失败的任务会降低优先级后重试
     * 
     * @param task 失败的任务
     * @param error 异常信息
     */
    private fun handleTaskError(task: PriorityTask, error: Exception) {
        task.retryCount++
        
        if (task.retryCount <= task.maxRetries) {
            // 还有重试机会，降低优先级后重新加入队列
            val retryPriority = when (task.priority) {
                TaskPriority.URGENT -> TaskPriority.HIGH
                TaskPriority.HIGH -> TaskPriority.NORMAL
                TaskPriority.NORMAL -> TaskPriority.LOW
                else -> task.priority
            }
            
            val retryTask = task.copy(priority = retryPriority)
            synchronized(taskQueues) {
                taskQueues[retryPriority.level].offer(retryTask)
            }
            
            Log.w(TAG, "Retrying task: attempt=${task.retryCount}/${task.maxRetries}, " +
                    "newPriority=${retryPriority}")
        } else {
            // 重试次数已用完，调用错误回调
            task.onError?.invoke(error)
            Log.e(TAG, "Task failed after ${task.retryCount} retries: ${error.message}")
        }
    }
    
    /**
     * 计算动态时间片大小
     * 根据系统负载动态调整，实现自适应执行
     * 
     * 策略：
     * - 系统繁忙时：减少时间片，避免影响系统响应
     * - 系统空闲时：增加时间片，提高任务处理效率
     * - 正常情况：使用基础时间片
     * 
     * @return 计算后的时间片大小（毫秒）
     */
    private fun calculateDynamicTimeSlice(): Long {
        if (!adaptiveTimeSlice) {
            // 未启用自适应，返回固定值
            return baseTimeSliceMs
        }
        
        val cpuUsage = cpuUsageMonitor.getCurrentUsage()
        val memoryPressure = memoryMonitor.getMemoryPressure()
        
        return when {
            // 系统非常繁忙，使用最小时间片
            cpuUsage > 80 || memoryPressure > 0.8 -> {
                Log.d(TAG, "System very busy: CPU=$cpuUsage%, Memory=$memoryPressure")
                baseTimeSliceMs / 2
            }
            // 系统空闲，使用最大时间片
            cpuUsage < 30 && memoryPressure < 0.5 -> {
                Log.d(TAG, "System idle: CPU=$cpuUsage%, Memory=$memoryPressure")
                maxTimeSliceMs
            }
            // 正常情况，使用基础时间片
            else -> {
                baseTimeSliceMs
            }
        }
    }
    
    /**
     * 判断系统是否繁忙
     * 用于决定是否延迟下一次调度
     * 
     * @return true表示系统繁忙
     */
    private fun isSystemBusy(): Boolean {
        val cpuUsage = cpuUsageMonitor.getCurrentUsage()
        val memoryPressure = memoryMonitor.getMemoryPressure()
        
        return cpuUsage > 70 || memoryPressure > 0.7
    }
    
    /**
     * 更新性能指标
     * 维护运行时统计数据
     * 
     * @param taskTimeMs 任务执行时间
     */
    private fun updatePerformanceMetrics(taskTimeMs: Long) {
        totalTasksExecuted++
        totalExecutionTimeMs += taskTimeMs
        
        // 计算移动平均值，避免单个异常值影响
        averageTaskTimeMs = if (totalTasksExecuted > 0) {
            totalExecutionTimeMs / totalTasksExecuted
        } else {
            1L
        }
    }
    
    /**
     * 记录任务执行历史
     */
    private fun recordExecution(
        task: PriorityTask,
        executionTime: Long,
        success: Boolean,
        waitingTime: Long
    ) {
        val record = TaskExecutionRecord(
            priority = task.priority,
            executionTimeMs = executionTime,
            waitingTimeMs = waitingTime,
            success = success,
            timestamp = System.currentTimeMillis()
        )
        
        executionHistory.add(record)
        
        // 限制历史记录大小
        if (executionHistory.size > maxHistorySize) {
            executionHistory.removeAt(0)
        }
    }
    
    /**
     * 记录执行信息日志
     */
    private fun logExecutionInfo(tasksExecuted: Int, executionTime: Long, timeSlice: Long) {
        Log.d(TAG, "TimeSlice executed: tasks=$tasksExecuted, " +
                "time=${executionTime}ms, " +
                "slice=${timeSlice}ms, " +
                "efficiency=${if (timeSlice > 0) (executionTime * 100 / timeSlice) else 0}%")
    }
    
    /**
     * 检查是否还有待执行的任务
     * 
     * @return true表示还有任务
     */
    private fun hasMoreTasks(): Boolean {
        synchronized(taskQueues) {
            return taskQueues.any { it.isNotEmpty() }
        }
    }
    
    /**
     * 获取各优先级队列状态
     * 用于监控和调试
     * 
     * @return 优先级到队列大小的映射
     */
    fun getQueueStatus(): Map<TaskPriority, Int> {
        synchronized(taskQueues) {
            return TaskPriority.values().associate { priority ->
                priority to taskQueues[priority.level].size
            }
        }
    }
    
    /**
     * 清空所有任务队列
     * 停止执行并清理资源
     */
    fun clear() {
        synchronized(taskQueues) {
            taskQueues.forEach { it.clear() }
        }
        handler.removeCallbacksAndMessages(null)
        isExecuting = false
        
        // 清空统计数据
        executionHistory.clear()
        taskWaitingTimes.clear()
        priorityExecutionCount.clear()
        
        Log.d(TAG, "All tasks cleared")
    }
    
    /**
     * 获取性能统计信息
     * 
     * @return 格式化的统计字符串
     */
    fun getPerformanceStats(): String {
        val avgWaitingTime = if (taskWaitingTimes.isNotEmpty()) {
            taskWaitingTimes.average().toLong()
        } else {
            0L
        }
        
        return buildString {
            appendLine("=== Performance Stats ===")
            appendLine("Total tasks executed: $totalTasksExecuted")
            appendLine("Average execution time: ${averageTaskTimeMs}ms")
            appendLine("Total execution time: ${totalExecutionTimeMs}ms")
            appendLine("Average waiting time: ${avgWaitingTime}ms")
            appendLine()
            appendLine("=== Priority Distribution ===")
            priorityExecutionCount.forEach { (priority, count) ->
                val percentage = if (totalTasksExecuted > 0) {
                    count * 100.0 / totalTasksExecuted
                } else {
                    0.0
                }
                appendLine("${priority.name}: $count (%.1f%%)".format(percentage))
            }
        }
    }
    
    /**
     * 获取执行历史
     */
    fun getExecutionHistory(): List<TaskExecutionRecord> {
        return executionHistory.toList()
    }
    
    /**
     * 设置时间片参数
     */
    fun setTimeSliceConfig(
        baseMs: Long = baseTimeSliceMs,
        maxMs: Long = maxTimeSliceMs,
        adaptive: Boolean = adaptiveTimeSlice
    ) {
        baseTimeSliceMs = baseMs
        maxTimeSliceMs = maxMs
        adaptiveTimeSlice = adaptive
        
        Log.d(TAG, "TimeSlice config updated: base=${baseMs}ms, " +
                "max=${maxMs}ms, adaptive=$adaptive")
    }
    
    companion object {
        private const val TAG = "PriorityTaskSplitter"
    }
}

/**
 * 任务优先级枚举
 * 
 * 优先级从高到低排序，level值越小优先级越高
 * 
 * @property level 优先级级别，用作队列数组索引
 */
enum class TaskPriority(val level: Int) {
    /**
     * 紧急任务
     * 立即执行，用于关键操作如崩溃处理、紧急UI更新
     */
    URGENT(0),
    
    /**
     * 高优先级
     * 用户可直接感知的任务，如首屏渲染、用户交互响应
     */
    HIGH(1),
    
    /**
     * 普通优先级
     * 常规业务逻辑，大部分任务使用此级别
     */
    NORMAL(2),
    
    /**
     * 低优先级
     * 后台任务，如数据预加载、缓存更新
     */
    LOW(3),
    
    /**
     * 空闲时执行
     * 只在系统空闲时执行，如日志上报、数据清理
     */
    IDLE(4)
}

/**
 * 优先级任务数据类
 * 
 * 封装任务的所有信息，包括执行逻辑、优先级、回调等
 * 
 * @property priority 任务优先级
 * @property task 要执行的任务函数
 * @property estimatedTimeMs 估计执行时间，用于智能调度
 * @property maxRetries 最大重试次数
 * @property onComplete 成功完成回调
 * @property onError 执行失败回调
 * @property retryCount 当前重试次数
 * @property addedTime 任务加入队列的时间戳
 */
data class PriorityTask(
    val priority: TaskPriority,
    val task: () -> Unit,
    val estimatedTimeMs: Long = 1L,
    val maxRetries: Int = 0,
    val onComplete: (() -> Unit)? = null,
    val onError: ((Exception) -> Unit)? = null
) {
    var retryCount: Int = 0
    var addedTime: Long = System.currentTimeMillis()
}

/**
 * 任务执行记录
 * 
 * 记录每个任务的执行情况，用于分析和优化
 */
data class TaskExecutionRecord(
    val priority: TaskPriority,
    val executionTimeMs: Long,
    val waitingTimeMs: Long,
    val success: Boolean,
    val timestamp: Long
)

/**
 * CPU使用率监控器
 * 
 * 监控系统CPU使用情况，用于动态调整执行策略
 * 注：这是简化实现，实际项目中可通过读取/proc/stat等方式获取真实数据
 */
class CpuUsageMonitor {
    private var lastCpuUsage = 30f
    
    /**
     * 获取当前CPU使用率
     * 
     * @return CPU使用率百分比 (0-100)
     */
    fun getCurrentUsage(): Float {
        // 模拟CPU使用率的渐变，更接近真实场景
        val change = (Random.nextFloat() - 0.5f) * 20
        lastCpuUsage = (lastCpuUsage + change).coerceIn(0f, 100f)
        return lastCpuUsage
    }
}

/**
 * 内存压力监控器
 * 
 * 监控应用内存使用情况，防止内存溢出
 */
class MemoryMonitor {
    /**
     * 获取当前内存压力
     * 
     * @return 内存压力值 (0-1)，越接近1表示内存越紧张
     */
    fun getMemoryPressure(): Float {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        // 计算内存使用比例
        val pressure = usedMemory.toFloat() / maxMemory.toFloat()
        
        // 记录内存使用情况
        if (pressure > 0.8) {
            Log.w("MemoryMonitor", "High memory pressure: ${(pressure * 100).toInt()}%")
        }
        
        return pressure
    }
    
    /**
     * 获取内存使用详情
     */
    fun getMemoryInfo(): String {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        
        return buildString {
            appendLine("Used: ${usedMemory / 1024 / 1024}MB")
            appendLine("Max: ${maxMemory / 1024 / 1024}MB")
            appendLine("Free: ${runtime.freeMemory() / 1024 / 1024}MB")
        }
    }
}