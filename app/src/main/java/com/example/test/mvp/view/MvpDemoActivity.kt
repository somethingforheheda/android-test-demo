package com.example.test.mvp.view

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.mvp.contract.UserContract
import com.example.test.mvp.model.User
import com.example.test.mvp.presenter.UserPresenter

/**
 * MVP架构演示Activity
 * 作为View层的实现，负责UI显示和用户交互
 * 
 * MVP架构说明：
 * - Model：数据层，负责数据的获取和处理
 * - View：视图层，负责UI的展示和用户交互
 * - Presenter：业务逻辑层，连接Model和View，处理业务逻辑
 */
class MvpDemoActivity : AppCompatActivity(), UserContract.View {
    
    // Presenter实例
    private lateinit var presenter: UserPresenter
    
    // UI组件
    private lateinit var btnLoadUser: Button
    private lateinit var btnLoadUsers: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var cardUserInfo: CardView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserAge: TextView
    private lateinit var tvUserPhone: TextView
    private lateinit var tvUserAddress: TextView
    private lateinit var rvUserList: RecyclerView
    private lateinit var tvErrorMessage: TextView
    
    // 用户列表适配器
    private lateinit var userListAdapter: UserListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mvp_demo)
        runOnUiThread {  }
        // 初始化UI组件
        initViews()


        // 初始化Presenter
        initPresenter()
        
        // 设置事件监听器
        setupListeners()
        
        // 初始化RecyclerView
        setupRecyclerView()
    }
    
    /**
     * 初始化UI组件
     */
    private fun initViews() {
        btnLoadUser = findViewById(R.id.btn_load_user)
        btnLoadUsers = findViewById(R.id.btn_load_users)
        progressBar = findViewById(R.id.progress_bar)
        cardUserInfo = findViewById(R.id.card_user_info)
        tvUserName = findViewById(R.id.tv_user_name)
        tvUserEmail = findViewById(R.id.tv_user_email)
        tvUserAge = findViewById(R.id.tv_user_age)
        tvUserPhone = findViewById(R.id.tv_user_phone)
        tvUserAddress = findViewById(R.id.tv_user_address)
        rvUserList = findViewById(R.id.rv_user_list)
        tvErrorMessage = findViewById(R.id.tv_error_message)
    }
    
    /**
     * 初始化Presenter并绑定View
     */
    private fun initPresenter() {
        presenter = UserPresenter()
        presenter.attachView(this)
    }
    
    /**
     * 设置事件监听器
     */
    private fun setupListeners() {
        // 加载单个用户按钮点击事件
        btnLoadUser.setOnClickListener {
            hideAllViews()
            presenter.loadUser("user_001")
        }
        
        // 加载用户列表按钮点击事件
        btnLoadUsers.setOnClickListener {
            hideAllViews()
            presenter.loadUserList()
        }
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        userListAdapter = UserListAdapter()
        rvUserList.apply {
            layoutManager = LinearLayoutManager(this@MvpDemoActivity)
            adapter = userListAdapter
        }
        
        // 设置列表项点击事件
        userListAdapter.setOnItemClickListener { user ->
            Toast.makeText(this, "点击了用户：${user.name}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 隐藏所有内容视图
     */
    private fun hideAllViews() {
        cardUserInfo.visibility = View.GONE
        rvUserList.visibility = View.GONE
        tvErrorMessage.visibility = View.GONE
    }
    
    // ============= MVP View接口实现 =============
    
    /**
     * 显示加载状态
     * 实现UserContract.View接口方法
     */
    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        btnLoadUser.isEnabled = false
        btnLoadUsers.isEnabled = false
    }
    
    /**
     * 隐藏加载状态
     * 实现UserContract.View接口方法
     */
    override fun hideLoading() {
        progressBar.visibility = View.GONE
        btnLoadUser.isEnabled = true
        btnLoadUsers.isEnabled = true
    }
    
    /**
     * 显示用户信息
     * 实现UserContract.View接口方法
     * @param user 用户信息对象
     */
    override fun showUser(user: User) {
        cardUserInfo.visibility = View.VISIBLE
        
        // 设置用户信息到UI组件
        tvUserName.text = "姓名：${user.name}"
        tvUserEmail.text = "邮箱：${user.email}"
        tvUserAge.text = "年龄：${user.age}岁"
        tvUserPhone.text = "手机：${user.phone ?: "未提供"}"
        tvUserAddress.text = "地址：${user.address ?: "未提供"}"
        
        // 显示成功提示
        Toast.makeText(this, "用户信息加载成功", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示错误信息
     * 实现UserContract.View接口方法
     * @param message 错误消息
     */
    override fun showError(message: String) {
        tvErrorMessage.text = message
        tvErrorMessage.visibility = View.VISIBLE
        
        // 显示错误提示
        Toast.makeText(this, "加载失败：$message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示用户列表
     * 实现UserContract.View接口方法
     * @param users 用户列表
     */
    override fun showUserList(users: List<User>) {
        rvUserList.visibility = View.VISIBLE
        userListAdapter.updateUsers(users)
        
        // 显示成功提示
        Toast.makeText(this, "用户列表加载成功，共${users.size}个用户", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Activity销毁时解绑Presenter
     * 防止内存泄漏
     */
    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}