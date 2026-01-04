package com.example.test.tests.fragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.test.R

// 简化的Fragment类，使用构造函数直接指定布局文件
class DemoTwoFragment : Fragment(R.layout.fragment_demo_two) {

    // Fragment视图创建完成后的回调方法
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 通过findViewById获取布局中的按钮
        val demoButton = view.findViewById<Button>(R.id.demo_button)

        // 设置按钮点击监听器，点击时显示Toast
        demoButton.setOnClickListener {
            Toast.makeText(requireContext(), "Hello from DemoTwoFragment!", Toast.LENGTH_SHORT).show()
        }
    }
} 