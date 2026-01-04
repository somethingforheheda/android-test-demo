package com.example.test.tests.viewbinding

/**
 * ViewBinding功能展示项的数据模型
 * 
 * 用于在RecyclerView中展示ViewBinding的各种特性
 * 每个FeatureItem代表一个ViewBinding的使用场景或特性
 * 
 * @property title 功能标题
 * @property description 功能描述
 * @property iconRes 图标资源ID
 * @property isEnabled 是否启用该功能
 */
data class FeatureItem(
    val title: String,
    val description: String,
    val iconRes: Int,
    val isEnabled: Boolean = true
)