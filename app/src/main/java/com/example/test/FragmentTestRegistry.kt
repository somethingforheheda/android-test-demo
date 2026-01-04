package com.example.test

import androidx.fragment.app.Fragment

/**
 * Fragment测试注册表
 * 用于管理所有Fragment类型的测试项目
 * 
 * 功能：
 * 1. 维护Fragment测试项目的注册表
 * 2. 提供注册和获取测试项目的接口
 * 3. 使用LinkedHashMap保持注册顺序
 * 
 * 使用方式：
 * 1. 在TestRegisterManager中注册新的Fragment测试
 * 2. MainActivity会自动生成对应的测试按钮
 * 3. 点击按钮后会通过FragmentContainerActivity展示Fragment
 */
object FragmentTestRegistry {
    // 使用LinkedHashMap保持插入顺序，确保按钮显示顺序一致
    private val fragmentMap = linkedMapOf<String, Class<out Fragment>>()

    /**
     * 注册Fragment测试项目
     * @param name 测试项目名称（显示在按钮上）
     * @param clazz Fragment类
     */
    fun register(name: String, clazz: Class<out Fragment>) {
        fragmentMap[name] = clazz
    }

    /**
     * 获取所有已注册的Fragment测试项目
     * @return 测试项目映射表（名称 -> Fragment类）
     */
    fun getFragments(): Map<String, Class<out Fragment>> = fragmentMap
}
