package com.example.test.mvvm.model

/**
 * MVVM架构演示 - 产品数据模型
 * 用于演示商品列表的数据实体
 */
data class Product(
    /**
     * 产品ID
     */
    val id: String,
    
    /**
     * 产品名称
     */
    val name: String,
    
    /**
     * 产品价格
     */
    val price: Double,
    
    /**
     * 产品描述
     */
    val description: String,
    
    /**
     * 产品图片URL
     */
    val imageUrl: String? = null,
    
    /**
     * 产品分类
     */
    val category: String,
    
    /**
     * 库存数量
     */
    val stock: Int,
    
    /**
     * 是否推荐
     */
    val isRecommended: Boolean = false,
    
    /**
     * 产品评分 (0.0 - 5.0)
     */
    val rating: Float = 0.0f
) {
    /**
     * 格式化价格显示
     * @return 格式化后的价格字符串
     */
    fun getFormattedPrice(): String {
        return "¥%.2f".format(price)
    }
    
    /**
     * 获取库存状态
     * @return 库存状态描述
     */
    fun getStockStatus(): String {
        return when {
            stock <= 0 -> "缺货"
            stock <= 10 -> "库存紧张"
            else -> "现货"
        }
    }
    
    /**
     * 获取评分星级显示
     * @return 星级字符串
     */
    fun getRatingStars(): String {
        val fullStars = rating.toInt()
        val hasHalfStar = rating - fullStars >= 0.5
        val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0
        
        return "★".repeat(fullStars) + 
               (if (hasHalfStar) "☆" else "") + 
               "☆".repeat(emptyStars)
    }
}