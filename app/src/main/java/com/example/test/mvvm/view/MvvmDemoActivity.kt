package com.example.test.mvvm.view

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.mvvm.viewmodel.ProductViewModel

/**
 * MVVM架构演示Activity
 * 展示MVVM架构的完整实现，包括数据绑定、状态管理等
 * 
 * MVVM架构说明：
 * - Model：数据层，包括Repository和数据模型
 * - View：视图层，Activity/Fragment负责UI展示
 * - ViewModel：视图模型层，持有UI状态，处理业务逻辑
 * 
 * 特点：
 * 1. View不直接与Model交互
 * 2. ViewModel通过LiveData暴露数据
 * 3. View观察LiveData的变化并更新UI
 * 4. ViewModel在配置变更时保持数据
 */
class MvvmDemoActivity : AppCompatActivity() {
    
    // 使用by viewModels()委托获取ViewModel实例
    // 这样可以确保ViewModel在配置变更时得到正确保持
    private val viewModel: ProductViewModel by viewModels()
    
    // UI组件
    private lateinit var etSearch: EditText
    private lateinit var btnRefresh: Button
    private lateinit var btnFilter: Button
    private lateinit var btnStatistics: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvCurrentFilter: TextView
    private lateinit var rvProducts: RecyclerView
    private lateinit var flStatisticsOverlay: FrameLayout
    private lateinit var tvStatisticsContent: TextView
    private lateinit var btnCloseStatistics: Button
    
    // 产品适配器
    private lateinit var productAdapter: ProductAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvvm_demo)
        
        // 初始化UI组件
        initViews()
        
        // 设置RecyclerView
        setupRecyclerView()
        
        // 设置事件监听器
        setupListeners()
        
        // 观察ViewModel中的LiveData
        observeViewModel()
    }
    
    /**
     * 初始化UI组件
     */
    private fun initViews() {
        etSearch = findViewById(R.id.et_search)
        btnRefresh = findViewById(R.id.btn_refresh)
        btnFilter = findViewById(R.id.btn_filter)
        btnStatistics = findViewById(R.id.btn_statistics)
        progressBar = findViewById(R.id.progress_bar)
        tvError = findViewById(R.id.tv_error)
        tvCurrentFilter = findViewById(R.id.tv_current_filter)
        rvProducts = findViewById(R.id.rv_products)
        flStatisticsOverlay = findViewById(R.id.fl_statistics_overlay)
        tvStatisticsContent = findViewById(R.id.tv_statistics_content)
        btnCloseStatistics = findViewById(R.id.btn_close_statistics)
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter()
        
        rvProducts.apply {
            // 使用网格布局管理器，每行显示2个条目
            layoutManager = GridLayoutManager(this@MvvmDemoActivity, 2)
            adapter = productAdapter
        }
        
        // 设置适配器事件监听器
        productAdapter.setOnItemClickListener { product ->
            viewModel.selectProduct(product)
        }
        
        productAdapter.setOnAddToCartListener { product ->
            viewModel.addToCart(product)
        }
        
        productAdapter.setOnAddToFavoritesListener { product ->
            viewModel.addToFavorites(product)
        }
    }
    
    /**
     * 设置事件监听器
     */
    private fun setupListeners() {
        // 搜索框文本变化监听
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchProducts(s.toString())
            }
        })
        
        // 刷新按钮点击事件
        btnRefresh.setOnClickListener {
            viewModel.refreshProducts()
        }
        
        // 筛选按钮点击事件
        btnFilter.setOnClickListener {
            showCategoryFilterDialog()
        }
        
        // 统计按钮点击事件
        btnStatistics.setOnClickListener {
            flStatisticsOverlay.visibility = View.VISIBLE
        }
        
        // 关闭统计弹窗
        btnCloseStatistics.setOnClickListener {
            flStatisticsOverlay.visibility = View.GONE
        }
        
        // 点击背景关闭统计弹窗
        flStatisticsOverlay.setOnClickListener {
            flStatisticsOverlay.visibility = View.GONE
        }
    }
    
    /**
     * 观察ViewModel中的LiveData变化
     * 这是MVVM架构的核心：View观察ViewModel的状态变化
     */
    private fun observeViewModel() {
        // 观察产品列表数据变化
        viewModel.products.observe(this) { products ->
            productAdapter.submitList(products)
            updateCurrentFilterText()
        }
        
        // 观察加载状态变化
        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // 观察错误信息变化
        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                tvError.text = error
                tvError.visibility = View.VISIBLE
            } else {
                tvError.visibility = View.GONE
            }
        }
        
        // 观察Toast消息
        viewModel.toastMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.clearToastMessage() // 清除消息，防止重复显示
            }
        }
        
        // 观察选中的产品
        viewModel.selectedProduct.observe(this) { product ->
            product?.let {
                showProductDetailDialog(it)
                viewModel.clearSelectedProduct() // 清除选中状态
            }
        }
        
        // 观察统计信息
        viewModel.statisticsInfo.observe(this) { statistics ->
            tvStatisticsContent.text = statistics
        }
        
        // 观察当前筛选分类
        viewModel.selectedCategory.observe(this) { category ->
            updateCurrentFilterText()
        }
        
        // 观察搜索关键词
        viewModel.searchQuery.observe(this) { query ->
            updateCurrentFilterText()
        }
    }
    
    /**
     * 更新当前筛选状态显示
     */
    private fun updateCurrentFilterText() {
        val category = viewModel.selectedCategory.value ?: "全部"
        val query = viewModel.searchQuery.value ?: ""
        val productCount = viewModel.products.value?.size ?: 0
        
        val filterText = when {
            query.isNotEmpty() -> "搜索：$query ($productCount 个结果)"
            category != "全部" -> "分类：$category ($productCount 个结果)"
            else -> "显示：全部产品 ($productCount 个结果)"
        }
        
        tvCurrentFilter.text = filterText
    }
    
    /**
     * 显示分类筛选对话框
     */
    private fun showCategoryFilterDialog() {
        val categories = mutableListOf("全部").apply {
            addAll(viewModel.getCategories())
        }
        
        AlertDialog.Builder(this)
            .setTitle("选择分类")
            .setItems(categories.toTypedArray()) { _, which ->
                val selectedCategory = if (which == 0) null else categories[which]
                viewModel.filterByCategory(selectedCategory)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示产品详情对话框
     */
    private fun showProductDetailDialog(product: com.example.test.mvvm.model.Product) {
        val message = """
            产品名称：${product.name}
            分类：${product.category}
            价格：${product.getFormattedPrice()}
            评分：${product.getRatingStars()} (${product.rating})
            库存：${product.stock} 件 (${product.getStockStatus()})
            推荐：${if (product.isRecommended) "是" else "否"}
            
            描述：${product.description}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("产品详情")
            .setMessage(message)
            .setPositiveButton("添加到购物车") { _, _ ->
                viewModel.addToCart(product)
            }
            .setNeutralButton("添加到收藏") { _, _ ->
                viewModel.addToFavorites(product)
            }
            .setNegativeButton("关闭", null)
            .show()
    }
}