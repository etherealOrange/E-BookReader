package com.example.ebook_reader

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.ActivityMainBinding

class Main_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nav_view = binding.navMain
        val navControl = findNavController(R.id.nav_host_main_content_fragment)
        nav_view.setupWithNavController(navControl)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) {
            v, insets -> val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom)
            insets
        }

//        val recyclerView: RecyclerView = findViewById(R.id.BookRecyclerView)
//        //设置RecyclerView的布局管理器为网格布局
//        recyclerView.layoutManager = GridLayoutManager(this, 3)
//        val items = listOf("Book 1", "Book 2", "Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6") // Example data
//        //设置RecyclerView的适配器
//        recyclerView.adapter = CardAdapter(items)
    }
}