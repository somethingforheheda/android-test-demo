package com.example.test.tests.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.test.R
import com.example.test.TestRegistry

class ShadowTextTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shadow_text_test)

        val tv = findViewById<TextView>(R.id.camera_magic_emoji_tv)
        tv.setShadowLayer(6f, 0f, 0f, ContextCompat.getColor(this, R.color.black))
        tv.text = "这是一段很长的文"
    }

}
