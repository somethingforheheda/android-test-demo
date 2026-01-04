package com.example.test.mvp.presenter

import com.example.test.mvp.contract.UserContract
import com.example.test.mvp.model.User
import com.example.test.mvp.model.UserModel

/**
 * MVP架构中的Presenter层实现
 * 负责处理业务逻辑，连接View和Model
 */
class UserPresenter : UserContract.Presenter {
    
    // View层引用，使用弱引用防止内存泄漏
    private var view: UserContract.View? = null
    
    // Model层实例
    private val model: UserContract.Model = UserModel()
    
    /**
     * 绑定View层
     * 在Activity或Fragment的onCreate或onResume中调用
     */
    override fun attachView(view: UserContract.View) {
        this.view = view
    }
    
    /**
     * 解绑View层
     * 在Activity或Fragment的onDestroy或onPause中调用，防止内存泄漏
     */
    override fun detachView() {
        this.view = null
    }
    
    /**
     * 加载用户信息
     * 展示加载状态，调用Model获取数据，处理结果
     */
    override fun loadUser(userId: String) {
        // 检查View是否存在
        view?.let { currentView ->
            // 显示加载状态
            currentView.showLoading()
            
            // 调用Model层获取数据
            model.getUser(userId, object : UserContract.Model.DataCallback<User> {
                override fun onSuccess(data: User) {
                    // 确保View仍然存在（防止异步回调时View已销毁）
                    view?.let {
                        it.hideLoading()
                        it.showUser(data)
                    }
                }
                
                override fun onError(error: String) {
                    // 确保View仍然存在
                    view?.let {
                        it.hideLoading()
                        it.showError(error)
                    }
                }
            })
        }
    }
    
    /**
     * 加载用户列表
     * 展示加载状态，调用Model获取列表数据，处理结果
     */
    override fun loadUserList() {
        view?.let { currentView ->
            // 显示加载状态
            currentView.showLoading()
            
            // 调用Model层获取用户列表
            model.getUserList(object : UserContract.Model.DataCallback<List<User>> {
                override fun onSuccess(data: List<User>) {
                    view?.let {
                        it.hideLoading()
                        it.showUserList(data)
                    }
                }
                
                override fun onError(error: String) {
                    view?.let {
                        it.hideLoading()
                        it.showError(error)
                    }
                }
            })
        }
    }
    
    /**
     * 刷新用户信息
     * 重新加载用户数据
     */
    override fun refreshUser(userId: String) {
        loadUser(userId)
    }
    
    /**
     * 检查View是否已绑定
     * @return true如果View已绑定，false如果View未绑定
     */
    private fun isViewAttached(): Boolean {
        return view != null
    }
}