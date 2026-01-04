package com.example.test.mvvm.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * MVVM架构演示 - 产品数据仓库
 * 负责管理产品数据的获取和缓存
 * 在实际项目中，这里会整合网络请求和本地数据库
 */
class ProductRepository {
    
    // 私有的可变LiveData，内部使用
    private val _products = MutableLiveData<List<Product>>()
    
    // 公开的不可变LiveData，供外部观察
    val products: LiveData<List<Product>> = _products
    
    // 加载状态LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    // 错误信息LiveData
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    // 缓存的产品列表
    private var cachedProducts: List<Product>? = null
    
    /**
     * 加载产品列表
     * 模拟网络请求过程
     */
    suspend fun loadProducts() {
        try {
            // 设置加载状态
            _isLoading.value = true
            _error.value = ""
            
            // 模拟网络延时
            delay(2000)
            
            // 模拟网络请求可能失败
            if (Random.nextFloat() < 0.2) { // 20%概率失败
                throw Exception("网络连接失败，请检查网络设置")
            }
            
            // 生成模拟产品数据
            val productList = generateMockProducts()
            
            // 缓存数据
            cachedProducts = productList
            
            // 更新LiveData
            _products.value = productList
            
        } catch (e: Exception) {
            // 处理异常
            _error.value = e.message ?: "未知错误"
        } finally {
            // 结束加载状态
            _isLoading.value = false
        }
    }
    
    /**
     * 刷新产品列表
     * 强制重新获取数据
     */
    suspend fun refreshProducts() {
        cachedProducts = null
        loadProducts()
    }
    
    /**
     * 根据分类筛选产品
     * @param category 产品分类，null表示显示所有产品
     */
    fun filterByCategory(category: String?) {
        val allProducts = cachedProducts ?: return
        
        val filteredProducts = if (category.isNullOrEmpty()) {
            allProducts
        } else {
            allProducts.filter { it.category == category }
        }
        
        _products.value = filteredProducts
    }
    
    /**
     * 搜索产品
     * @param query 搜索关键词
     */
    fun searchProducts(query: String) {
        val allProducts = cachedProducts ?: return
        
        val searchResults = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true)
            }
        }
        
        _products.value = searchResults
    }
    
    /**
     * 获取产品分类列表
     * @return 所有可用的产品分类
     */
    fun getCategories(): List<String> {
        return cachedProducts?.map { it.category }?.distinct() ?: emptyList()
    }
    
    /**
     * 生成模拟产品数据
     * @return 产品列表
     */
    private fun generateMockProducts(): List<Product> {
        val categories = listOf("电子产品", "服装", "食品", "图书", "家居", "运动")
        val productNames = mapOf(
            "电子产品" to listOf("智能手机", "笔记本电脑", "无线耳机", "平板电脑", "智能手表"),
            "服装" to listOf("T恤", "牛仔裤", "运动鞋", "夹克", "连衣裙"),
            "食品" to listOf("有机苹果", "进口咖啡", "手工巧克力", "天然蜂蜜", "精选茶叶"),
            "图书" to listOf("编程入门", "设计思维", "历史故事", "科学探索", "小说集"),
            "家居" to listOf("智能台灯", "舒适抱枕", "收纳盒", "装饰画", "绿植盆栽"),
            "运动" to listOf("瑜伽垫", "跑步鞋", "哑铃", "运动水杯", "健身手套")
        )
        
        val products = mutableListOf<Product>()
        
        categories.forEach { category ->
            val names = productNames[category] ?: emptyList()
            names.forEachIndexed { index, name ->
                val product = Product(
                    id = "${category}_${index + 1}",
                    name = name,
                    price = Random.nextDouble(50.0, 2000.0),
                    description = "这是一个优质的${name}，具有出色的性能和品质保证。",
                    imageUrl = "https://picsum.photos/300/300?random=${Random.nextInt(1000)}",
                    category = category,
                    stock = Random.nextInt(0, 100),
                    isRecommended = Random.nextBoolean(),
                    rating = Random.nextFloat() * 5f
                )
                products.add(product)
            }
        }
        
        return products.shuffled() // 随机排序
    }
}