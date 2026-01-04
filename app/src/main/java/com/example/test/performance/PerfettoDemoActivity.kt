package com.example.test.performance

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.Trace
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class PerfettoDemoActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_perfetto_demo)

        // 初始化 UI
        resultTextView = findViewById(R.id.result_text_view)

        findViewById<Button>(R.id.btn_quick).setOnClickListener { quickOperation() }
        findViewById<Button>(R.id.btn_heavy).setOnClickListener { heavyComputation() }
        findViewById<Button>(R.id.btn_io).setOnClickListener { ioOperation() }
        findViewById<Button>(R.id.btn_nested).setOnClickListener { nestedTraces() }
        findViewById<Button>(R.id.btn_async).setOnClickListener { asyncTrace() }
    }

    /**
     * 极短耗时操作示例
     */
    private fun quickOperation() {
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("quickOperation")
        // 简单字符串拼接
        val text = (1..100).joinToString(",")
        Trace.endSection()
        val end = SystemClock.elapsedRealtime()
        appendResult("quickOperation 耗时 ${end - start}ms, 结果字符串长度=${text.length}")
    }

    /**
     * CPU 密集型操作示例
     */
    private fun heavyComputation() {
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("heavyComputation")
        var sum = 0L
        for (i in 0 until 20_000_000) {
            sum += i
        }
        Trace.endSection()
        val end = SystemClock.elapsedRealtime()
        appendResult("heavyComputation 耗时 ${end - start}ms, sum=$sum")
    }

    /**
     * 模拟 I/O（Thread.sleep）
     */
    private fun ioOperation() {
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("ioOperation")
        Thread.sleep(300)
        Trace.endSection()
        val end = SystemClock.elapsedRealtime()
        appendResult("ioOperation 耗时 ${end - start}ms")
    }

    /**
     * 嵌套 Trace 示例
     */
    private fun nestedTraces() {
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("nestedTraces_outer")
        // 外层耗时
        Thread.sleep(100)

        Trace.beginSection("nestedTraces_inner")
        // 内层耗时
        Thread.sleep(150)
        Trace.endSection()

        Trace.endSection()
        val end = SystemClock.elapsedRealtime()
        appendResult("nestedTraces 总耗时 ${end - start}ms")
    }

    /**
     * 异步 Trace 示例
     * 在主线程发起 Trace，延迟结束
     */
    private fun asyncTrace() {
        val start = SystemClock.elapsedRealtime()
        Trace.beginSection("asyncTrace_begin")
        appendResult("asyncTrace 已开始，500ms 后结束…")

        Handler(Looper.getMainLooper()).postDelayed({
            Trace.endSection()
            val end = SystemClock.elapsedRealtime()
            appendResult("asyncTrace 结束，总耗时 ${end - start}ms")
        }, 500)
    }

    private fun appendResult(text: String) {
        resultTextView.append("$text\n")
    }
} 