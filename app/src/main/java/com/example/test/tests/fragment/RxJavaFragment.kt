package com.example.test.tests.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.test.R
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.ReplaySubject
import io.reactivex.subjects.AsyncSubject
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

/**
 * RxJava测试Fragment
 * 简单的测试环境，包含一个按钮和空方法
 */
class RxJavaFragment : Fragment(R.layout.fragment_rxjava) {
    
    private val compositeDisposable = CompositeDisposable()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val btnTest = view.findViewById<Button>(R.id.btn_test)
        
        btnTest.setOnClickListener {
            testRxJava()
        }
    }
    
    /**
     * RxJava测试方法
     * 演示各种RxJava操作符
     */
    private fun testRxJava() {
        Log.d("wangning", "========== RxJava操作符测试开始 ==========")
        
        // 清理之前的订阅
        compositeDisposable.clear()
        
        // 1. 创建操作符
        testCreationOperators()
        
        // 2. 转换操作符
        testTransformationOperators()
        
        // 3. 过滤操作符
        testFilteringOperators()
        
        // 4. 组合操作符
        testCombiningOperators()
        
        // 5. 错误处理操作符
        testErrorHandlingOperators()
        
        // 6. 工具操作符
        testUtilityOperators()
        
        // 7. Subject测试
        testSubjects()
    }
    
    /**
     * 测试创建操作符
     * 创建操作符用于创建Observable实例，是RxJava数据流的起点
     */
    private fun testCreationOperators() {
        Log.d("wangning", "---------- 创建操作符 ----------")
        
        // create操作符：最基础的创建方式，手动控制数据发射
        // Observable.create 创建一个被观察者
        // emitter 是发射器，用于发射数据
        val createDisposable = Observable.create<String> { emitter ->
            // onNext：发射一个数据项
            emitter.onNext("create-1")
            emitter.onNext("create-2")
            emitter.onNext("create-3")
            // onComplete：标记数据流结束，之后不能再发射数据
            emitter.onComplete()
        }.subscribe(  // subscribe：订阅Observable，建立观察关系
            { Log.d("wangning", "create: $it") },  // onNext回调：接收每个数据项
            { Log.e("wangning", "create error: ${it.message}") },  // onError回调：处理错误
            { Log.d("wangning", "create completed") }  // onComplete回调：数据流结束
        )
        // 将订阅添加到CompositeDisposable统一管理，防止内存泄漏
        compositeDisposable.add(createDisposable)
        
        // just操作符：直接发射传入的参数，最多支持10个参数
        // 适用于已知固定数据的场景
        val justDisposable = Observable.just("just-A", "just-B", "just-C")
            .subscribe { Log.d("wangning", "just: $it") }
        compositeDisposable.add(justDisposable)

        // fromCallable操作符：延迟创建，在订阅时才执行Callable
        // Callable：Java的接口，返回一个结果
        val fromCallableDisposable = Observable.fromCallable(Callable {
            Thread.sleep(100)  // 模拟耗时操作
            "fromCallable-result"  // 返回结果
        })
            .subscribeOn(Schedulers.io())  // 指定上游在IO线程执行
            .observeOn(AndroidSchedulers.mainThread())  // 指定下游在主线程接收
            .subscribe { Log.d("wangning", "fromCallable: $it") }
        compositeDisposable.add(fromCallableDisposable)
        
        // fromArray操作符：将数组元素逐个发射
        // 注意：直接传入多个参数，不是传入数组对象
        val fromArrayDisposable = Observable.fromArray("array-1", "array-2", "array-3")
            .subscribe { Log.d("wangning", "fromArray: $it") }
        compositeDisposable.add(fromArrayDisposable)

        val arrayOf = arrayOf("url1", "url2", "url3")
        val disposable = Observable.fromArray(*arrayOf)
            .subscribe { Log.d("wangning", "*fromArray: $it") }
        compositeDisposable.add(disposable)

        // fromIterable操作符：将可迭代对象（List、Set等）的元素逐个发射
        // listOf：Kotlin创建不可变List的函数
        val fromIterableDisposable = Observable.fromIterable(listOf("iterable-X", "iterable-Y", "iterable-Z"))
            .subscribe { Log.d("wangning", "fromIterable: $it") }
        compositeDisposable.add(fromIterableDisposable)
        
        // range操作符：发射指定范围的整数序列
        // 参数1：起始值(10)，参数2：数量(5)，将发射10,11,12,13,14
        val rangeDisposable = Observable.range(10, 5)
            .subscribe { Log.d("wangning", "range: $it") }
        compositeDisposable.add(rangeDisposable)
        
        // interval操作符：按固定时间间隔发射递增的Long数字(从0开始)
        // 常用于定时轮询场景
        val intervalDisposable = Observable.interval(500, TimeUnit.MILLISECONDS)
            .take(3)  // 只取前3个，否则会无限发射
            .subscribe { Log.d("wangning", "interval: $it") }
        compositeDisposable.add(intervalDisposable)
        
        // timer操作符：延迟指定时间后发射一个0L
        // 常用于延迟执行场景
        val timerDisposable = Observable.timer(1, TimeUnit.SECONDS)
            .subscribe { Log.d("wangning", "timer: $it") }
        compositeDisposable.add(timerDisposable)
        
        // empty操作符：创建一个不发射数据但正常终止的Observable
        // 用于测试或占位场景
        val emptyDisposable = Observable.empty<String>()
            .subscribe(
                { Log.d("wangning", "empty: $it") },  // 不会执行
                { Log.e("wangning", "empty error: ${it.message}") },  // 不会执行
                { Log.d("wangning", "empty completed") }  // 只会执行这个
            )
        compositeDisposable.add(emptyDisposable)
        
        // never操作符：创建一个不发射数据也不终止的Observable
        // timeout：设置超时时间，超时后会发射onError
        val neverDisposable = Observable.never<String>()
            .timeout(2, TimeUnit.SECONDS)  // 2秒后超时
            .subscribe(
                { Log.d("wangning", "never: $it") },  // 永远不会执行
                { Log.e("wangning", "never timeout: ${it.message}") }  // 超时后执行
            )
        compositeDisposable.add(neverDisposable)
        
        // defer操作符：延迟创建Observable，每次订阅时都会创建新的Observable
        // 可以确保Observable包含最新的数据
        var deferValue = 0
        val deferObservable = Observable.defer {
            // 每次订阅时都会执行这个lambda
            Observable.just(++deferValue)  // 每次订阅都会递增
        }
        
        val deferDisposable1 = deferObservable.subscribe { 
            Log.d("wangning", "defer-1: $it")  // 输出1
        }
        compositeDisposable.add(deferDisposable1)
        
        val deferDisposable2 = deferObservable.subscribe { 
            Log.d("wangning", "defer-2: $it")  // 输出2
        }
        compositeDisposable.add(deferDisposable2)
    }
    
    /**
     * 测试转换操作符
     * 转换操作符用于将Observable发射的数据进行变换
     */
    private fun testTransformationOperators() {
        Log.d("wangning", "---------- 转换操作符 ----------")
        
        // map操作符：对每个发射的数据进行变换
        // 一对一转换，保持数据流的数量不变
        val mapDisposable = Observable.just(1, 2, 3)
            .map { it * 10 }  // 将每个数字乘10
            .subscribe { Log.d("wangning", "map: $it") }  // 输出10, 20, 30
        compositeDisposable.add(mapDisposable)
        
        // flatMap操作符：将每个数据转换为Observable，然后将所有Observable合并
        // 一对多转换，不保证顺序，适合并发场景
        val flatMapDisposable = Observable.just("A", "B", "C")
            .flatMap { 
                // 每个元素都转换成一个新的Observable
                Observable.just("$it-1", "$it-2")
            }
            .subscribe { Log.d("wangning", "flatMap: $it") }  // 可能乱序输出
        compositeDisposable.add(flatMapDisposable)
        
        // concatMap操作符：类似flatMap，但保证发射顺序
        // 等待前一个Observable完成后才处理下一个，适合需要保持顺序的场景
        val concatMapDisposable = Observable.just(1, 2, 3)
            .concatMap { 
                Observable.just("concat-$it-a", "concat-$it-b")
                    .delay(100, TimeUnit.MILLISECONDS)  // 即使有延迟也保持顺序
            }
            .subscribe { Log.d("wangning", "concatMap: $it") }  // 严格按顺序输出
        compositeDisposable.add(concatMapDisposable)
        
        // switchMap操作符：只发射最新的Observable的数据
        // 如果新的Observable开始发射，会取消订阅之前的Observable
        // 适用于搜索联想等需要取消旧请求的场景
        val switchMapDisposable = Observable.just(100, 200, 300)
            .switchMap { 
                Observable.just("switch-$it")
                    .delay(50, TimeUnit.MILLISECONDS)
            }
            .subscribe { Log.d("wangning", "switchMap: $it") }  // 可能只输出最后一个
        compositeDisposable.add(switchMapDisposable)
        
        // buffer操作符：将数据按指定数量打包成List
        // 参数：每个List包含的元素数量
        val bufferDisposable = Observable.range(1, 10)
            .buffer(3)  // 每3个打包成一个List
            .subscribe { Log.d("wangning", "buffer: $it") }  // [1,2,3], [4,5,6], [7,8,9], [10]
        compositeDisposable.add(bufferDisposable)
        
        // groupBy操作符：按照指定规则对数据分组
        // 返回GroupedObservable，每个组都是一个Observable
        val groupByDisposable = Observable.range(1, 10)
            .groupBy { it % 2 == 0 }  // 按奇偶性分组
            .subscribe { group ->
                // group.key 是分组的key（true或false）
                group.toList().subscribe { list ->
                    Log.d("wangning", "groupBy key=${group.key}: $list")  // false:[1,3,5,7,9], true:[2,4,6,8,10]
                }
            }
        compositeDisposable.add(groupByDisposable)
        
        // scan操作符：累加器，将每次计算的中间结果都发射出来
        // acc：累加器（上次的结果），value：当前值
        val scanDisposable = Observable.just(1, 2, 3, 4, 5)
            .scan { acc, value -> acc + value }  // 累加求和
            .subscribe { Log.d("wangning", "scan: $it") }  // 1, 3, 6, 10, 15
        compositeDisposable.add(scanDisposable)
        
        // window操作符：类似buffer，但返回的是Observable而不是List
        // 每个窗口都是一个独立的Observable
        val windowDisposable = Observable.range(1, 10)
            .window(3)  // 每3个元素创建一个窗口
            .subscribe { window ->
                window.toList().subscribe { list ->
                    Log.d("wangning", "window: $list")  // [1,2,3], [4,5,6], [7,8,9], [10]
                }
            }
        compositeDisposable.add(windowDisposable)
        
        // cast操作符：强制类型转换
        // 如果转换失败会抛出ClassCastException
        val castDisposable = Observable.just(1, 2, 3)
            .cast(Number::class.java)  // 将Integer转换为Number类型
            .subscribe { Log.d("wangning", "cast: $it") }
        compositeDisposable.add(castDisposable)
    }
    
    /**
     * 测试过滤操作符
     * 过滤操作符用于有选择地发射Observable的数据
     */
    private fun testFilteringOperators() {
        Log.d("wangning", "---------- 过滤操作符 ----------")
        
        // filter操作符：只发射满足条件的数据
        // 返回true的数据会被发射，返回false的会被过滤
        val filterDisposable = Observable.range(1, 10)
            .filter { it % 2 == 0 }  // 只保留偶数
            .subscribe { Log.d("wangning", "filter: $it") }  // 2, 4, 6, 8, 10
        compositeDisposable.add(filterDisposable)
        
        // take操作符：只发射前N个数据
        // 参数：要取的数据数量
        val takeDisposable = Observable.just("take-1", "take-2", "take-3", "take-4")
            .take(2)  // 只取前2个
            .subscribe { Log.d("wangning", "take: $it") }  // take-1, take-2
        compositeDisposable.add(takeDisposable)
        
        // takeLast操作符：只发射后N个数据
        // 需要缓存所有数据，直到数据流结束才发射
        val takeLastDisposable = Observable.just("last-1", "last-2", "last-3", "last-4")
            .takeLast(2)  // 只取后2个
            .subscribe { Log.d("wangning", "takeLast: $it") }  // last-3, last-4
        compositeDisposable.add(takeLastDisposable)
        
        // skip操作符：跳过前N个数据
        // 与take相反，忽略开头的数据
        val skipDisposable = Observable.just("skip-1", "skip-2", "skip-3", "skip-4")
            .skip(2)  // 跳过前2个
            .subscribe { Log.d("wangning", "skip: $it") }  // skip-3, skip-4
        compositeDisposable.add(skipDisposable)
        
        // skipLast操作符
        val skipLastDisposable = Observable.just("skipL-1", "skipL-2", "skipL-3", "skipL-4")
            .skipLast(2)
            .subscribe { Log.d("wangning", "skipLast: $it") }
        compositeDisposable.add(skipLastDisposable)
        
        // distinct操作符：去重，只发射不重复的数据
        // 内部使用HashSet记录已发射的数据
        val distinctDisposable = Observable.just(1, 2, 2, 3, 3, 3, 4)
            .distinct()  // 全局去重
            .subscribe { Log.d("wangning", "distinct: $it") }  // 1, 2, 3, 4
        compositeDisposable.add(distinctDisposable)
        
        // distinctUntilChanged操作符：去除连续重复的数据
        // 只比较相邻的两个数据，不是全局去重
        val distinctUntilChangedDisposable = Observable.just(1, 1, 2, 2, 2, 3, 3, 1, 1)
            .distinctUntilChanged()  // 去除连续重复
            .subscribe { Log.d("wangning", "distinctUntilChanged: $it") }  // 1, 2, 3, 1
        compositeDisposable.add(distinctUntilChangedDisposable)
        
        // elementAt操作符
        val elementAtDisposable = Observable.just("elem-0", "elem-1", "elem-2", "elem-3")
            .elementAt(2)
            .subscribe { Log.d("wangning", "elementAt: $it") }
        compositeDisposable.add(elementAtDisposable)
        
        // first操作符
        val firstDisposable = Observable.just("first-A", "first-B", "first-C")
            .first("default")
            .subscribe(
                { result -> Log.d("wangning", "first: $result") },
                { error -> Log.e("wangning", "first error: ${error.message}") }
            )
        compositeDisposable.add(firstDisposable)
        
        // last操作符
        val lastDisposable = Observable.just("last-X", "last-Y", "last-Z")
            .last("default")
            .subscribe(
                { result -> Log.d("wangning", "last: $result") },
                { error -> Log.e("wangning", "last error: ${error.message}") }
            )
        compositeDisposable.add(lastDisposable)
        
        // debounce操作符：防抖，只发射指定时间段内没有新数据的数据
        // 常用于搜索框输入防抖，避免频繁请求
        val debounceDisposable = Observable.create<String> { emitter ->
            emitter.onNext("debounce-1")  // 被丢弃（100ms后有新数据）
            Thread.sleep(100)
            emitter.onNext("debounce-2")  // 被丢弃（300ms > 200ms，但后面还有数据）
            Thread.sleep(300)  
            emitter.onNext("debounce-3")  // 会发射（后面没有新数据了）
            emitter.onComplete()
        }
            .subscribeOn(Schedulers.io())
            .debounce(200, TimeUnit.MILLISECONDS)  // 200ms内没有新数据才发射
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { Log.d("wangning", "debounce: $it") }  // 只输出debounce-3
        compositeDisposable.add(debounceDisposable)
        
        // throttleFirst操作符：节流，在指定时间窗口内只发射第一个数据
        // 常用于防止按钮重复点击
        val throttleFirstDisposable = Observable.interval(100, TimeUnit.MILLISECONDS)  // 每100ms发射一个
            .take(10)  // 取10个
            .throttleFirst(250, TimeUnit.MILLISECONDS)  // 250ms内只取第一个
            .subscribe { Log.d("wangning", "throttleFirst: $it") }  // 0, 3, 6, 9
        compositeDisposable.add(throttleFirstDisposable)
    }
    
    /**
     * 测试组合操作符
     * 组合操作符用于将多个Observable组合成一个
     */
    private fun testCombiningOperators() {
        Log.d("wangning", "---------- 组合操作符 ----------")
        
        // merge操作符：合并多个Observable，按时间顺序发射
        // 不保证Observable之间的顺序，谁先发射就先输出谁
        val observable1 = Observable.just("merge-A1", "merge-A2")
        val observable2 = Observable.just("merge-B1", "merge-B2")
        val mergeDisposable = Observable.merge(observable1, observable2)
            .subscribe { Log.d("wangning", "merge: $it") }  // 可能交叉输出
        compositeDisposable.add(mergeDisposable)
        
        // concat操作符：连接多个Observable，严格按顺序发射
        // 第一个Observable完成后才开始第二个
        val concatDisposable = Observable.concat(
            Observable.just("concat-1", "concat-2"),
            Observable.just("concat-3", "concat-4")
        )
            .subscribe { Log.d("wangning", "concat: $it") }  // 严格按顺序1,2,3,4
        compositeDisposable.add(concatDisposable)
        
        // zip操作符：将多个Observable的数据按顺序配对组合
        // 按照最短的Observable的长度组合，多余的丢弃
        val zipDisposable = Observable.zip(
            Observable.just(1, 2, 3),
            Observable.just("A", "B", "C")
        ) { num, letter -> "$num-$letter" }  // 组合函数
            .subscribe { Log.d("wangning", "zip: $it") }  // 1-A, 2-B, 3-C
        compositeDisposable.add(zipDisposable)
        
        // combineLatest操作符：组合最新的数据
        // 任一Observable发射新数据时，都与其他Observable的最新数据组合
        val combineLatestDisposable = Observable.combineLatest(
            Observable.interval(100, TimeUnit.MILLISECONDS).take(3),  // 0,1,2
            Observable.interval(150, TimeUnit.MILLISECONDS).take(3)   // 0,1,2
        ) { a, b -> "combine: $a-$b" }
            .subscribe { Log.d("wangning", "combineLatest: $it") }  // 可能输出0-0, 1-0, 1-1, 2-1, 2-2
        compositeDisposable.add(combineLatestDisposable)
        
        // startWith操作符：在数据流开始前插入指定数据
        // 常用于添加默认值或初始状态
        val startWithDisposable = Observable.just("normal-1", "normal-2")
            .startWith("startWith-0")  // 在开头插入
            .subscribe { Log.d("wangning", "startWith: $it") }  // startWith-0, normal-1, normal-2
        compositeDisposable.add(startWithDisposable)
        
        // amb操作符：竞争操作符，只发射最先发射数据的Observable的所有数据
        // amb = ambiguous(模糊的)，选择最快的数据源
        val amb1 = Observable.timer(100, TimeUnit.MILLISECONDS).map { "amb-A" }  // 100ms后发射
        val amb2 = Observable.timer(200, TimeUnit.MILLISECONDS).map { "amb-B" }  // 200ms后发射
        val ambDisposable = Observable.ambArray(amb1, amb2)
            .subscribe { Log.d("wangning", "amb: $it") }  // 只输出mb-A
        compositeDisposable.add(ambDisposable)
        
        // join操作符：类似SQL的join，在时间窗口内组合两个Observable的数据
        // 参数1：右Observable
        // 参数2：左窗口选择器（定义左边数据的有效时间）
        // 参数3：右窗口选择器（定义右边数据的有效时间）
        // 参数4：组合函数
        val left = Observable.interval(100, TimeUnit.MILLISECONDS).take(3)
        val right = Observable.interval(150, TimeUnit.MILLISECONDS).take(3)
        val joinDisposable = left.join(
            right,
            { Observable.timer(200, TimeUnit.MILLISECONDS) },  // 左数据有效200ms
            { Observable.timer(200, TimeUnit.MILLISECONDS) },  // 右数据有效200ms
            { l, r -> "join: $l-$r" }
        )
            .subscribe { Log.d("wangning", "join: $it") }  // 在时间窗口重叠时组合
        compositeDisposable.add(joinDisposable)
    }
    
    /**
     * 测试错误处理操作符
     * 错误处理操作符用于处理Observable中的异常
     */
    private fun testErrorHandlingOperators() {
        Log.d("wangning", "---------- 错误处理操作符 ----------")
        
        // onErrorReturn操作符：遇到错误时返回默认值并正常结束
        // 错误被捕获并转换为正常数据
        val onErrorReturnDisposable = Observable.create<String> { emitter ->
            emitter.onNext("error-1")  // 会发射
            emitter.onError(Exception("Test error"))  // 抛出错误
            emitter.onNext("error-2")  // 不会执行（错误后数据流终止）
        }
            .onErrorReturn { "Error occurred: ${it.message}" }  // 将错误转换为数据
            .subscribe { Log.d("wangning", "onErrorReturn: $it") }  // 输出error-1和错误信息
        compositeDisposable.add(onErrorReturnDisposable)
        
        // onErrorResumeNext操作符：遇到错误时切换到备用Observable
        // 用于提供降级方案或备用数据源
        val onErrorResumeNextDisposable = Observable.create<String> { emitter ->
            emitter.onNext("resume-1")  // 会发射
            emitter.onError(Exception("Resume error"))  // 抛出错误
        }
            .onErrorResumeNext(Observable.just("resume-fallback-1", "resume-fallback-2"))  // 备用数据源
            .subscribe { Log.d("wangning", "onErrorResumeNext: $it") }  // resume-1, fallback-1, fallback-2
        compositeDisposable.add(onErrorResumeNextDisposable)
        
        // retry操作符：遇到错误时重试指定次数
        // 参数：最大重试次数（不包括第一次）
        var retryCount = 0
        val retryDisposable = Observable.create<String> { emitter ->
            retryCount++
            Log.d("wangning", "retry attempt: $retryCount")
            if (retryCount < 3) {
                emitter.onError(Exception("Retry error"))  // 前2次抛出错误
            } else {
                emitter.onNext("retry-success")  // 第3次成功
                emitter.onComplete()
            }
        }
            .retry(3)  // 最多重试3次
            .subscribe(
                { Log.d("wangning", "retry result: $it") },  // 成功时输出
                { Log.e("wangning", "retry failed: ${it.message}") }  // 重试失败后输出
            )
        compositeDisposable.add(retryDisposable)
        
        // retryWhen操作符：根据自定义逻辑决定是否重试
        // 可以实现递增延迟重试、指数退避等策略
        var retryWhenCount = 0
        val retryWhenDisposable = Observable.create<String> { emitter ->
            retryWhenCount++
            Log.d("wangning", "retryWhen attempt: $retryWhenCount")
            emitter.onError(Exception("RetryWhen error"))
        }
            .retryWhen { errors ->  // errors是错误流
                errors.zipWith(Observable.range(1, 3)) { error, retryCount ->
                    Log.d("wangning", "retryWhen: attempt $retryCount")
                    retryCount
                }.flatMap {
                    // 递增延迟：第1次100ms，第2次200ms，第3次300ms
                    Observable.timer(it.toLong() * 100, TimeUnit.MILLISECONDS)
                }
            }
            .subscribe(
                { Log.d("wangning", "retryWhen result: $it") },
                { Log.e("wangning", "retryWhen failed: ${it.message}") }
            )
        compositeDisposable.add(retryWhenDisposable)
    }
    
    /**
     * 测试工具操作符
     * 工具操作符提供了一些实用的辅助功能
     */
    private fun testUtilityOperators() {
        Log.d("wangning", "---------- 工具操作符 ----------")
        
        // delay操作符：延迟发射数据
        // 数据本身不变，只是延迟发射时间
        val delayDisposable = Observable.just("delay-item")
            .delay(500, TimeUnit.MILLISECONDS)  // 延迟500ms后发射
            .subscribe { Log.d("wangning", "delay: $it (after 500ms)") }
        compositeDisposable.add(delayDisposable)
        
        // doOnNext操作符：在每个数据发射时执行副作用
        // 不改变数据，常用于调试、日志、保存状态等
        val doOnNextDisposable = Observable.just("doOn-1", "doOn-2", "doOn-3")
            .doOnNext { Log.d("wangning", "doOnNext side-effect: $it") }  // 副作用：打印日志
            .map { it.uppercase() }  // 真正的数据转换
            .subscribe { Log.d("wangning", "doOnNext result: $it") }
        compositeDisposable.add(doOnNextDisposable)
        
        // doOnError操作符
        val doOnErrorDisposable = Observable.create<String> { emitter ->
            emitter.onNext("doOnError-item")
            emitter.onError(Exception("DoOnError test"))
        }
            .doOnError { Log.e("wangning", "doOnError side-effect: ${it.message}") }
            .onErrorReturn { "error-handled" }
            .subscribe { Log.d("wangning", "doOnError result: $it") }
        compositeDisposable.add(doOnErrorDisposable)
        
        // doOnComplete操作符
        val doOnCompleteDisposable = Observable.just("complete-1", "complete-2")
            .doOnComplete { Log.d("wangning", "doOnComplete side-effect") }
            .subscribe { Log.d("wangning", "doOnComplete item: $it") }
        compositeDisposable.add(doOnCompleteDisposable)
        
        // doOnSubscribe操作符：在订阅开始时执行副作用
        // 参数是Disposable，可以用来管理订阅
        val doOnSubscribeDisposable = Observable.just("subscribe-item")
            .doOnSubscribe { Log.d("wangning", "doOnSubscribe: Starting subscription") }  // 订阅开始时执行
            .subscribe { Log.d("wangning", "doOnSubscribe result: $it") }
        compositeDisposable.add(doOnSubscribeDisposable)
        
        // doOnDispose操作符
        val doOnDisposeDisposable = Observable.interval(200, TimeUnit.MILLISECONDS)
            .doOnDispose { Log.d("wangning", "doOnDispose: Disposed") }
            .take(2)
            .subscribe { Log.d("wangning", "doOnDispose item: $it") }
        compositeDisposable.add(doOnDisposeDisposable)
        
        // doFinally操作符
        val doFinallyDisposable = Observable.just("finally-1", "finally-2")
            .doFinally { Log.d("wangning", "doFinally: Cleanup") }
            .subscribe { Log.d("wangning", "doFinally item: $it") }
        compositeDisposable.add(doFinallyDisposable)
        
        // timeout操作符：设置超时时间，超时后发射错误
        // 常用于网络请求超时处理
        val timeoutDisposable = Observable.create<String> { emitter ->
            Thread.sleep(2000)  // 模拟耗时2秒的操作
            emitter.onNext("timeout-item")
        }
            .subscribeOn(Schedulers.io())
            .timeout(1, TimeUnit.SECONDS)  // 设置1秒超时
            .subscribe(
                { Log.d("wangning", "timeout: $it") },  // 不会执行
                { Log.e("wangning", "timeout error: ${it.message}") }  // 超时错误
            )
        compositeDisposable.add(timeoutDisposable)
        
        // materialize/dematerialize操作符
        val materializeDisposable = Observable.just("mat-1", "mat-2")
            .materialize()
            .doOnNext { Log.d("wangning", "materialize notification: $it") }
            .dematerialize<String>()
            .subscribe { Log.d("wangning", "dematerialize: $it") }
        compositeDisposable.add(materializeDisposable)
        
        // subscribeOn/observeOn操作符：线程调度
        // subscribeOn：指定上游（数据发射）的线程，只有第一个生效
        // observeOn：指定下游（数据接收）的线程，可以多次切换
        val threadingDisposable = Observable.just("threading-item")
            .subscribeOn(Schedulers.io())  // 数据创建在IO线程
            .doOnNext { Log.d("wangning", "subscribeOn thread: ${Thread.currentThread().name}") }  // IO线程
            .observeOn(AndroidSchedulers.mainThread())  // 切换到主线程
            .subscribe { Log.d("wangning", "observeOn thread: ${Thread.currentThread().name}, item: $it") }  // 主线程
        compositeDisposable.add(threadingDisposable)
    }
    
    /**
     * 测试Subject
     */
    private fun testSubjects() {
        Log.d("wangning", "---------- Subject测试 ----------")
        
        // PublishSubject
        val publishSubject = PublishSubject.create<String>()
        val publishDisposable1 = publishSubject.subscribe { 
            Log.d("wangning", "PublishSubject-1: $it") 
        }
        publishSubject.onNext("publish-A")
        
        val publishDisposable2 = publishSubject.subscribe { 
            Log.d("wangning", "PublishSubject-2: $it") 
        }
        publishSubject.onNext("publish-B")
        publishSubject.onNext("publish-C")
        compositeDisposable.addAll(publishDisposable1, publishDisposable2)
        
        // BehaviorSubject
        val behaviorSubject = BehaviorSubject.createDefault("behavior-default")
        behaviorSubject.onNext("behavior-A")
        
        val behaviorDisposable = behaviorSubject.subscribe { 
            Log.d("wangning", "BehaviorSubject: $it") 
        }
        behaviorSubject.onNext("behavior-B")
        behaviorSubject.onNext("behavior-C")
        compositeDisposable.add(behaviorDisposable)
        
        // ReplaySubject
        val replaySubject = ReplaySubject.create<String>()
        replaySubject.onNext("replay-A")
        replaySubject.onNext("replay-B")
        
        val replayDisposable = replaySubject.subscribe { 
            Log.d("wangning", "ReplaySubject: $it") 
        }
        replaySubject.onNext("replay-C")
        compositeDisposable.add(replayDisposable)
        
        // AsyncSubject
        val asyncSubject = AsyncSubject.create<String>()
        val asyncDisposable = asyncSubject.subscribe { 
            Log.d("wangning", "AsyncSubject: $it") 
        }
        asyncSubject.onNext("async-A")
        asyncSubject.onNext("async-B")
        asyncSubject.onNext("async-C")
        asyncSubject.onComplete() // 只发射最后一个值
        compositeDisposable.add(asyncDisposable)
        
        // Single
        val singleDisposable = Single.just("single-value")
            .subscribe(
                { result -> Log.d("wangning", "Single: $result") },
                { error -> Log.e("wangning", "Single error: ${error.message}") }
            )
        compositeDisposable.add(singleDisposable)
        
        // Maybe
        val maybeDisposable = Maybe.just("maybe-value")
            .subscribe(
                { result -> Log.d("wangning", "Maybe: $result") },
                { error -> Log.e("wangning", "Maybe error: ${error.message}") }
            )
        compositeDisposable.add(maybeDisposable)
        
        // Completable
        val completableDisposable = Completable.fromAction {
            Log.d("wangning", "Completable: Performing action")
        }
            .subscribe(
                { Log.d("wangning", "Completable: Completed") },
                { error -> Log.e("wangning", "Completable error: ${error.message}") }
            )
        compositeDisposable.add(completableDisposable)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}