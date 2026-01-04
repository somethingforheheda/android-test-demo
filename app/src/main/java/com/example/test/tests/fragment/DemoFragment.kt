package com.example.test.tests.fragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.test.R

// ViewModel定义 - 负责管理UI相关数据和业务逻辑
class DemoViewModel : ViewModel() {

    // 私有可变LiveData，存储用户数据，只允许ViewModel内部修改
    private val _userData = MutableLiveData<String>()
    // 对外暴露的只读LiveData，UI层只能观察不能修改，保证数据封装性
    val userData: LiveData<String> = _userData

    // 私有可变LiveData，管理加载状态
    private val _loading = MutableLiveData<Boolean>()
    // 对外暴露的加载状态，用于控制UI加载指示器
    val loading: LiveData<Boolean> = _loading

    // 私有可变LiveData，管理计数器数值
    private val _counter = MutableLiveData<Int>()
    // 对外暴露的计数器数据
    val counter: LiveData<Int> = _counter

    // ViewModel初始化块，设置所有LiveData的初始值
    init {
        _counter.value = 0                    // 计数器初始值为0
        _userData.value = "初始用户数据"        // 用户数据初始状态
        _loading.value = false               // 初始状态为非加载中
    }

    // 模拟异步加载用户数据的方法
    fun loadUser(userId: String) {
        _loading.value = true                // 开始加载，更新UI显示加载状态
        // 使用Handler模拟网络请求的异步操作
        android.os.Handler().postDelayed({
            // 2秒后更新用户数据，模拟网络请求完成
            _userData.value = "用户ID: $userId 的数据已加载"
            _loading.value = false           // 加载完成，隐藏加载指示器
        }, 2000)                            // 延迟2000毫秒（2秒）
    }

    // 计数器递增方法
    fun incrementCounter() {
        // 使用Elvis操作符(?:)处理null值，确保计数器从0开始递增
        _counter.value = (_counter.value ?: 0) + 1
    }

    // ViewModel销毁时的回调方法，用于清理资源防止内存泄漏
    override fun onCleared() {
        super.onCleared()
        // 在此处清理资源，如取消网络请求、关闭数据库连接等
        // 当前示例中Handler会自动清理，无需额外处理
    }
}

// Fragment类，使用构造函数直接指定布局文件
class DemoFragment : Fragment(R.layout.fragment_demo) {

    // 延迟初始化的ViewModel实例，避免空值检查
    private lateinit var viewModel: DemoViewModel

    // Fragment视图创建完成后的回调方法
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 通过ViewModelProvider获取ViewModel实例，确保配置更改时数据保持
        // this指向Fragment，作为ViewModel的生命周期拥有者
        viewModel = ViewModelProvider(this)[DemoViewModel::class.java]

        // 通过findViewById获取布局中的UI控件引用
        val demoText = view.findViewById<TextView>(R.id.demo_text)           // 显示用户数据的文本控件
        val loadingBar = view.findViewById<ProgressBar>(R.id.progress_bar)   // 加载进度条
        val counterText = view.findViewById<TextView>(R.id.counter_text)     // 显示计数器的文本控件
        val loadButton = view.findViewById<Button>(R.id.load_button)         // 触发数据加载的按钮
        val incrementButton = view.findViewById<Button>(R.id.increment_button) // 计数器递增按钮

        // 观察用户数据变化，使用viewLifecycleOwner确保Fragment视图销毁时自动解除观察
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            demoText.text = userData         // 数据更新时自动更新UI显示
        }

        // 观察加载状态变化，控制UI加载指示器和按钮可用性
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // 根据加载状态控制进度条可见性：加载中显示，否则隐藏
            loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            // 加载中禁用按钮，防止重复请求
            loadButton.isEnabled = !isLoading
        }

        // 观察计数器数值变化，实时更新计数显示
        viewModel.counter.observe(viewLifecycleOwner) { count ->
            counterText.text = "计数: $count" // 格式化显示计数器数值
        }

        // 设置加载按钮点击监听器
        loadButton.setOnClickListener {
            viewModel.loadUser("12345")     // 点击时触发用户数据加载，传入固定用户ID
        }

        // 设置计数器按钮点击监听器
        incrementButton.setOnClickListener {
            viewModel.incrementCounter()    // 点击时递增计数器
        }
    }
}