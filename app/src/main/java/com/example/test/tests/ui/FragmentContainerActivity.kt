package com.example.test.tests.ui

import androidx.fragment.app.Fragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class FragmentContainerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)
        
        val fragmentClassName = intent.getStringExtra(EXTRA_FRAGMENT_CLASS)
        if (fragmentClassName != null) {
            val fragmentClass = Class.forName(fragmentClassName) as Class<out Fragment>
            val fragment = fragmentClass.newInstance()

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    companion object {
        const val EXTRA_FRAGMENT_CLASS = "fragment_class"
    }
}
