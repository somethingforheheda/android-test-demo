# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个Android测试Demo工程，用于演示和学习各种Android开发技术和架构模式。项目采用Kotlin语言开发，包含MVP、MVVM架构演示，以及性能优化、UI组件等多个技术模块的示例代码。

## 常用开发命令

### 构建和运行
```bash
# 构建Debug版本
./gradlew assembleDebug

# 安装到设备
./gradlew installDebug

# 清理构建
./gradlew clean

# 完整构建（含测试）
./gradlew build

# 运行单元测试
./gradlew test

# 运行Android仪器测试
./gradlew connectedAndroidTest
```

### 启动应用
```bash
# 启动主Activity
adb shell am start -n com.example.test/com.example.test.MainActivity
```

## 核心架构设计

### 测试框架注册系统
项目使用注册模式管理所有测试Demo：

1. **TestRegistry** - Activity测试注册表，管理所有Activity类型的测试项目
2. **FragmentTestRegistry** - Fragment测试注册表，管理Fragment类型的测试项目  
3. **TestRegisterManager** - 统一注册管理器，在`registerAll()`中集中注册所有测试项

新增测试时，在TestRegisterManager中注册即可自动在主界面生成对应按钮。

### 模块组织结构

```
app/src/main/java/com/example/test/
├── MainActivity.kt              # 主入口，动态生成测试按钮列表
├── FragmentListActivity.kt      # Fragment测试集合界面
├── mvp/                        # MVP架构演示模块
│   ├── contract/               # 契约接口定义
│   ├── model/                  # 数据模型层
│   ├── presenter/              # 业务逻辑层
│   └── view/                   # 视图层
├── mvvm/                       # MVVM架构演示模块
│   ├── model/                  # 数据模型和Repository
│   ├── viewmodel/              # ViewModel层
│   └── view/                   # Activity和Adapter
├── idletask/                   # 空闲任务执行器演示
├── frametask/                  # 帧任务分割器演示
├── performance/                # 性能分析工具演示
└── tests/                      # 各类UI和功能测试
```

### 关键技术特性

1. **ViewBinding** - 项目启用了ViewBinding，在build.gradle中配置
2. **DataBinding** - 已启用但当前主要使用ViewBinding
3. **Compose** - 已配置Compose支持但未实际使用
4. **LiveData + ViewModel** - MVVM模块使用Architecture Components
5. **协程** - ViewModel中使用viewModelScope管理协程生命周期
6. **RxJava** - 包含RxJava 2.x的测试示例

### 依赖版本管理

项目使用Version Catalog (gradle/libs.versions.toml) 统一管理依赖版本：
- Android Gradle Plugin: 8.4.1
- Kotlin: 1.9.0
- Target SDK: 34
- Min SDK: 24

## 添加新功能指南

### 添加新的Activity测试
1. 创建新的Activity类
2. 在TestRegisterManager.registerAll()中注册：
   ```kotlin
   TestRegistry.register("测试名称", YourActivity::class.java)
   ```

### 添加新的Fragment测试
1. 创建新的Fragment类
2. 在TestRegisterManager.registerAll()中注册：
   ```kotlin
   FragmentTestRegistry.register("测试名称", YourFragment::class.java)
   ```

### 性能分析
项目包含Perfetto trace文件用于性能分析，相关演示在PerfettoDemoActivity中。