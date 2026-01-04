package com.example.test

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.test.tests.ui.FragmentContainerActivity

/**
 * 主Activity - Android测试Demo工程入口
 * 
 * 功能说明：
 * 1. 作为应用的主入口界面
 * 2. 动态生成测试按钮列表
 * 3. 支持Activity和Fragment两种类型的测试
 * 4. 展示各种Android开发模式和技术点
 * 
 * 设计目标：
 * - 提供完整的Android开发架构演示
 * - 涵盖MVP、MVVM等主流架构模式
 * - 演示网络请求、数据库操作、UI组件等
 * - 方便开发者学习和测试不同技术方案
 */
class MainActivity : ComponentActivity() {
    
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        runOnUiThread {  }
        // 注册所有测试项目
        TestRegisterManager.registerAll()
        
        // 获取主布局容器
        val linearLayout = findViewById<LinearLayout>(R.id.test_list_layout)
        
        // 动态生成Activity测试按钮
        TestRegistry.getTests().forEach { (name, clazz) ->
            val button = Button(this).apply {
                text = "[Activity] $name"
                // 设置按钮点击事件，启动对应的测试Activity
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, clazz))
                }
            }
            linearLayout.addView(button)
        }
        
        // 添加Fragment测试集合按钮
        if (FragmentTestRegistry.getFragments().isNotEmpty()) {
            val fragmentButton = Button(this).apply {
                text = "📦 Fragment 测试集合 (${FragmentTestRegistry.getFragments().size}个)"
                textSize = 16f
                setPadding(20, 30, 20, 30)
                // 设置按钮样式
                setBackgroundColor(android.graphics.Color.parseColor("#4CAF50"))
                setTextColor(android.graphics.Color.WHITE)
                // 设置点击事件，启动FragmentListActivity
                setOnClickListener {
                    startActivity(Intent(this@MainActivity, FragmentListActivity::class.java))
                }
            }
            // 添加一些间距
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 20, 0, 0)
            }
            fragmentButton.layoutParams = params
            linearLayout.addView(fragmentButton)
        }
    }
}
