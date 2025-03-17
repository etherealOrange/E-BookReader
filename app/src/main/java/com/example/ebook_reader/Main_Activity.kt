package com.example.ebook_reader

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnAttach
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.ActivityMainBinding
import com.example.ebook_reader.databinding.NavViewHeadMainBinding
import com.example.ebook_reader.ui.BookShelf.BookShelfViewModel
import kotlin.getValue

class Main_Activity : AppCompatActivity() {
    //设置Activity的布局文件
    private lateinit var binding: ActivityMainBinding
    private  lateinit var appBarConfiguration: AppBarConfiguration
    //设置Viewmodel
    private val viewModel: BookShelfViewModel by viewModels{
        object : ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BookShelfViewModel(application) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //隐藏ActionBar
        //supportActionBar?.hide()
        //设置Activity的布局文件
        binding = ActivityMainBinding.inflate(layoutInflater)
        //设置Activity的布局视图
        setContentView(binding.root)
        //设置顶部ActionBar
        val toolbar = binding.activityMainContentICD.mainToolbar
        setSupportActionBar(toolbar)

        //设置底部导航栏
        val navView = binding.navViewMain
        //获取头部视图
        val navHead = navView.getHeaderView(0)
        //绑定头部视图
        val navHeadBind = NavViewHeadMainBinding.bind(navHead)
        val button = navHeadBind.EditYourNameBTN
        val editText = navHeadBind.EditYourNameET
        val textView = navHeadBind.YourNameTV
        //设置头部编辑 你的姓名 按钮的点击事件
        button.setOnClickListener {
            editText.setText(textView.text)
            textView.visibility = View.INVISIBLE
            editText.visibility = View.VISIBLE
            editText.requestFocus()
        }
        //设置编辑框失焦 保存姓名
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                textView.text = editText.text
                editText.visibility = View.INVISIBLE
                textView.visibility = View.VISIBLE
            }
        }
        val navControl = findNavController(R.id.nav_host_main_content_fragment)
        appBarConfiguration = AppBarConfiguration(
            navControl.graph,binding.drawerLayoutMain
        )
        //设置ActionBar和NavController的关联
        setupActionBarWithNavController(navControl, appBarConfiguration)

        //启动底部导航栏
        navView.setupWithNavController(navControl)



        //设置状态栏和导航栏间隙
//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) {
//            v, insets -> val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(
//                systemBars.left,
//                systemBars.top,
//                systemBars.right,
//                systemBars.bottom)
//            insets
//        }

//        val recyclerView: RecyclerView = findViewById(R.id.BookRecyclerView)
//        //设置RecyclerView的布局管理器为网格布局
//        recyclerView.layoutManager = GridLayoutManager(this, 3)
//        val items = listOf("Book 1", "Book 2", "Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6","Book 3", "Book 4", "Book 5", "Book 6") // Example data
//        //设置RecyclerView的适配器
//        recyclerView.adapter = CardAdapter(items)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_main_content_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}