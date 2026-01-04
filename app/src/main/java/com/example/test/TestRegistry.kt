package com.example.test

import android.app.Activity

/**
 * Activity测试注册表
 * 用于管理所有Activity类型的测试项目
 * 
 * 功能：
 * 1. 维护Activity测试项目的注册表
 * 2. 提供注册和获取测试项目的接口
 * 3. 使用LinkedHashMap保持注册顺序
 * 
 * 使用方式：
 * 1. 在TestRegisterManager中注册新的Activity测试
 * 2. MainActivity会自动生成对应的测试按钮
 */
object TestRegistry {
    // 使用LinkedHashMap保持插入顺序，确保按钮显示顺序一致
    private val testMap = linkedMapOf<String, Class<out Activity>>()

    /**
     * 注册Activity测试项目
     * @param name 测试项目名称（显示在按钮上）
     * @param clazz Activity类
     */
    fun register(name: String, clazz: Class<out Activity>) {
        testMap[name] = clazz
    }

    /**
     * 获取所有已注册的Activity测试项目
     * @return 测试项目映射表（名称 -> Activity类）
     */
    fun getTests(): Map<String, Class<out Activity>> = testMap
}
