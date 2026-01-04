package com.example.test.mvvm.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.mvvm.model.Product

/**
 * MVVM架构演示 - 产品列表适配器
 * 使用ListAdapter和DiffUtil实现高效的列表更新
 */
class ProductAdapter : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {
    
    // 点击事件监听器
    private var onItemClickListener: ((Product) -> Unit)? = null
    private var onAddToCartListener: ((Product) -> Unit)? = null
    private var onAddToFavoritesListener: ((Product) -> Unit)? = null
    
    /**
     * 设置条目点击监听器
     */
    fun setOnItemClickListener(listener: (Product) -> Unit) {
        onItemClickListener = listener
    }
    
    /**
     * 设置添加到购物车监听器
     */
    fun setOnAddToCartListener(listener: (Product) -> Unit) {
        onAddToCartListener = listener
    }
    
    /**
     * 设置添加到收藏夹监听器
     */
    fun setOnAddToFavoritesListener(listener: (Product) -> Unit) {
        onAddToFavoritesListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        
        // 设置点击事件
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(product)
        }
        
        // 设置按钮点击事件
        holder.btnAddToCart.setOnClickListener {
            onAddToCartListener?.invoke(product)
        }
        
        holder.btnAddToFavorites.setOnClickListener {
            onAddToFavoritesListener?.invoke(product)
        }
    }
    
    /**
     * 产品ViewHolder
     */
    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val tvRecommended: TextView = itemView.findViewById(R.id.tv_recommended)
        private val tvStockStatus: TextView = itemView.findViewById(R.id.tv_stock_status)
        val btnAddToCart: Button = itemView.findViewById(R.id.btn_add_to_cart)
        val btnAddToFavorites: Button = itemView.findViewById(R.id.btn_add_to_favorites)
        
        /**
         * 绑定产品数据到视图
         */
        fun bind(product: Product) {
            // 设置基本信息
            tvProductName.text = product.name
            tvCategory.text = product.category
            tvDescription.text = product.description
            tvPrice.text = product.getFormattedPrice()
            tvRating.text = product.getRatingStars()
            
            // 设置推荐标签
            tvRecommended.visibility = if (product.isRecommended) {
                View.VISIBLE
            } else {
                View.GONE
            }
            
            // 设置库存状态
            tvStockStatus.text = product.getStockStatus()
            tvStockStatus.setBackgroundColor(getStockStatusColor(product))
            
            // 设置按钮状态
            btnAddToCart.isEnabled = product.stock > 0
            btnAddToCart.text = if (product.stock > 0) "加购物车" else "缺货"
        }
        
        /**
         * 根据库存状态获取对应的颜色
         */
        private fun getStockStatusColor(product: Product): Int {
            return when {
                product.stock <= 0 -> android.graphics.Color.parseColor("#F44336") // 红色
                product.stock <= 10 -> android.graphics.Color.parseColor("#FF9800") // 橙色
                else -> android.graphics.Color.parseColor("#4CAF50") // 绿色
            }
        }
    }
    
    /**
     * DiffUtil回调，用于高效地计算列表差异
     */
    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        
        /**
         * 判断是否为同一个条目
         */
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }
        
        /**
         * 判断条目内容是否相同
         */
        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}