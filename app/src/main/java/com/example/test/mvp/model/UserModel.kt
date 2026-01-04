package com.example.test.mvp.model

import android.os.Handler
import android.os.Looper
import com.example.test.mvp.contract.UserContract
import kotlin.random.Random

/**
 * MVP架构中的Model层实现
 * 负责数据的获取和处理，模拟网络请求
 */
class UserModel : UserContract.Model {
    
    // 主线程Handler，用于模拟异步操作
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 模拟获取用户信息
     * 通过延时操作模拟网络请求过程
     */
    override fun getUser(userId: String, callback: UserContract.Model.DataCallback<User>) {
        // 模拟网络请求延时
        mainHandler.postDelayed({
            try {
                // 模拟随机成功或失败
                if (Random.nextBoolean()) {
                    val user = createMockUser(userId)
                    callback.onSuccess(user)
                } else {
                    callback.onError("网络请求失败：无法获取用户信息")
                }
            } catch (e: Exception) {
                callback.onError("数据解析错误：${e.message}")
            }
        }, 1500) // 1.5秒延时模拟网络请求
    }
    
    /**
     * 模拟获取用户列表
     * 通过延时操作模拟网络请求过程
     */
    override fun getUserList(callback: UserContract.Model.DataCallback<List<User>>) {
        // 模拟网络请求延时
        mainHandler.postDelayed({
            try {
                val userList = createMockUserList()
                callback.onSuccess(userList)
            } catch (e: Exception) {
                callback.onError("获取用户列表失败：${e.message}")
            }
        }, 2000) // 2秒延时模拟网络请求
    }
    
    /**
     * 创建模拟用户数据
     * @param userId 用户ID
     * @return 模拟的用户对象
     */
    private fun createMockUser(userId: String): User {
        return User(
            id = userId,
            name = "用户$userId",
            email = "user$userId@example.com",
            age = Random.nextInt(18, 65),
            avatarUrl = "https://picsum.photos/200/200?random=$userId",
            phone = "1${Random.nextInt(3, 9)}${Random.nextInt(100000000, 999999999)}",
            address = "北京市海淀区中关村软件园${Random.nextInt(1, 20)}号楼"
        )
    }
    
    /**
     * 创建模拟用户列表数据
     * @return 模拟的用户列表
     */
    private fun createMockUserList(): List<User> {
        val userList = mutableListOf<User>()
        val names = listOf("张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十")
        
        repeat(8) { index ->
            val user = User(
                id = "user_${index + 1}",
                name = names[index],
                email = "${names[index].lowercase()}@company.com",
                age = Random.nextInt(22, 50),
                avatarUrl = "https://picsum.photos/200/200?random=${index + 100}",
                phone = "1${Random.nextInt(3, 9)}${Random.nextInt(100000000, 999999999)}",
                address = getRandomAddress()
            )
            userList.add(user)
        }
        
        return userList
    }
    
    /**
     * 获取随机地址
     * @return 随机生成的地址字符串
     */
    private fun getRandomAddress(): String {
        val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "西安")
        val districts = listOf("朝阳区", "海淀区", "西城区", "东城区", "丰台区", "石景山区")
        val streets = listOf("中关村大街", "建国门外大街", "王府井大街", "长安街", "三里屯路")
        
        return "${cities.random()}${districts.random()}${streets.random()}${Random.nextInt(1, 999)}号"
    }
}