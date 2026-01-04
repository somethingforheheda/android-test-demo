package com.example.test.mvp.contract

import com.example.test.mvp.model.User

/**
 * MVP用户模块契约接口
 * 定义了View、Presenter、Model之间的通信契约
 */
interface UserContract {
    
    /**
     * View接口 - 定义UI层需要实现的方法
     */
    interface View {
        /**
         * 显示加载状态
         */
        fun showLoading()
        
        /**
         * 隐藏加载状态
         */
        fun hideLoading()
        
        /**
         * 显示用户信息
         * @param user 用户信息
         */
        fun showUser(user: User)
        
        /**
         * 显示错误信息
         * @param message 错误消息
         */
        fun showError(message: String)
        
        /**
         * 显示用户列表
         * @param users 用户列表
         */
        fun showUserList(users: List<User>)
    }
    
    /**
     * Presenter接口 - 定义业务逻辑层需要实现的方法
     */
    interface Presenter {
        /**
         * 绑定View
         * @param view View实例
         */
        fun attachView(view: View)
        
        /**
         * 解绑View，防止内存泄漏
         */
        fun detachView()
        
        /**
         * 加载用户信息
         * @param userId 用户ID
         */
        fun loadUser(userId: String)
        
        /**
         * 加载用户列表
         */
        fun loadUserList()
        
        /**
         * 刷新用户信息
         * @param userId 用户ID
         */
        fun refreshUser(userId: String)
    }
    
    /**
     * Model接口 - 定义数据层需要实现的方法
     */
    interface Model {
        /**
         * 获取用户信息
         * @param userId 用户ID
         * @param callback 回调接口
         */
        fun getUser(userId: String, callback: DataCallback<User>)
        
        /**
         * 获取用户列表
         * @param callback 回调接口
         */
        fun getUserList(callback: DataCallback<List<User>>)
        
        /**
         * 数据回调接口
         * @param T 数据类型
         */
        interface DataCallback<T> {
            /**
             * 成功回调
             * @param data 返回的数据
             */
            fun onSuccess(data: T)
            
            /**
             * 失败回调
             * @param error 错误信息
             */
            fun onError(error: String)
        }
    }
}