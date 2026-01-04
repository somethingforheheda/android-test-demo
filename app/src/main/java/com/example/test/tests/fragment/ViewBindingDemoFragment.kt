package com.example.test.tests.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.R
import com.example.test.databinding.FragmentViewBindingDemoBinding
import com.example.test.tests.viewbinding.FeatureAdapter
import com.example.test.tests.viewbinding.FeatureItem

/**
 * ViewBinding使用演示Fragment
 * 
 * 本Fragment全面展示了ViewBinding在Android开发中的使用方法和最佳实践
 * 
 * ViewBinding的主要优势：
 * 1. 类型安全 - 编译时生成绑定类，避免运行时类型转换错误
 * 2. 空安全 - 不会因为错误的view ID导致空指针异常
 * 3. 性能优化 - 只在初始化时查找view一次，后续直接访问
 * 4. 代码简洁 - 自动生成绑定代码，减少样板代码
 * 
 * 本示例包含：
 * - Fragment中的基础ViewBinding使用
 * - RecyclerView + ViewBinding的集成
 * - Include布局的ViewBinding处理
 * - ViewBinding生命周期管理
 * - 事件监听器的设置
 * - 与传统findViewById的对比
 */
class ViewBindingDemoFragment : Fragment() {
    
    // ViewBinding实例
    // 使用可空类型，因为在onDestroyView后需要置空防止内存泄漏
    private var _binding: FragmentViewBindingDemoBinding? = null
    
    // 提供非空的binding访问，仅在onCreateView和onDestroyView之间有效
    // 使用Kotlin的属性委托，确保在正确的生命周期内访问
    private val binding get() = _binding!!
    
    // RecyclerView的适配器
    private lateinit var featureAdapter: FeatureAdapter
    
    /**
     * 创建Fragment视图
     * 在这里初始化ViewBinding
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 使用ViewBinding的inflate方法创建视图
        // 这是ViewBinding在Fragment中的标准初始化方式
        _binding = FragmentViewBindingDemoBinding.inflate(inflater, container, false)
        
        // 返回binding的根视图
        // binding.root就是布局文件的根ViewGroup
        return binding.root
    }
    
    /**
     * 视图创建完成后的初始化
     * 在这里设置UI组件和事件监听器
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 设置标题和说明文字
        setupHeaderSection()
        
        // 设置输入演示区域
        setupInputSection()
        
        // 设置Include布局演示
        setupIncludeSection()
        
        // 设置RecyclerView列表
        setupRecyclerView()
        
        // 设置对比演示
        setupComparisonDemo()
    }
    
    /**
     * 设置头部区域
     * 展示ViewBinding的基本使用 - 直接访问视图
     */
    private fun setupHeaderSection() {
        // 通过binding直接访问视图，无需findViewById
        // IDE会自动提示所有可用的视图ID
        binding.titleText.text = "ViewBinding 使用演示"
        
        binding.subtitleText.text = """
            ViewBinding是Android官方推荐的视图绑定方案。
            它在编译时为每个布局文件生成一个绑定类，
            让你可以类型安全地访问布局中的所有视图。
        """.trimIndent()
    }
    
    /**
     * 设置输入演示区域
     * 展示ViewBinding处理用户输入和事件监听
     */
    private fun setupInputSection() {
        // 设置按钮点击事件
        // ViewBinding让事件监听器的设置更加直观
        binding.demoButton.setOnClickListener {
            // 获取输入框的文本
            val inputText = binding.inputEditText.text.toString()
            
            if (inputText.isNotEmpty()) {
                // 更新显示文本
                binding.resultText.text = "你输入的内容是：$inputText"
                
                // 显示Toast提示
                Toast.makeText(context, "已更新显示内容", Toast.LENGTH_SHORT).show()
                
                // 清空输入框
                binding.inputEditText.text.clear()
            } else {
                Toast.makeText(context, "请先输入一些内容", Toast.LENGTH_SHORT).show()
            }
        }
        
        // 设置清除按钮
        binding.clearButton.setOnClickListener {
            binding.inputEditText.text.clear()
            binding.resultText.text = "显示区域已清空"
        }
    }
    
    /**
     * 设置Include布局演示
     * 展示ViewBinding如何处理include标签
     */
    private fun setupIncludeSection() {
        // 访问include的布局
        // ViewBinding会为include的布局生成一个嵌套的binding对象
        binding.includedLayout.includeTitle.text = "这是Include布局"
        binding.includedLayout.includeDescription.text = """
            ViewBinding完美支持include标签。
            通过binding.includedLayout可以访问被包含布局中的所有视图。
            注意：include标签必须设置id才能在ViewBinding中访问。
        """.trimIndent()
        
        // 设置include布局中的按钮
        binding.includedLayout.includeButton.setOnClickListener {
            Toast.makeText(context, "点击了Include布局中的按钮", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 设置RecyclerView
     * 展示ViewBinding在RecyclerView中的使用
     */
    private fun setupRecyclerView() {
        // 创建功能列表数据
        val features = createFeatureList()
        
        // 初始化适配器
        featureAdapter = FeatureAdapter { feature ->
            // 处理列表项点击事件
            Toast.makeText(
                context, 
                "${feature.title} - ${if (feature.isEnabled) "已启用" else "已禁用"}", 
                Toast.LENGTH_SHORT
            ).show()
        }
        
        // 设置RecyclerView
        binding.featureRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = featureAdapter
            
            // 添加分割线等装饰
            // 这里可以添加ItemDecoration等
        }
        
        // 提交数据到适配器
        featureAdapter.submitList(features)
    }
    
    /**
     * 创建功能展示列表数据
     */
    private fun createFeatureList(): List<FeatureItem> {
        return listOf(
            FeatureItem(
                title = "类型安全",
                description = "编译时检查，避免运行时错误",
                iconRes = android.R.drawable.ic_dialog_info,
                isEnabled = true
            ),
            FeatureItem(
                title = "空安全",
                description = "不会因错误ID导致空指针",
                iconRes = android.R.drawable.ic_secure,
                isEnabled = true
            ),
            FeatureItem(
                title = "性能优化",
                description = "只查找视图一次，提升性能",
                iconRes = android.R.drawable.ic_menu_manage,
                isEnabled = true
            ),
            FeatureItem(
                title = "代码简洁",
                description = "减少样板代码，提高开发效率",
                iconRes = android.R.drawable.ic_menu_edit,
                isEnabled = true
            ),
            FeatureItem(
                title = "IDE支持",
                description = "自动补全和代码导航",
                iconRes = android.R.drawable.ic_menu_help,
                isEnabled = true
            )
        )
    }
    
    /**
     * 设置对比演示
     * 展示ViewBinding与传统方式的区别
     */
    private fun setupComparisonDemo() {
        binding.comparisonText.text = """
            传统方式 vs ViewBinding：
            
            传统方式：
            val textView = view.findViewById<TextView>(R.id.text_view)
            textView.text = "Hello" // 可能空指针
            
            ViewBinding方式：
            binding.textView.text = "Hello" // 类型安全，空安全
            
            优势总结：
            ✓ 编译时类型检查
            ✓ 避免findViewById的性能开销
            ✓ 自动处理视图类型转换
            ✓ IDE智能提示支持
        """.trimIndent()
    }
    
    /**
     * Fragment视图销毁时的清理工作
     * 这是ViewBinding在Fragment中使用的关键步骤
     * 必须将binding置空以避免内存泄漏
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // 置空binding引用，防止内存泄漏
        // 这是Fragment中使用ViewBinding的最佳实践
        _binding = null
    }
}