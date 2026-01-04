package com.example.test.tests.viewbinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.test.databinding.ItemFeatureBinding

/**
 * ViewBinding功能展示列表适配器
 * 
 * 这个适配器展示了如何在RecyclerView中使用ViewBinding
 * 
 * 关键点：
 * 1. ViewHolder使用ViewBinding而不是传统的itemView
 * 2. 使用ListAdapter配合DiffUtil实现高效的列表更新
 * 3. 在ViewHolder中直接通过binding访问视图
 * 4. 避免了findViewById的性能开销
 * 
 * @param onItemClick 列表项点击回调
 */
class FeatureAdapter(
    private val onItemClick: (FeatureItem) -> Unit
) : ListAdapter<FeatureItem, FeatureAdapter.FeatureViewHolder>(FeatureDiffCallback()) {
    
    /**
     * 创建ViewHolder
     * 使用ViewBinding创建视图，而不是传统的inflate方式
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        // 使用ViewBinding的inflate方法创建视图
        // 这是RecyclerView中使用ViewBinding的标准方式
        val binding = ItemFeatureBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return FeatureViewHolder(binding, onItemClick)
    }
    
    /**
     * 绑定数据到ViewHolder
     */
    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder类
     * 使用ViewBinding而不是传统的View引用
     * 
     * @property binding 布局的ViewBinding实例
     * @property onItemClick 点击事件回调
     */
    class FeatureViewHolder(
        private val binding: ItemFeatureBinding,
        private val onItemClick: (FeatureItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        // 当前绑定的数据项
        private var currentItem: FeatureItem? = null
        
        init {
            // 设置整个item的点击事件
            // 注意：使用binding.root访问根视图
            binding.root.setOnClickListener {
                currentItem?.let { onItemClick(it) }
            }
        }
        
        /**
         * 绑定数据到视图
         * 展示ViewBinding在ViewHolder中的使用优势
         */
        fun bind(item: FeatureItem) {
            currentItem = item
            
            // 通过binding直接访问视图，类型安全且高效
            // 不需要findViewById，不需要类型转换
            binding.apply {
                // 设置图标
                featureIcon.setImageResource(item.iconRes)
                
                // 设置标题
                featureTitle.text = item.title
                
                // 设置描述
                featureDescription.text = item.description
                
                // 设置开关状态
                featureSwitch.isChecked = item.isEnabled
                
                // 处理开关状态变化
                // 注意：使用setOnCheckedChangeListener时要先移除之前的监听器
                // 防止在RecyclerView复用时触发错误的回调
                featureSwitch.setOnCheckedChangeListener(null)
                featureSwitch.setOnCheckedChangeListener { _, isChecked ->
                    // 这里可以处理开关状态变化
                    // 例如：更新数据模型，通知外部等
                    // 为了演示，我们只是简单地调用点击回调
                    currentItem?.let {
                        // 创建一个新的item实例，更新enabled状态
                        val updatedItem = it.copy(isEnabled = isChecked)
                        onItemClick(updatedItem)
                    }
                }
            }
        }
    }
    
    /**
     * DiffUtil回调类
     * 用于计算列表差异，实现高效的列表更新
     */
    class FeatureDiffCallback : DiffUtil.ItemCallback<FeatureItem>() {
        /**
         * 判断两个项目是否为同一个项目
         * 通常比较唯一标识符
         */
        override fun areItemsTheSame(oldItem: FeatureItem, newItem: FeatureItem): Boolean {
            // 这里我们使用title作为唯一标识
            // 在实际项目中，应该使用真正的唯一ID
            return oldItem.title == newItem.title
        }
        
        /**
         * 判断两个项目的内容是否相同
         * 用于决定是否需要更新UI
         */
        override fun areContentsTheSame(oldItem: FeatureItem, newItem: FeatureItem): Boolean {
            // 使用data class的自动生成的equals方法
            return oldItem == newItem
        }
    }
}