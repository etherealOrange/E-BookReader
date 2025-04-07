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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.ActivityMainBinding
import com.example.ebook_reader.databinding.NavViewHeadMainBinding
import com.example.ebook_reader.entities.InsideFolderName
import com.example.ebook_reader.ui.BookShelf.BookShelfViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
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
        //获取view Model的数据 设置顶部状态栏的 显示与隐藏
        lifecycleScope.launch {
            viewModel.isHideActionBar.collectLatest {
                if (it){
                    supportActionBar?.hide()
                }else{
                    supportActionBar?.show()
                }
            }
        }
        mkFolderCoverDir()
        mkBookCoverDir()


    }
    //创建存储文件夹封面的图片的文件夹
    fun mkFolderCoverDir(){
        mkDir(InsideFolderName.FOLDERSCOVERFOLDER.displayName)
    }
    //创建存储书籍封面的图片的文件夹
    fun mkBookCoverDir(){
        mkDir(InsideFolderName.BOOKSCOVERFOLDER.displayName)
    }
    //创建内部存储的文件夹
    private fun mkDir(string: String){
        val folder = File(filesDir, string)
        if (!folder.exists()){
            folder.mkdirs()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_main_content_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}