package com.example.test.mvp.model

/**
 * 用户数据模型
 * 用于演示MVP架构中的数据实体
 */
data class User(
    /**
     * 用户ID
     */
    val id: String,
    
    /**
     * 用户名
     */
    val name: String,
    
    /**
     * 用户邮箱
     */
    val email: String,
    
    /**
     * 用户年龄
     */
    val age: Int,
    
    /**
     * 用户头像URL
     */
    val avatarUrl: String? = null,
    
    /**
     * 用户手机号
     */
    val phone: String? = null,
    
    /**
     * 用户地址
     */
    val address: String? = null
)