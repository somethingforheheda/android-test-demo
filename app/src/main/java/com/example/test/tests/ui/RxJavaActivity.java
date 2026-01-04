package com.example.test.tests.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.test.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava响应式编程演示Activity
 * 
 * 功能说明：
 * 1. 演示RxJava的基本使用方法
 * 2. 展示Observable的创建和订阅
 * 3. 演示线程调度和切换
 * 4. 展示观察者模式的实现
 * 
 * RxJava核心概念：
 * - Observable：可观察对象，数据源
 * - Observer：观察者，数据消费者
 * - Subscription：订阅关系
 * - Scheduler：调度器，控制线程切换
 * 
 * 学习要点：
 * 1. 理解响应式编程思想
 * 2. 掌握Observable的创建方式
 * 3. 理解线程调度的重要性
 * 4. 学会处理异步数据流
 */

public class RxJavaActivity extends AppCompatActivity {

    private static final String TAG = "RxJavaActivity";

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);

        // 获取用于显示结果的TextView
        TextView textView = findViewById(R.id.rx_result);

        // ============= RxJava基础示例 =============
        
        // 示例1：使用Observable.just()创建简单的Observable
        // just()会立即发射提供的数据，然后完成
        Observable.just(doSomething())
                .subscribeOn(Schedulers.io())              // 指定订阅线程（数据产生线程）
                .observeOn(AndroidSchedulers.mainThread()) // 指定观察线程（数据消费线程）
                .subscribe(result -> {
                    // 在主线程中更新UI
                    textView.setText("结果: " + result);
                    Log.d(TAG, "Observable.just() 结果：" + result);
                    Toast.makeText(this, "just()结果: " + result, Toast.LENGTH_SHORT).show();
                });

        // 示例2：使用Observable.create()自定义Observable
        // create()方法允许我们完全控制数据的发射过程
        Observable.create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Exception {
                        try {
                            // 模拟一些耗时操作
                            Thread.sleep(1000);
                            
                            // 发射数据
                            emitter.onNext("第一条数据");
                            emitter.onNext("第二条数据");
                            emitter.onNext("第三条数据");
                            
                            // 标记完成
                            emitter.onComplete();
                            
                        } catch (Exception e) {
                            // 发生错误时通知观察者
                            emitter.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())              // 在IO线程执行耗时操作
                .observeOn(AndroidSchedulers.mainThread()) // 在主线程处理结果
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        // 订阅开始时调用
                        Log.d(TAG, "Observable.create() - 开始订阅");
                    }

                    @Override
                    public void onNext(@NonNull String data) {
                        // 接收到数据时调用
                        Log.d(TAG, "Observable.create() - 接收数据: " + data);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // 发生错误时调用
                        Log.e(TAG, "Observable.create() - 发生错误: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        // 数据流完成时调用
                        Log.d(TAG, "Observable.create() - 数据流完成");
                        Toast.makeText(RxJavaActivity.this, "自定义Observable执行完成", Toast.LENGTH_SHORT).show();
                    }
                });

        // 示例3：演示错误处理
        demoErrorHandling();
        
        // 示例4：演示数据变换
        demoDataTransformation();
    }

    /**
     * 演示RxJava的错误处理机制
     */
    private void demoErrorHandling() {
        Observable.create(emitter -> {
                    // 模拟可能出错的操作
                    int randomValue = (int) (Math.random() * 10);
                    if (randomValue < 5) {
                        emitter.onError(new RuntimeException("模拟网络错误: 随机值=" + randomValue));
                    } else {
                        emitter.onNext("成功获取数据: " + randomValue);
                        emitter.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // 成功回调
                        result -> {
                            Log.d(TAG, "错误处理示例 - 成功: " + result);
                            Toast.makeText(this, "成功: " + result, Toast.LENGTH_SHORT).show();
                        },
                        // 错误回调
                        throwable -> {
                            Log.e(TAG, "错误处理示例 - 失败: " + throwable.getMessage());
                            Toast.makeText(this, "失败: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                );
    }

    /**
     * 演示RxJava的数据变换操作符
     */
    private void demoDataTransformation() {
        Observable.just(1, 2, 3, 4, 5)
                .map(number -> "数字: " + number)          // map操作符：转换数据类型
                .filter(text -> !text.contains("3"))      // filter操作符：过滤数据
                .subscribeOn(Schedulers.computation())     // 在计算线程执行
                .observeOn(AndroidSchedulers.mainThread()) // 在主线程观察结果
                .subscribe(
                        result -> Log.d(TAG, "数据变换示例: " + result),
                        error -> Log.e(TAG, "数据变换错误: " + error.getMessage()),
                        () -> {
                            Log.d(TAG, "数据变换完成");
                            Toast.makeText(this, "数据变换演示完成，查看日志", Toast.LENGTH_SHORT).show();
                        }
                );
    }

    /**
     * 模拟一个耗时操作
     * @return 操作结果字符串
     */
    private String doSomething() {
        Log.d(TAG, "doSomething() 方法被调用 - 模拟耗时操作");
        // 模拟耗时操作
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Hello from RxJava!";
    }

    /**
     * 创建一个Observable的工厂方法
     * 注意：使用just()时，doSomething()会立即执行，而不是在订阅时执行
     * @return Observable对象
     */
    private Observable<String> createObservable() {
        return Observable.just(doSomething());
    }

    /**
     * 演示方法 - 显示Toast消息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}