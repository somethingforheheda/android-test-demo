package com.example.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.tests.ui.FragmentContainerActivity

/**
 * Fragment 测试列表 Activity
 * 
 * 功能说明：
 * 1. 展示所有已注册的 Fragment 测试项
 * 2. 点击列表项启动对应的 Fragment
 * 3. 使用 RecyclerView 展示，提供更好的用户体验
 * 
 * 设计说明：
 * - 集中管理所有 Fragment 测试，避免主界面过于复杂
 * - 通过 FragmentTestRegistry 获取所有注册的 Fragment
 * - 点击后通过 FragmentContainerActivity 显示对应 Fragment
 */
class FragmentListActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FragmentListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_list)
        
        // 设置标题
        supportActionBar?.title = "Fragment 测试集合"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerViewFragments)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        // 获取所有 Fragment 测试项
        val fragmentList = FragmentTestRegistry.getFragments().map { (name, clazz) ->
            FragmentTestItem(name, clazz)
        }
        
        // 设置适配器
        adapter = FragmentListAdapter(fragmentList) { item ->
            // 点击事件：启动对应的 Fragment
            val intent = Intent(this, FragmentContainerActivity::class.java)
            intent.putExtra(FragmentContainerActivity.EXTRA_FRAGMENT_CLASS, item.clazz.name)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

/**
 * Fragment 测试项数据类
 */
data class FragmentTestItem(
    val name: String,
    val clazz: Class<out androidx.fragment.app.Fragment>
)

/**
 * Fragment 列表适配器
 */
class FragmentListAdapter(
    private val items: List<FragmentTestItem>,
    private val onItemClick: (FragmentTestItem) -> Unit
) : RecyclerView.Adapter<FragmentListAdapter.ViewHolder>() {
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardFragmentItem)
        private val tvName: TextView = itemView.findViewById(R.id.tvFragmentName)
        private val tvDescription: TextView = itemView.findViewById(R.id.tvFragmentDescription)
        
        fun bind(item: FragmentTestItem) {
            tvName.text = item.name
            
            // 根据 Fragment 名称添加描述
            tvDescription.text = when {
                item.name.contains("FrameTaskSplitter") -> "演示帧任务分割器的使用，批量任务分帧执行"
                item.name.contains("IdleTask") -> "演示空闲任务执行器的使用"
                item.name.contains("ViewBinding") -> "展示 ViewBinding 的使用方法"
                item.name.contains("Demo Two") -> "Fragment 基础功能演示 2"
                item.name.contains("Demo") -> "Fragment 基础功能演示"
                else -> "Fragment 功能测试"
            }
            
            // 设置点击事件
            cardView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_fragment_test, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
}