package com.example.test

import com.example.test.TestRegistry.register
import com.example.test.idletask.IdleTaskDemoFragment
import com.example.test.frametask.FrameTaskSplitterDemoFragment
import com.example.test.prioritytask.PriorityTaskDemoFragment
import com.example.test.mvp.view.MvpDemoActivity
import com.example.test.tests.fragment.DemoFragment
import com.example.test.tests.fragment.DemoTwoFragment
import com.example.test.tests.fragment.ViewBindingDemoFragment
import com.example.test.tests.fragment.RxJavaFragment
import com.example.test.tests.ui.RxJavaActivity
import com.example.test.tests.ui.ShadowTextTestActivity

/**
 * 测试注册管理器
 * 统一管理所有测试Activity和Fragment的注册
 */
object TestRegisterManager {
    /**
     * 注册所有测试项目
     * 在MainActivity中调用，用于生成测试按钮列表
     */
    fun registerAll() {
        // UI测试相关
        TestRegistry.register("UI 阴影测试", ShadowTextTestActivity::class.java)
        
        // 架构模式演示
        register("MVP架构演示", MvpDemoActivity::class.java)
        register("MVVM架构演示", com.example.test.mvvm.view.MvvmDemoActivity::class.java)
        
        // RxJava相关测试
        register("RxJava 测试", RxJavaActivity::class.java)
        
        // Fragment测试相关
        FragmentTestRegistry.register("Demo Fragment", DemoFragment::class.java)
        FragmentTestRegistry.register("Demo Two Fragment", DemoTwoFragment::class.java)
        FragmentTestRegistry.register("ViewBinding 使用演示", ViewBindingDemoFragment::class.java)
        FragmentTestRegistry.register("RxJava测试", RxJavaFragment::class.java)
        FragmentTestRegistry.register("IdleTaskExecutor 演示", IdleTaskDemoFragment::class.java)
        FragmentTestRegistry.register("FrameTaskSplitter 演示", FrameTaskSplitterDemoFragment::class.java)
        FragmentTestRegistry.register("PriorityTaskSplitter 优先级调度演示", PriorityTaskDemoFragment::class.java)
        
        // Perfetto Trace 演示
        register("Perfetto Trace 演示", com.example.test.performance.PerfettoDemoActivity::class.java)
    }
}
