package com.example.test.prioritytask

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import kotlin.random.Random

/**
 * 优先级任务调度演示Fragment
 * 
 * 功能说明：
 * 1. 可视化展示不同优先级任务的执行情况
 * 2. 对比普通顺序执行和优先级调度的差异
 * 3. 实时显示系统负载和性能指标
 * 4. 支持动态配置时间片参数
 * 
 * 演示场景：
 * - 模拟复杂数据处理场景
 * - 展示优先级调度的优势
 * - 分析任务执行效率
 * 
 * @author wangning
 * @date 2025-08-15
 */
class PriorityTaskDemoFragment : Fragment() {
    
    // ========== UI组件 ==========
    private lateinit var rootView: View
    private lateinit var btnStartPriority: Button
    private lateinit var btnStartNormal: Button
    private lateinit var btnClear: Button
    private lateinit var btnAddTasks: Button
    private lateinit var switchAdaptive: Switch
    
    private lateinit var tvStatus: TextView
    private lateinit var tvPerformance: TextView
    private lateinit var tvSystemLoad: TextView
    private lateinit var tvQueueStatus: TextView
    private lateinit var tvComparison: TextView
    
    private lateinit var progressBar: ProgressBar
    private lateinit var seekBarTimeSlice: SeekBar
    private lateinit var tvTimeSlice: TextView
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskExecutionAdapter
    
    private lateinit var spinnerPriority: Spinner
    private lateinit var btnAddCustomTask: Button
    private lateinit var etTaskCount: EditText
    
    // ========== 执行器 ==========
    private val priorityTaskSplitter = PriorityTaskSplitter()
    private val normalExecutor = NormalTaskExecutor()
    
    // ========== 监控器 ==========
    private val handler = Handler(Looper.getMainLooper())
    private val cpuMonitor = CpuUsageMonitor()
    private val memoryMonitor = MemoryMonitor()
    
    // ========== 统计数据 ==========
    private var priorityStartTime = 0L
    private var priorityEndTime = 0L
    private var normalStartTime = 0L
    private var normalEndTime = 0L
    private var totalTaskCount = 0
    private var completedTaskCount = 0
    
    // 任务执行记录
    private val executionLogs = mutableListOf<TaskLog>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_priority_task_demo, container, false)
        initViews()
        setupListeners()
        startMonitoring()
        return rootView
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews() {
        // 控制按钮
        btnStartPriority = rootView.findViewById(R.id.btn_start_priority)
        btnStartNormal = rootView.findViewById(R.id.btn_start_normal)
        btnClear = rootView.findViewById(R.id.btn_clear)
        btnAddTasks = rootView.findViewById(R.id.btn_add_tasks)
        switchAdaptive = rootView.findViewById(R.id.switch_adaptive)
        
        // 状态显示
        tvStatus = rootView.findViewById(R.id.tv_status)
        tvPerformance = rootView.findViewById(R.id.tv_performance)
        tvSystemLoad = rootView.findViewById(R.id.tv_system_load)
        tvQueueStatus = rootView.findViewById(R.id.tv_queue_status)
        tvComparison = rootView.findViewById(R.id.tv_comparison)
        
        // 进度和配置
        progressBar = rootView.findViewById(R.id.progress_bar)
        seekBarTimeSlice = rootView.findViewById(R.id.seekbar_time_slice)
        tvTimeSlice = rootView.findViewById(R.id.tv_time_slice)
        
        // 任务列表
        recyclerView = rootView.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskExecutionAdapter()
        recyclerView.adapter = taskAdapter
        
        // 自定义任务
        spinnerPriority = rootView.findViewById(R.id.spinner_priority)
        btnAddCustomTask = rootView.findViewById(R.id.btn_add_custom_task)
        etTaskCount = rootView.findViewById(R.id.et_task_count)
        
        // 设置优先级选择器
        val priorities = TaskPriority.values().map { it.name }
        spinnerPriority.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            priorities
        )
        spinnerPriority.setSelection(2) // 默认选择NORMAL
        
        // 设置时间片滑动条
        seekBarTimeSlice.max = 32
        seekBarTimeSlice.progress = 8
        tvTimeSlice.text = "时间片: 8ms"
        
        // 默认开启自适应
        switchAdaptive.isChecked = true
    }
    
    /**
     * 设置事件监听器
     */
    private fun setupListeners() {
        // 优先级执行按钮
        btnStartPriority.setOnClickListener {
            startPriorityExecution()
        }
        
        // 普通执行按钮
        btnStartNormal.setOnClickListener {
            startNormalExecution()
        }
        
        // 清空按钮
        btnClear.setOnClickListener {
            clearAll()
        }
        
        // 添加测试任务按钮
        btnAddTasks.setOnClickListener {
            addDemoTasks()
        }
        
        // 添加自定义任务按钮
        btnAddCustomTask.setOnClickListener {
            addCustomTasks()
        }
        
        // 自适应开关
        switchAdaptive.setOnCheckedChangeListener { _, isChecked ->
            updateTimeSliceConfig()
            showToast(if (isChecked) "自适应模式已开启" else "自适应模式已关闭")
        }
        
        // 时间片调节
        seekBarTimeSlice.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvTimeSlice.text = "时间片: ${progress}ms"
                if (fromUser) {
                    updateTimeSliceConfig()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    /**
     * 开始监控系统状态
     */
    private fun startMonitoring() {
        handler.post(object : Runnable {
            override fun run() {
                updateSystemStatus()
                updateQueueStatus()
                handler.postDelayed(this, 500) // 每500ms更新一次
            }
        })
    }
    
    /**
     * 更新系统状态显示
     */
    private fun updateSystemStatus() {
        val cpuUsage = cpuMonitor.getCurrentUsage()
        val memoryPressure = memoryMonitor.getMemoryPressure()
        
        tvSystemLoad.text = buildString {
            append("系统负载: ")
            append("CPU: ${cpuUsage.toInt()}% ")
            append("内存: ${(memoryPressure * 100).toInt()}%")
        }
        
        // 根据负载改变颜色
        tvSystemLoad.setTextColor(when {
            cpuUsage > 80 || memoryPressure > 0.8 -> Color.RED
            cpuUsage > 50 || memoryPressure > 0.5 -> Color.rgb(255, 165, 0) // Orange
            else -> Color.GREEN
        })
    }
    
    /**
     * 更新队列状态显示
     */
    private fun updateQueueStatus() {
        val queueStatus = priorityTaskSplitter.getQueueStatus()
        tvQueueStatus.text = buildString {
            append("队列状态: ")
            queueStatus.forEach { (priority, count) ->
                if (count > 0) {
                    append("${priority.name}:$count ")
                }
            }
            if (queueStatus.values.sum() == 0) {
                append("空闲")
            }
        }
    }
    
    /**
     * 更新时间片配置
     */
    private fun updateTimeSliceConfig() {
        val baseMs = seekBarTimeSlice.progress.toLong()
        val maxMs = (baseMs * 2).coerceAtMost(32)
        val adaptive = switchAdaptive.isChecked
        
        priorityTaskSplitter.setTimeSliceConfig(baseMs, maxMs, adaptive)
    }
    
    /**
     * 开始优先级执行
     */
    private fun startPriorityExecution() {
        clearAll()
        tvStatus.text = "状态: 正在使用优先级调度执行..."
        tvStatus.setTextColor(Color.BLUE)
        progressBar.visibility = View.VISIBLE
        
        priorityStartTime = System.currentTimeMillis()
        totalTaskCount = 0
        completedTaskCount = 0
        
        // 添加混合优先级任务
        addMixedPriorityTasks()
        
        // 延迟检查完成状态
        handler.postDelayed({
            checkPriorityCompletion()
        }, 100)
    }
    
    /**
     * 开始普通顺序执行
     */
    private fun startNormalExecution() {
        clearAll()
        tvStatus.text = "状态: 正在使用普通顺序执行..."
        tvStatus.setTextColor(Color.GRAY)
        progressBar.visibility = View.VISIBLE
        
        normalStartTime = System.currentTimeMillis()
        totalTaskCount = 0
        completedTaskCount = 0
        
        // 添加相同的任务到普通执行器
        addNormalTasks()
        
        // 开始执行
        normalExecutor.startExecution()
        
        // 延迟检查完成状态
        handler.postDelayed({
            checkNormalCompletion()
        }, 100)
    }
    
    /**
     * 添加混合优先级任务
     */
    private fun addMixedPriorityTasks() {
        // 1. 紧急任务：UI关键更新
        for (i in 1..3) {
            totalTaskCount++
            priorityTaskSplitter.addTask(
                priority = TaskPriority.URGENT,
                task = {
                    simulateWork(5) // 5ms
                    logTaskExecution("紧急任务 $i", TaskPriority.URGENT)
                },
                estimatedTimeMs = 5,
                onComplete = {
                    completedTaskCount++
                    updateProgress()
                }
            )
        }
        
        // 2. 高优先级任务：首屏数据
        for (i in 1..5) {
            totalTaskCount++
            priorityTaskSplitter.addTask(
                priority = TaskPriority.HIGH,
                task = {
                    simulateWork(10) // 10ms
                    logTaskExecution("高优先级任务 $i", TaskPriority.HIGH)
                },
                estimatedTimeMs = 10,
                onComplete = {
                    completedTaskCount++
                    updateProgress()
                }
            )
        }
        
        // 3. 普通任务：常规业务
        for (i in 1..10) {
            totalTaskCount++
            priorityTaskSplitter.addTask(
                priority = TaskPriority.NORMAL,
                task = {
                    simulateWork(8) // 8ms
                    logTaskExecution("普通任务 $i", TaskPriority.NORMAL)
                },
                estimatedTimeMs = 8,
                onComplete = {
                    completedTaskCount++
                    updateProgress()
                }
            )
        }
        
        // 4. 低优先级任务：后台处理
        for (i in 1..8) {
            totalTaskCount++
            priorityTaskSplitter.addTask(
                priority = TaskPriority.LOW,
                task = {
                    simulateWork(15) // 15ms
                    logTaskExecution("低优先级任务 $i", TaskPriority.LOW)
                },
                estimatedTimeMs = 15,
                onComplete = {
                    completedTaskCount++
                    updateProgress()
                }
            )
        }
        
        // 5. 空闲任务：清理优化
        for (i in 1..5) {
            totalTaskCount++
            priorityTaskSplitter.addTask(
                priority = TaskPriority.IDLE,
                task = {
                    simulateWork(20) // 20ms
                    logTaskExecution("空闲任务 $i", TaskPriority.IDLE)
                },
                estimatedTimeMs = 20,
                onComplete = {
                    completedTaskCount++
                    updateProgress()
                }
            )
        }
        
        tvStatus.text = "状态: 已添加 $totalTaskCount 个优先级任务"
    }
    
    /**
     * 添加普通任务（用于对比）
     */
    private fun addNormalTasks() {
        val tasks = mutableListOf<() -> Unit>()
        
        // 添加相同数量和类型的任务
        for (i in 1..3) {
            totalTaskCount++
            tasks.add {
                simulateWork(5)
                logTaskExecution("任务 ${tasks.size}", null)
                completedTaskCount++
                updateProgress()
            }
        }
        
        for (i in 1..5) {
            totalTaskCount++
            tasks.add {
                simulateWork(10)
                logTaskExecution("任务 ${tasks.size}", null)
                completedTaskCount++
                updateProgress()
            }
        }
        
        for (i in 1..10) {
            totalTaskCount++
            tasks.add {
                simulateWork(8)
                logTaskExecution("任务 ${tasks.size}", null)
                completedTaskCount++
                updateProgress()
            }
        }
        
        for (i in 1..8) {
            totalTaskCount++
            tasks.add {
                simulateWork(15)
                logTaskExecution("任务 ${tasks.size}", null)
                completedTaskCount++
                updateProgress()
            }
        }
        
        for (i in 1..5) {
            totalTaskCount++
            tasks.add {
                simulateWork(20)
                logTaskExecution("任务 ${tasks.size}", null)
                completedTaskCount++
                updateProgress()
            }
        }
        
        normalExecutor.addTasks(tasks)
        tvStatus.text = "状态: 已添加 $totalTaskCount 个普通任务"
    }
    
    /**
     * 添加演示任务
     */
    private fun addDemoTasks() {
        // 模拟真实场景：页面加载
        
        // 1. 紧急：显示加载动画
        priorityTaskSplitter.addUrgentTask {
            handler.post { 
                showToast("显示加载动画")
                logTaskExecution("显示加载动画", TaskPriority.URGENT)
            }
        }
        
        // 2. 高优先级：加载首屏数据
        for (i in 1..3) {
            priorityTaskSplitter.addHighPriorityTask {
                simulateWork(50)
                handler.post {
                    logTaskExecution("加载首屏数据 $i", TaskPriority.HIGH)
                }
            }
        }
        
        // 3. 普通：加载次要内容
        for (i in 1..5) {
            priorityTaskSplitter.addNormalTask {
                simulateWork(30)
                handler.post {
                    logTaskExecution("加载次要内容 $i", TaskPriority.NORMAL)
                }
            }
        }
        
        // 4. 低优先级：预加载图片
        for (i in 1..3) {
            priorityTaskSplitter.addLowPriorityTask {
                simulateWork(100)
                handler.post {
                    logTaskExecution("预加载图片 $i", TaskPriority.LOW)
                }
            }
        }
        
        // 5. 空闲：数据统计上报
        priorityTaskSplitter.addIdleTask {
            simulateWork(50)
            handler.post {
                logTaskExecution("数据统计上报", TaskPriority.IDLE)
            }
        }
        
        showToast("已添加演示任务")
    }
    
    /**
     * 添加自定义任务
     */
    private fun addCustomTasks() {
        val count = etTaskCount.text.toString().toIntOrNull() ?: 5
        val priority = TaskPriority.values()[spinnerPriority.selectedItemPosition]
        
        for (i in 1..count) {
            priorityTaskSplitter.addTask(
                priority = priority,
                task = {
                    val workTime = when (priority) {
                        TaskPriority.URGENT -> 5L
                        TaskPriority.HIGH -> 10L
                        TaskPriority.NORMAL -> 15L
                        TaskPriority.LOW -> 20L
                        TaskPriority.IDLE -> 30L
                    }
                    simulateWork(workTime)
                    handler.post {
                        logTaskExecution("${priority.name} 任务 $i", priority)
                    }
                },
                onComplete = {
                    handler.post {
                        updateQueueStatus()
                    }
                }
            )
        }
        
        showToast("已添加 $count 个 ${priority.name} 任务")
    }
    
    /**
     * 模拟任务执行
     */
    private fun simulateWork(durationMs: Long) {
        val endTime = System.currentTimeMillis() + durationMs
        while (System.currentTimeMillis() < endTime) {
            // 模拟CPU密集型任务
            for (i in 0..1000) {
                Math.sqrt(Random.nextDouble())
            }
        }
    }
    
    /**
     * 记录任务执行
     */
    private fun logTaskExecution(taskName: String, priority: TaskPriority?) {
        val log = TaskLog(
            name = taskName,
            priority = priority,
            timestamp = System.currentTimeMillis(),
            threadName = Thread.currentThread().name
        )
        
        handler.post {
            executionLogs.add(log)
            taskAdapter.addLog(log)
            
            // 滚动到最新
            if (executionLogs.size > 0) {
                recyclerView.smoothScrollToPosition(executionLogs.size - 1)
            }
        }
    }
    
    /**
     * 更新进度
     */
    private fun updateProgress() {
        handler.post {
            val progress = if (totalTaskCount > 0) {
                (completedTaskCount * 100) / totalTaskCount
            } else {
                0
            }
            progressBar.progress = progress
            
            tvStatus.text = "状态: 执行中 ($completedTaskCount/$totalTaskCount)"
        }
    }
    
    /**
     * 检查优先级执行完成
     */
    private fun checkPriorityCompletion() {
        if (completedTaskCount >= totalTaskCount && totalTaskCount > 0) {
            priorityEndTime = System.currentTimeMillis()
            val duration = priorityEndTime - priorityStartTime
            
            tvStatus.text = "优先级执行完成: ${duration}ms"
            tvStatus.setTextColor(Color.GREEN)
            progressBar.visibility = View.GONE
            
            // 显示性能统计
            tvPerformance.text = priorityTaskSplitter.getPerformanceStats()
            
            compareResults()
        } else {
            // 继续检查
            handler.postDelayed({
                checkPriorityCompletion()
            }, 100)
        }
    }
    
    /**
     * 检查普通执行完成
     */
    private fun checkNormalCompletion() {
        if (normalExecutor.isCompleted() || completedTaskCount >= totalTaskCount) {
            normalEndTime = System.currentTimeMillis()
            val duration = normalEndTime - normalStartTime
            
            tvStatus.text = "普通执行完成: ${duration}ms"
            tvStatus.setTextColor(Color.GREEN)
            progressBar.visibility = View.GONE
            
            compareResults()
        } else {
            // 继续检查
            handler.postDelayed({
                checkNormalCompletion()
            }, 100)
        }
    }
    
    /**
     * 对比执行结果
     */
    private fun compareResults() {
        if (priorityEndTime > 0 && normalEndTime > 0) {
            val priorityDuration = priorityEndTime - priorityStartTime
            val normalDuration = normalEndTime - normalStartTime
            
            val improvement = if (normalDuration > 0) {
                ((normalDuration - priorityDuration) * 100.0 / normalDuration).toInt()
            } else {
                0
            }
            
            tvComparison.text = buildString {
                appendLine("=== 执行对比 ===")
                appendLine("优先级调度: ${priorityDuration}ms")
                appendLine("普通顺序: ${normalDuration}ms")
                if (improvement > 0) {
                    appendLine("性能提升: $improvement%")
                    appendLine("优先级调度更快!")
                } else if (improvement < 0) {
                    appendLine("性能差异: ${-improvement}%")
                } else {
                    appendLine("性能相当")
                }
            }
            
            tvComparison.setTextColor(if (improvement > 0) Color.GREEN else Color.GRAY)
        }
    }
    
    /**
     * 清空所有任务和状态
     */
    private fun clearAll() {
        priorityTaskSplitter.clear()
        normalExecutor.clear()
        
        executionLogs.clear()
        taskAdapter.clearLogs()
        
        priorityStartTime = 0
        priorityEndTime = 0
        normalStartTime = 0
        normalEndTime = 0
        totalTaskCount = 0
        completedTaskCount = 0
        
        tvStatus.text = "状态: 就绪"
        tvStatus.setTextColor(Color.BLACK)
        tvPerformance.text = "性能统计: 暂无数据"
        tvComparison.text = "执行对比: 暂无数据"
        progressBar.visibility = View.GONE
        progressBar.progress = 0
    }
    
    /**
     * 显示Toast消息
     */
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        priorityTaskSplitter.clear()
        normalExecutor.clear()
    }
}

/**
 * 普通任务执行器（用于对比）
 * 简单的顺序执行，不考虑优先级
 */
class NormalTaskExecutor {
    private val tasks = mutableListOf<() -> Unit>()
    private val handler = Handler(Looper.getMainLooper())
    private var isExecuting = false
    private var currentIndex = 0
    
    fun addTasks(taskList: List<() -> Unit>) {
        tasks.addAll(taskList)
    }
    
    fun startExecution() {
        if (isExecuting) return
        isExecuting = true
        currentIndex = 0
        executeNext()
    }
    
    private fun executeNext() {
        if (currentIndex >= tasks.size) {
            isExecuting = false
            return
        }
        
        handler.post {
            // 执行当前任务
            try {
                tasks[currentIndex].invoke()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            currentIndex++
            
            // 继续下一个
            if (currentIndex < tasks.size) {
                executeNext()
            } else {
                isExecuting = false
            }
        }
    }
    
    fun isCompleted(): Boolean {
        return !isExecuting && currentIndex >= tasks.size
    }
    
    fun clear() {
        tasks.clear()
        currentIndex = 0
        isExecuting = false
        handler.removeCallbacksAndMessages(null)
    }
}

/**
 * 任务执行日志
 */
data class TaskLog(
    val name: String,
    val priority: TaskPriority?,
    val timestamp: Long,
    val threadName: String
)

/**
 * 任务执行记录适配器
 */
class TaskExecutionAdapter : RecyclerView.Adapter<TaskExecutionAdapter.ViewHolder>() {
    private val logs = mutableListOf<TaskLog>()
    
    fun addLog(log: TaskLog) {
        logs.add(log)
        notifyItemInserted(logs.size - 1)
    }
    
    fun clearLogs() {
        logs.clear()
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(logs[position])
    }
    
    override fun getItemCount() = logs.size
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(log: TaskLog) {
            text1.text = log.name
            text2.text = buildString {
                if (log.priority != null) {
                    append("[${log.priority.name}] ")
                }
                append("线程: ${log.threadName}")
            }
            
            // 根据优先级设置颜色
            val color = when (log.priority) {
                TaskPriority.URGENT -> Color.RED
                TaskPriority.HIGH -> Color.rgb(255, 165, 0) // Orange
                TaskPriority.NORMAL -> Color.BLUE
                TaskPriority.LOW -> Color.GRAY
                TaskPriority.IDLE -> Color.LTGRAY
                null -> Color.BLACK
            }
            text1.setTextColor(color)
        }
    }
}