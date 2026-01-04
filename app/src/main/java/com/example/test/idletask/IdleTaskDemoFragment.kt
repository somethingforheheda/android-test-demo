package com.example.test.idletask

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * IdleTaskExecutor 使用示例 Fragment
 * 
 * 展示了 IdleTaskExecutor 的各种使用场景：
 * 1. 添加普通任务和优先级任务
 * 2. 模拟耗时操作（数据预加载、图片解码、数据库查询等）
 * 3. 实时显示任务执行状态
 * 4. 任务完成回调
 * 
 * 实际应用场景：
 * - 应用启动时的非关键组件初始化
 * - 列表滑动停止后预加载下一页数据
 * - 后台预处理用户可能需要的数据
 * - 延迟加载统计、广告等非核心功能
 */
class IdleTaskDemoFragment : Fragment() {
    
    // IdleTaskExecutor 实例
    private lateinit var idleTaskExecutor: IdleTaskExecutor
    
    // ViewModel 用于管理任务状态
    private lateinit var viewModel: IdleTaskViewModel
    
    // UI 组件
    private lateinit var tvStatus: TextView
    private lateinit var tvQueueSize: TextView
    private lateinit var tvExecutedCount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var llCompletionStatus: LinearLayout
    private lateinit var tvCompletionMessage: TextView
    private lateinit var tvExecutionLogs: TextView
    private lateinit var scrollViewLogs: ScrollView
    
    // 任务计数器
    private var taskIdCounter = 0
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_idle_task_demo, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this).get(IdleTaskViewModel::class.java)
        
        // 初始化 IdleTaskExecutor
        initIdleTaskExecutor()
        
        // 初始化 UI
        initViews(view)
        
        // 观察 ViewModel 数据变化
        observeViewModel()
        
        // 设置按钮点击事件
        setupButtons(view)
    }
    
    /**
     * 初始化 IdleTaskExecutor
     */
    private fun initIdleTaskExecutor() {
        idleTaskExecutor = IdleTaskExecutor()
        
        // 设置最大空闲执行时间为 10ms（可根据需求调整）
        idleTaskExecutor.setMaxIdleTime(10L)
        
        // 设置所有任务完成的回调
        idleTaskExecutor.setOnQueueEmpty {
            activity?.runOnUiThread {
                tvStatus.text = "所有任务执行完成 ✅"
                progressBar.visibility = View.GONE
                
                // 显示完成状态视图，带动画效果
                llCompletionStatus.apply {
                    visibility = View.VISIBLE
                    alpha = 0f
                    animate()
                        .alpha(1f)
                        .setDuration(500)
                        .start()
                }
                
                // 更新完成消息
                val executedCount = viewModel.executedCount.value ?: 0
                tvCompletionMessage.text = "所有 $executedCount 个任务已成功完成！"
                
                // 5秒后自动隐藏完成状态
                llCompletionStatus.postDelayed({
                    llCompletionStatus.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction {
                            llCompletionStatus.visibility = View.GONE
                        }
                        .start()
                }, 5000)
            }
        }
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews(view: View) {
        tvStatus = view.findViewById(R.id.tvStatus)
        tvQueueSize = view.findViewById(R.id.tvQueueSize)
        tvExecutedCount = view.findViewById(R.id.tvExecutedCount)
        progressBar = view.findViewById(R.id.progressBar)
        llCompletionStatus = view.findViewById(R.id.llCompletionStatus)
        tvCompletionMessage = view.findViewById(R.id.tvCompletionMessage)
        tvExecutionLogs = view.findViewById(R.id.tvExecutionLogs)
        scrollViewLogs = view.findViewById(R.id.scrollViewLogs)
        
        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewTasks)
        recyclerView.layoutManager = LinearLayoutManager(context)
        taskAdapter = TaskAdapter()
        recyclerView.adapter = taskAdapter
        
        // 初始状态
        tvStatus.text = "空闲任务执行器就绪"
        progressBar.visibility = View.GONE
        tvExecutionLogs.text = "等待任务执行..."
    }
    
    /**
     * 观察 ViewModel 中的数据变化
     */
    private fun observeViewModel() {
        // 观察任务列表变化
        viewModel.taskList.observe(viewLifecycleOwner) { tasks ->
            taskAdapter.updateTasks(tasks)
        }
        
        // 观察执行完成的任务数
        viewModel.executedCount.observe(viewLifecycleOwner) { count ->
            tvExecutedCount.text = "已执行: $count 个任务"
        }
        
        // 观察队列大小
        viewModel.queueSize.observe(viewLifecycleOwner) { size ->
            tvQueueSize.text = "队列中: $size 个任务"
        }
    }
    
    /**
     * 设置按钮点击事件
     */
    private fun setupButtons(view: View) {
        // 添加普通任务按钮
        view.findViewById<Button>(R.id.btnAddNormalTask).setOnClickListener {
            addNormalTask()
        }
        
        // 添加优先级任务按钮
        view.findViewById<Button>(R.id.btnAddPriorityTask).setOnClickListener {
            addPriorityTask()
        }
        
        // 批量添加任务按钮
        view.findViewById<Button>(R.id.btnAddBatchTasks).setOnClickListener {
            addBatchTasks()
        }
        
        // 模拟复杂任务按钮
        view.findViewById<Button>(R.id.btnAddComplexTask).setOnClickListener {
            addComplexTask()
        }
        
        // 清空任务按钮
        view.findViewById<FloatingActionButton>(R.id.fabClearTasks).setOnClickListener {
            clearAllTasks()
        }
    }
    
    /**
     * 添加普通任务
     * 模拟一般优先级的后台任务
     */
    private fun addNormalTask() {
        val taskId = ++taskIdCounter
        val taskName = "普通任务 #$taskId"
        
        // 创建任务信息
        val taskInfo = TaskInfo(
            id = taskId,
            name = taskName,
            type = TaskType.NORMAL,
            status = TaskStatus.PENDING
        )
        viewModel.addTask(taskInfo)
        
        // 添加任务到执行器
        idleTaskExecutor.addTask {
            // 更新任务状态为执行中
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.RUNNING)
                tvStatus.text = "正在执行: $taskName"
                appendLog("▶ 开始执行: $taskName")
            }
            
            // 模拟任务执行（如：数据处理、网络请求等）
            simulateWork(50 + (Math.random() * 100).toLong())
            
            // 更新任务状态为完成
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                viewModel.incrementExecutedCount()
                updateQueueSizeDisplay()
                appendLog("✓ 完成: $taskName")
            }
        }
        
        updateQueueSizeDisplay()
        progressBar.visibility = View.VISIBLE
        // 隐藏完成状态（如果正在显示）
        llCompletionStatus.visibility = View.GONE
    }
    
    /**
     * 添加优先级任务
     * 模拟需要优先处理的任务，如用户即将查看的内容预加载
     */
    private fun addPriorityTask() {
        val taskId = ++taskIdCounter
        val taskName = "优先任务 #$taskId ⭐"
        
        val taskInfo = TaskInfo(
            id = taskId,
            name = taskName,
            type = TaskType.PRIORITY,
            status = TaskStatus.PENDING
        )
        viewModel.addTask(taskInfo)
        
        idleTaskExecutor.addPriorityTask {
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.RUNNING)
                tvStatus.text = "优先执行: $taskName"
                appendLog("⭐ 优先执行: $taskName")
            }
            
            // 模拟优先任务（通常执行时间较短）
            simulateWork(30 + (Math.random() * 50).toLong())
            
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                viewModel.incrementExecutedCount()
                updateQueueSizeDisplay()
            }
        }
        
        updateQueueSizeDisplay()
        progressBar.visibility = View.VISIBLE
        llCompletionStatus.visibility = View.GONE
    }
    
    /**
     * 批量添加任务
     * 模拟应用启动时的多个初始化任务
     */
    private fun addBatchTasks() {
        val tasks = mutableListOf<() -> Unit>()
        val batchSize = 5
        
        for (i in 1..batchSize) {
            val taskId = ++taskIdCounter
            val taskName = "批量任务 #$taskId"
            
            val taskInfo = TaskInfo(
                id = taskId,
                name = taskName,
                type = TaskType.BATCH,
                status = TaskStatus.PENDING
            )
            viewModel.addTask(taskInfo)
            
            tasks.add {
                activity?.runOnUiThread {
                    viewModel.updateTaskStatus(taskId, TaskStatus.RUNNING)
                    tvStatus.text = "批量执行: $taskName"
                    appendLog("📦 批量执行: $taskName")
                }
                
                // 批量任务通常较轻量
                simulateWork(20 + (Math.random() * 30).toLong())
                
                activity?.runOnUiThread {
                    viewModel.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                    viewModel.incrementExecutedCount()
                    updateQueueSizeDisplay()
                }
            }
        }
        
        idleTaskExecutor.addTasks(tasks)
        updateQueueSizeDisplay()
        progressBar.visibility = View.VISIBLE
        llCompletionStatus.visibility = View.GONE
        appendLog("📋 批量添加了 $batchSize 个任务")
    }
    
    /**
     * 添加复杂任务
     * 模拟需要较长时间的任务，如图片处理、数据库操作等
     */
    private fun addComplexTask() {
        val taskId = ++taskIdCounter
        val taskName = "复杂任务 #$taskId 🔧"
        
        val taskInfo = TaskInfo(
            id = taskId,
            name = taskName,
            type = TaskType.COMPLEX,
            status = TaskStatus.PENDING,
            progress = 0
        )
        viewModel.addTask(taskInfo)
        
        idleTaskExecutor.addTask {
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.RUNNING)
                tvStatus.text = "处理复杂任务: $taskName"
                appendLog("🔧 开始复杂任务: $taskName")
            }
            
            // 模拟分步骤的复杂任务
            for (step in 1..5) {
                simulateWork(30)
                val progress = step * 20
                activity?.runOnUiThread {
                    viewModel.updateTaskProgress(taskId, progress)
                    tvStatus.text = "复杂任务进度: $progress%"
                    if (progress == 100) {
                        appendLog("✓ 完成: $taskName")
                    }
                }
            }
            
            activity?.runOnUiThread {
                viewModel.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                viewModel.incrementExecutedCount()
                updateQueueSizeDisplay()
            }
        }
        
        updateQueueSizeDisplay()
        progressBar.visibility = View.VISIBLE
        llCompletionStatus.visibility = View.GONE
    }
    
    /**
     * 清空所有任务
     */
    private fun clearAllTasks() {
        idleTaskExecutor.clear()
        viewModel.clearAllTasks()
        tvStatus.text = "已清空所有任务"
        tvQueueSize.text = "队列中: 0 个任务"
        progressBar.visibility = View.GONE
        llCompletionStatus.visibility = View.GONE
        tvExecutionLogs.text = "等待任务执行..."
        appendLog("🗑 清空所有任务")
    }
    
    /**
     * 更新队列大小显示
     */
    private fun updateQueueSizeDisplay() {
        val queueSize = idleTaskExecutor.getQueueSize()
        viewModel.updateQueueSize(queueSize)
        
        if (queueSize == 0) {
            progressBar.visibility = View.GONE
        }
    }
    
    /**
     * 添加日志到执行日志视图
     * 
     * @param message 日志消息
     */
    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
            .format(java.util.Date())
        val logMessage = "[$timestamp] $message\n"
        
        if (tvExecutionLogs.text.toString() == "等待任务执行...") {
            tvExecutionLogs.text = logMessage
        } else {
            tvExecutionLogs.append(logMessage)
        }
        
        // 自动滚动到最新日志
        scrollViewLogs.post {
            scrollViewLogs.fullScroll(ScrollView.FOCUS_DOWN)
        }
        
        // 限制日志行数，避免内存占用过多
        val lines = tvExecutionLogs.text.toString().lines()
        if (lines.size > 50) {
            tvExecutionLogs.text = lines.takeLast(40).joinToString("\n")
        }
    }
    
    /**
     * 模拟耗时工作
     * 
     * @param durationMs 模拟执行时间（毫秒）
     */
    private fun simulateWork(durationMs: Long) {
        // 使用 SystemClock.sleep 模拟耗时操作
        // 实际应用中这里会是真实的业务逻辑
        SystemClock.sleep(durationMs)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // 清理资源
        idleTaskExecutor.clear()
    }
}