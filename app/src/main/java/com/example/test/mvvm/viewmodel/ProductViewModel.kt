package com.example.test.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test.mvvm.model.Product
import com.example.test.mvvm.model.ProductRepository
import kotlinx.coroutines.launch

/**
 * MVVM架构演示 - 产品ViewModel
 * 作为View和Model之间的桥梁，处理UI相关的业务逻辑
 * 
 * ViewModel的职责：
 * 1. 持有UI状态数据
 * 2. 处理用户操作
 * 3. 与Repository交互获取数据
 * 4. 将数据转换为UI需要的格式
 * 5. 处理配置变更时的数据保持
 */
class ProductViewModel : ViewModel() {
    
    // 数据仓库实例
    private val repository = ProductRepository()
    
    // 产品列表LiveData - 从Repository获取
    val products: LiveData<List<Product>> = repository.products
    
    // 加载状态LiveData - 从Repository获取
    val isLoading: LiveData<Boolean> = repository.isLoading
    
    // 错误信息LiveData - 从Repository获取
    val error: LiveData<String> = repository.error
    
    // 当前选中的分类
    private val _selectedCategory = MutableLiveData<String>()
    val selectedCategory: LiveData<String> = _selectedCategory
    
    // 搜索关键词
    private val _searchQuery = MutableLiveData<String>()
    val searchQuery: LiveData<String> = _searchQuery
    
    // Toast消息LiveData
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage
    
    // 产品详情LiveData
    private val _selectedProduct = MutableLiveData<Product?>()
    val selectedProduct: LiveData<Product?> = _selectedProduct
    
    // 统计信息LiveData
    private val _statisticsInfo = MutableLiveData<String>()
    val statisticsInfo: LiveData<String> = _statisticsInfo
    
    init {
        // 初始化时加载产品数据
        loadProducts()
    }
    
    /**
     * 加载产品列表
     * 在协程中执行，避免阻塞UI线程
     */
    fun loadProducts() {
        viewModelScope.launch {
            repository.loadProducts()
            updateStatistics()
        }
    }
    
    /**
     * 刷新产品列表
     * 强制重新获取数据
     */
    fun refreshProducts() {
        viewModelScope.launch {
            repository.refreshProducts()
            updateStatistics()
            _toastMessage.value = "数据已刷新"
        }
    }
    
    /**
     * 按分类筛选产品
     * @param category 产品分类，null表示显示所有产品
     */
    fun filterByCategory(category: String?) {
        _selectedCategory.value = category ?: "全部"
        repository.filterByCategory(category)
        updateStatistics()
        
        val message = if (category.isNullOrEmpty()) {
            "显示所有产品"
        } else {
            "筛选分类：$category"
        }
        _toastMessage.value = message
    }
    
    /**
     * 搜索产品
     * @param query 搜索关键词
     */
    fun searchProducts(query: String) {
        _searchQuery.value = query
        repository.searchProducts(query)
        updateStatistics()
        
        if (query.isEmpty()) {
            _toastMessage.value = "显示所有产品"
        } else {
            _toastMessage.value = "搜索：$query"
        }
    }
    
    /**
     * 选择产品查看详情
     * @param product 选中的产品
     */
    fun selectProduct(product: Product) {
        _selectedProduct.value = product
        _toastMessage.value = "查看产品：${product.name}"
    }
    
    /**
     * 清除选中的产品
     */
    fun clearSelectedProduct() {
        _selectedProduct.value = null
    }
    
    /**
     * 获取所有产品分类
     * @return 分类列表
     */
    fun getCategories(): List<String> {
        return repository.getCategories()
    }
    
    /**
     * 获取推荐产品
     * @return 推荐产品列表
     */
    fun getRecommendedProducts(): List<Product> {
        return products.value?.filter { it.isRecommended } ?: emptyList()
    }
    
    /**
     * 获取热门产品（高评分产品）
     * @return 热门产品列表
     */
    fun getPopularProducts(): List<Product> {
        return products.value?.filter { it.rating >= 4.0f }?.sortedByDescending { it.rating } ?: emptyList()
    }
    
    /**
     * 更新统计信息
     */
    private fun updateStatistics() {
        val currentProducts = products.value ?: emptyList()
        if (currentProducts.isEmpty()) {
            _statisticsInfo.value = "暂无数据"
            return
        }
        
        val totalCount = currentProducts.size
        val categories = currentProducts.map { it.category }.distinct().size
        val averagePrice = currentProducts.map { it.price }.average()
        val recommendedCount = currentProducts.count { it.isRecommended }
        val inStockCount = currentProducts.count { it.stock > 0 }
        
        val statistics = """
            产品总数：$totalCount
            分类数量：$categories
            平均价格：¥%.2f
            推荐产品：$recommendedCount
            有库存：$inStockCount
        """.trimIndent().format(averagePrice)
        
        _statisticsInfo.value = statistics
    }
    
    /**
     * 模拟添加到购物车
     * @param product 要添加的产品
     */
    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            _toastMessage.value = "抱歉，${product.name} 已售罄"
        } else {
            _toastMessage.value = "${product.name} 已添加到购物车"
        }
    }
    
    /**
     * 模拟收藏产品
     * @param product 要收藏的产品
     */
    fun addToFavorites(product: Product) {
        _toastMessage.value = "${product.name} 已添加到收藏夹"
    }
    
    /**
     * 清除Toast消息
     * 防止消息重复显示
     */
    fun clearToastMessage() {
        _toastMessage.value = ""
    }
}