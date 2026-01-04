package com.example.test.frametask

import android.os.Bundle
import android.util.Log
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.test.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FrameTaskSplitter 使用示例 Fragment
 * 
 * 展示了 FrameTaskSplitter 的各种使用场景：
 * 1. 批量图片处理任务
 * 2. 数据解析任务
 * 3. 复杂计算任务
 * 4. 实时性能监控和动态调整
 * 
 * 实际应用场景：
 * - 相册应用中批量生成缩略图
 * - 大数据集的分批处理
 * - 游戏中的资源预加载
 * - 复杂动画的预计算
 */
class FrameTaskSplitterDemoFragment : Fragment() {
    
    companion object {
        private const val TAG = "FrameTaskDemo"
    }
    
    // FrameTaskSplitter 实例
    private lateinit var frameTaskSplitter: FrameTaskSplitter
    
    // 待执行的任务列表
    private var pendingTasks: List<() -> Unit>? = null
    
    // UI 组件
    private lateinit var spinnerTaskType: Spinner
    private lateinit var seekBarComplexity: SeekBar
    private lateinit var tvComplexity: TextView
    private lateinit var btnGenerate1000: Button
    private lateinit var btnGenerate5000: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var tvFrameTime: TextView
    private lateinit var tvTasksPerFrame: TextView
    private lateinit var tvElapsedTime: TextView
    private lateinit var tvFrameHistory: TextView
    private lateinit var btnStart: Button
    private lateinit var btnPause: Button
    private lateinit var btnResume: Button
    private lateinit var btnCancel: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvDirectExecutionResult: TextView
    private lateinit var tvFrameExecutionResult: TextView
    private lateinit var progressBarDirect: ProgressBar
    private lateinit var progressBarFrame: ProgressBar
    private lateinit var tvProgressDirect: TextView
    private lateinit var tvProgressFrame: TextView
    private lateinit var btnStartComparison: Button
    // 新增的性能对比UI元素
    private lateinit var tvDirectFrameTime: TextView
    private lateinit var tvDirectDropRate: TextView
    private lateinit var tvDirectStatus: TextView
    private lateinit var tvFrameDropRate: TextView
    private lateinit var tvDirectFrameHistory: TextView
    
    // 任务类型
    private enum class TaskType {
        IMAGE_PROCESSING,
        DATA_PARSING,
        CALCULATION
    }
    
    // 当前选中的任务类型
    private var selectedTaskType = TaskType.IMAGE_PROCESSING
    
    // 任务复杂度 (1-10)
    private var taskComplexity = 5
    
    // 帧时间历史记录
    private val frameTimeHistory = mutableListOf<Long>()
    private var maxFrameHistorySize = 20
    
    // 开始时间
    private var startTime = 0L
    
    // 执行结果
    private var directExecutionTime = 0L
    private var frameExecutionTime = 0L
    private var directExecutionDroppedFrames = 0
    private var isDirectExecutionRunning = false
    private val directFrameTimeHistory = mutableListOf<Long>()
    private var directExecutionFrameCount = 0 // 直接执行的帧计数
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_frame_task_splitter_demo, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化 FrameTaskSplitter
        frameTaskSplitter = FrameTaskSplitter()
        
        // 初始化 UI
        initViews(view)
        
        // 设置监听器
        setupListeners()
    }
    
    /**
     * 初始化视图组件
     */
    private fun initViews(view: View) {
        spinnerTaskType = view.findViewById(R.id.spinnerTaskType)
        seekBarComplexity = view.findViewById(R.id.seekBarComplexity)
        tvComplexity = view.findViewById(R.id.tvComplexity)
        btnGenerate1000 = view.findViewById(R.id.btnGenerate1000)
        btnGenerate5000 = view.findViewById(R.id.btnGenerate5000)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)
        tvFrameTime = view.findViewById(R.id.tvFrameTime)
        tvTasksPerFrame = view.findViewById(R.id.tvTasksPerFrame)
        tvElapsedTime = view.findViewById(R.id.tvElapsedTime)
        tvFrameHistory = view.findViewById(R.id.tvFrameHistory)
        btnStart = view.findViewById(R.id.btnStart)
        btnPause = view.findViewById(R.id.btnPause)
        btnResume = view.findViewById(R.id.btnResume)
        btnCancel = view.findViewById(R.id.btnCancel)
        tvStatus = view.findViewById(R.id.tvStatus)
        tvDirectExecutionResult = view.findViewById(R.id.tvDirectExecutionResult)
        tvFrameExecutionResult = view.findViewById(R.id.tvFrameExecutionResult)
        progressBarDirect = view.findViewById(R.id.progressBarDirect)
        progressBarFrame = view.findViewById(R.id.progressBarFrame)
        tvProgressDirect = view.findViewById(R.id.tvProgressDirect)
        tvProgressFrame = view.findViewById(R.id.tvProgressFrame)
        btnStartComparison = view.findViewById(R.id.btnStartComparison)
        
        // 新增的性能对比UI元素
        tvDirectFrameTime = view.findViewById(R.id.tvDirectFrameTime)
        tvDirectDropRate = view.findViewById(R.id.tvDirectDropRate)
        tvDirectStatus = view.findViewById(R.id.tvDirectStatus)
        tvFrameDropRate = view.findViewById(R.id.tvFrameDropRate)
        tvDirectFrameHistory = view.findViewById(R.id.tvDirectFrameHistory)
        
        // 设置任务类型选项
        val taskTypes = arrayOf("图片处理", "数据解析", "复杂计算")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, taskTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTaskType.adapter = adapter
        
        // 设置初始状态
        seekBarComplexity.progress = 5
        tvComplexity.text = "任务复杂度: 5"
        progressBar.max = 100
        progressBarDirect.max = 100
        progressBarFrame.max = 100
        updateButtonStates(false)
        tvDirectExecutionResult.text = "直接执行：等待开始..."
        tvFrameExecutionResult.text = "分帧执行：等待开始..."
    }
    
    /**
     * 设置监听器
     */
    private fun setupListeners() {
        // 任务类型选择
        spinnerTaskType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedTaskType = TaskType.values()[position]
                updateTaskDescription()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 复杂度调整
        seekBarComplexity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                taskComplexity = progress.coerceAtLeast(1)
                tvComplexity.text = "任务复杂度: $taskComplexity"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // 生成任务按钮
        btnGenerate1000.setOnClickListener {
            generateTasks(1000)
        }
        
        btnGenerate5000.setOnClickListener {
            generateTasks(5000)
        }
        
        // 控制按钮 (仅用于分帧执行的控制)
        btnPause.setOnClickListener {
            pauseTasks()
        }
        
        btnResume.setOnClickListener {
            resumeTasks()
        }
        
        btnCancel.setOnClickListener {
            cancelTasks()
        }
        
        // 开始对比按钮
        btnStartComparison.setOnClickListener {
            startComparisonTest()
        }
    }
    
    
    /**
     * 更新任务描述
     */
    private fun updateTaskDescription() {
        val description = when (selectedTaskType) {
            TaskType.IMAGE_PROCESSING -> "模拟图片处理：缩放、滤镜、压缩等操作"
            TaskType.DATA_PARSING -> "模拟数据解析：JSON解析、数据转换等"
            TaskType.CALCULATION -> "模拟复杂计算：矩阵运算、算法处理等"
        }
        tvStatus.text = description
    }
    
    /**
     * 生成任务
     */
    private fun generateTasks(count: Int) {
        val tasks = mutableListOf<() -> Unit>()
        
        when (selectedTaskType) {
            TaskType.IMAGE_PROCESSING -> {
                // 模拟图片处理任务
                for (i in 0 until count) {
                    tasks.add {
                        simulateImageProcessing(i, taskComplexity)
                    }
                }
            }
            TaskType.DATA_PARSING -> {
                // 模拟数据解析任务
                for (i in 0 until count) {
                    tasks.add {
                        simulateDataParsing(i, taskComplexity)
                    }
                }
            }
            TaskType.CALCULATION -> {
                // 模拟复杂计算任务
                for (i in 0 until count) {
                    tasks.add {
                        simulateComplexCalculation(i, taskComplexity)
                    }
                }
            }
        }
        
        // 保存任务
        pendingTasks = tasks
        
        tvStatus.text = "已生成 $count 个${getTaskTypeName()}任务，点击开始对比"
        btnStartComparison.isEnabled = true
        progressBarDirect.progress = 0
        progressBarFrame.progress = 0
        tvProgressDirect.text = "0 / $count (0%)"
        tvProgressFrame.text = "0 / $count (0%)"
    }
    
    /**
     * 开始对比测试
     */
    private fun startComparisonTest() {
        val tasks = pendingTasks ?: return
        
        btnStartComparison.isEnabled = false
        tvStatus.text = "正在执行对比测试..."
        
        // 重置结果
        directExecutionTime = 0
        frameExecutionTime = 0
        directExecutionDroppedFrames = 0
        frameTimeHistory.clear()
        directFrameTimeHistory.clear()
        
        // 先执行直接方式
        tvDirectExecutionResult.text = "直接执行：运行中..."
        tvDirectStatus.text = "• 状态: 执行中"
        executeDirectly(tasks)
    }
    
    /**
     * 直接执行方式（不分帧）
     */
    private fun executeDirectly(tasks: List<() -> Unit>) {
        Log.d(TAG, "===== 开始直接执行测试 =====")
        Log.d(TAG, "任务总数: ${tasks.size}")
        
        val startTime = System.currentTimeMillis()
        var completed = 0
        var currentTaskIndex = 0
        val choreographer = Choreographer.getInstance()
        var lastFrameTime = System.nanoTime()
        var frameCount = 0
        
        // 使用Choreographer来监测真实的帧率
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount++
                val frameStartTime = System.currentTimeMillis()
                
                // 计算上一帧到这一帧的时间间隔
                val frameDuration = if (lastFrameTime > 0) {
                    (frameTimeNanos - lastFrameTime) / 1_000_000L // 转换为毫秒
                } else {
                    0L // 第一帧没有间隔
                }
                
                Log.d(TAG, "[直接执行] 第${frameCount}帧开始")
                Log.d(TAG, "[直接执行] 帧间隔: ${frameDuration}ms")
                Log.d(TAG, "[直接执行] 当前任务索引: $currentTaskIndex")
                
                lastFrameTime = frameTimeNanos
                
                // 在主线程直接执行任务（会阻塞UI）
                // 为了展示掉帧效果，每帧执行大量任务，使帧时间超过16ms
                val batchStartTime = System.currentTimeMillis()
                var batchExecuted = 0
                val tasksPerFrame = 100 // 直接执行每帧执行100个任务，故意造成掉帧
                
                Log.d(TAG, "[直接执行] 开始执行任务批次")
                
                while (currentTaskIndex < tasks.size && batchExecuted < tasksPerFrame) {
                    try {
                        tasks[currentTaskIndex]()
                        currentTaskIndex++
                        completed++
                        batchExecuted++
                        
                        // 每执行10个任务更新一次UI
                        if (batchExecuted % 10 == 0) {
                            val progress = (completed * 100f / tasks.size).toInt()
                            progressBarDirect.progress = progress
                            tvProgressDirect.text = "$completed / ${tasks.size} ($progress%)"
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "[直接执行] 任务执行失败", e)
                        e.printStackTrace()
                    }
                }
                
                val batchExecutionTime = System.currentTimeMillis() - batchStartTime
                Log.d(TAG, "[直接执行] 本批次执行了 $batchExecuted 个任务，耗时: ${batchExecutionTime}ms")
                
                // 记录帧执行时间（不是帧间隔，而是帧内任务执行时间）
                val frameEndTime = System.currentTimeMillis()
                val frameExecutionTime = frameEndTime - frameStartTime
                
                // 如果帧执行时间超过16ms，说明掉帧了
                if (frameExecutionTime > 16) {
                    directExecutionDroppedFrames++
                    Log.w(TAG, "[直接执行] 检测到掉帧！帧执行时间: ${frameExecutionTime}ms")
                }
                
                // 记录帧执行时间
                directFrameTimeHistory.add(frameExecutionTime)
                if (directFrameTimeHistory.size > 20) {
                    directFrameTimeHistory.removeAt(0)
                }
                
                directExecutionFrameCount++
                
                // 更新UI显示
                val progress = (completed * 100f / tasks.size).toInt()
                progressBarDirect.progress = progress
                tvProgressDirect.text = "$completed / ${tasks.size} ($progress%)"
                
                if (directFrameTimeHistory.isNotEmpty()) {
                    val avgFrameTime = directFrameTimeHistory.average().toInt()
                    tvDirectFrameTime.text = "• 平均帧时间: ${avgFrameTime}ms"
                    
                    val historyText = directFrameTimeHistory.takeLast(10).joinToString(", ") { "${it}ms" }
                    tvDirectFrameHistory.text = "直接执行: $historyText"
                    
                    Log.d(TAG, "[直接执行] 帧时间历史: $historyText")
                    Log.d(TAG, "[直接执行] 平均帧时间: ${avgFrameTime}ms")
                }
                
                val dropRate = if (directFrameTimeHistory.isNotEmpty()) {
                    (directExecutionDroppedFrames * 100f / directFrameTimeHistory.size).toInt()
                } else 0
                tvDirectDropRate.text = "• 掉帧率: $dropRate%"
                
                Log.d(TAG, "[直接执行] 当前统计 - 总帧数: ${directFrameTimeHistory.size}, 掉帧数: $directExecutionDroppedFrames, 掉帧率: $dropRate%")
                
                // 所有任务执行完成
                if (currentTaskIndex >= tasks.size) {
                    directExecutionTime = System.currentTimeMillis() - startTime
                    
                    // 计算实际的掉帧数（基于记录的帧时间）
                    val actualDroppedFrames = directFrameTimeHistory.count { it > 16 }
                    
                    Log.d(TAG, "===== 直接执行完成 =====")
                    Log.d(TAG, "总执行时间: ${directExecutionTime}ms")
                    Log.d(TAG, "总帧数: $frameCount")
                    Log.d(TAG, "记录的帧时间数: ${directFrameTimeHistory.size}")
                    Log.d(TAG, "基于Choreographer的掉帧数: $directExecutionDroppedFrames")
                    Log.d(TAG, "基于帧时间的掉帧数: $actualDroppedFrames")
                    Log.d(TAG, "帧时间列表: ${directFrameTimeHistory.joinToString(", ")}ms")
                    
                    val finalDropRate = if (directFrameTimeHistory.isNotEmpty()) {
                        (actualDroppedFrames * 100f / directFrameTimeHistory.size).toInt()
                    } else 0
                    
                    tvDirectExecutionResult.text = """
                        直接执行完成：
                        • 总时间：${directExecutionTime}ms
                        • 掉帧数：$actualDroppedFrames
                        • 掉帧率：$finalDropRate%
                    """.trimIndent()
                    tvDirectStatus.text = "• 状态: 完成"
                    
                    // 开始分帧执行
                    startFrameExecution()
                } else {
                    // 继续下一帧
                    choreographer.postFrameCallback(this)
                }
            }
        }
        
        // 开始执行
        choreographer.postFrameCallback(frameCallback)
    }
    
    /**
     * 开始分帧执行
     */
    private fun startFrameExecution() {
        val tasks = pendingTasks ?: return
        
        Log.d(TAG, "\n===== 开始分帧执行测试 =====")
        Log.d(TAG, "任务总数: ${tasks.size}")
        
        tvFrameExecutionResult.text = "分帧执行：运行中..."
        startTime = System.currentTimeMillis()
        
        // 启用分帧控制按钮
        btnPause.isEnabled = true
        btnCancel.isEnabled = true
        
        frameTaskSplitter = FrameTaskSplitter()
        frameTaskSplitter.executeTasks(
            tasks,
            onProgress = { completed, total ->
                activity?.runOnUiThread {
                    val progress = (completed * 100f / total).toInt()
                    progressBarFrame.progress = progress
                    tvProgressFrame.text = "$completed / $total ($progress%)"
                }
            },
            onFrameInfo = { frameTimeMs, tasksExecuted ->
                Log.d(TAG, "[分帧执行] 帧时间: ${frameTimeMs}ms, 执行任务数: $tasksExecuted")
                
                activity?.runOnUiThread {
                    tvFrameTime.text = "• 当前帧时间: ${frameTimeMs}ms"
                    tvTasksPerFrame.text = "• 每帧任务数: $tasksExecuted"
                    updateFrameHistory(frameTimeMs)
                    
                    // 更新实时掉帧率
                    val droppedFrames = frameTimeHistory.count { it > 16 }
                    val dropRate = if (frameTimeHistory.isNotEmpty()) {
                        (droppedFrames * 100f / frameTimeHistory.size).toInt()
                    } else 0
                    tvFrameDropRate.text = "• 掉帧率: $dropRate%"
                    
                    Log.d(TAG, "[分帧执行] 当前统计 - 总帧数: ${frameTimeHistory.size}, 掉帧数: $droppedFrames, 掉帧率: $dropRate%")
                    
                    // 更新已用时间对比
                    val elapsedMs = System.currentTimeMillis() - startTime
                    tvElapsedTime.text = "执行时间对比: 直接(${directExecutionTime}ms) vs 分帧(${elapsedMs}ms)"
                }
            },
            onComplete = {
                frameExecutionTime = System.currentTimeMillis() - startTime
                
                Log.d(TAG, "===== 分帧执行完成 =====")
                Log.d(TAG, "总执行时间: ${frameExecutionTime}ms")
                Log.d(TAG, "记录的帧数: ${frameTimeHistory.size}")
                Log.d(TAG, "帧时间列表: ${frameTimeHistory.joinToString(", ")}ms")
                
                activity?.runOnUiThread {
                    // 禁用控制按钮
                    btnPause.isEnabled = false
                    btnResume.isEnabled = false
                    btnCancel.isEnabled = false
                    showComparisonResults()
                }
            }
        )
    }
    
    /**
     * 显示对比结果
     */
    private fun showComparisonResults() {
        val droppedFrames = frameTimeHistory.count { it > 16 }
        tvFrameExecutionResult.text = """
            分帧执行完成：
            • 总时间：${frameExecutionTime}ms
            • 掉帧数：$droppedFrames
            • 掉帧率：${(droppedFrames * 100f / frameTimeHistory.size).toInt()}%
            • 平均帧时间：${frameTimeHistory.average().toInt()}ms
        """.trimIndent()
        
        // 更新最终对比
        val directAvgFrameTime = synchronized(directFrameTimeHistory) {
            if (directFrameTimeHistory.isNotEmpty()) directFrameTimeHistory.average().toInt() else 0
        }
        val frameAvgFrameTime = if (frameTimeHistory.isNotEmpty()) frameTimeHistory.average().toInt() else 0
        
        tvElapsedTime.text = """
            执行时间对比：
            • 直接执行: ${directExecutionTime}ms (平均帧时间: ${directAvgFrameTime}ms)
            • 分帧执行: ${frameExecutionTime}ms (平均帧时间: ${frameAvgFrameTime}ms)
            • 时间差异: ${frameExecutionTime - directExecutionTime}ms
        """.trimIndent()
        
        // 显示对比总结
        val timeDiff = frameExecutionTime - directExecutionTime
        val timeRatio = if (directExecutionTime > 0) {
            ((frameExecutionTime - directExecutionTime) * 100f / directExecutionTime).toInt()
        } else 0
        
        val directDropRate = (directExecutionDroppedFrames * 100f / pendingTasks!!.size).toInt()
        val frameDropRate = (droppedFrames * 100f / frameTimeHistory.size).toInt()
        
        tvStatus.text = """
            🎯 对比测试完成！
            ⏱ 时间: 分帧执行多用了 ${timeDiff}ms ($timeRatio%)
            📊 掉帧: 直接执行 $directDropRate% vs 分帧执行 $frameDropRate%
            ✅ 结论: 分帧执行保持了UI流畅性，避免了ANR风险
        """.trimIndent()
        
        btnStartComparison.isEnabled = true
    }
    
    
    /**
     * 暂停任务
     */
    private fun pauseTasks() {
        frameTaskSplitter.pause()
        btnPause.isEnabled = false
        btnResume.isEnabled = true
        tvStatus.text = "任务已暂停"
    }
    
    /**
     * 恢复任务
     */
    private fun resumeTasks() {
        frameTaskSplitter.resume()
        btnPause.isEnabled = true
        btnResume.isEnabled = false
        tvStatus.text = "任务已恢复"
    }
    
    /**
     * 取消任务
     */
    private fun cancelTasks() {
        frameTaskSplitter.cancel()
        btnPause.isEnabled = false
        btnResume.isEnabled = false
        btnCancel.isEnabled = false
        tvFrameExecutionResult.text = "分帧执行：已取消"
        tvStatus.text = "对比测试已取消"
        btnStartComparison.isEnabled = true
    }
    
    /**
     * 更新按钮状态
     */
    private fun updateButtonStates(hasTask: Boolean) {
        btnStartComparison.isEnabled = hasTask
        btnPause.isEnabled = false
        btnResume.isEnabled = false
        btnCancel.isEnabled = false
    }
    
    /**
     * 更新帧时间历史
     */
    private fun updateFrameHistory(frameTime: Long) {
        frameTimeHistory.add(frameTime)
        
        // 限制历史记录数量
        if (frameTimeHistory.size > maxFrameHistorySize) {
            frameTimeHistory.removeAt(0)
        }
        
        // 更新显示
        val historyText = frameTimeHistory.takeLast(10).joinToString(", ") { "${it}ms" }
        tvFrameHistory.text = "分帧执行: $historyText"
    }
    
    
    /**
     * 模拟图片处理
     */
    private fun simulateImageProcessing(index: Int, complexity: Int) {
        // 模拟图片处理的计算
        var result = 0.0
        for (i in 0 until complexity * 100) {
            result += Math.random() * sin(index.toDouble()) * cos(i.toDouble())
        }
    }
    
    /**
     * 模拟数据解析
     */
    private fun simulateDataParsing(index: Int, complexity: Int) {
        // 模拟JSON解析的字符串操作
        val sb = StringBuilder()
        for (i in 0 until complexity * 50) {
            sb.append("data_${index}_${i}_")
        }
        val result = sb.toString().hashCode()
    }
    
    /**
     * 模拟复杂计算
     */
    private fun simulateComplexCalculation(index: Int, complexity: Int) {
        // 模拟矩阵运算
        var result = 0.0
        for (i in 0 until complexity * 200) {
            result += sqrt((index * i).toDouble()) + sin(i.toDouble())
        }
    }
    
    /**
     * 获取任务类型名称
     */
    private fun getTaskTypeName(): String {
        return when (selectedTaskType) {
            TaskType.IMAGE_PROCESSING -> "图片处理"
            TaskType.DATA_PARSING -> "数据解析"
            TaskType.CALCULATION -> "复杂计算"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        frameTaskSplitter.cancel()
    }
}