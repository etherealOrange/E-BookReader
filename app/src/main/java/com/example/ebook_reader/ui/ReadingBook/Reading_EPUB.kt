package com.example.ebook_reader.ui.ReadingBook

import android.os.Bundle
import android.util.Log
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.databinding.ActivityReadingEpubBinding

class Reading_EPUB : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingEpubBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadingEpubBinding.inflate(layoutInflater)
        setContentView(bind.root)

        Log.d("RB","onCreate 成功创建")



    }

    override fun onStart() {
        super.onStart()
        Log.d("RB","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
        Log.d("RB","onStop 停止")
    }

    override fun onResume() {
        super.onResume()
        Log.d("RB","onResume 恢复")
    }

    override fun onPause() {
        super.onPause()
        Log.d("RB","onResume 暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RB","onDestroy 销毁")
    }
}