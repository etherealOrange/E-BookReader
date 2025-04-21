package com.example.ebook_reader

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.ebook_reader.databinding.ActivityMainBinding
import com.example.ebook_reader.databinding.NavViewHeadMainBinding
import com.example.ebook_reader.Enum.InsideFolderName
import com.example.ebook_reader.ui.BookShelf.BookShelfDataViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.getValue

@AndroidEntryPoint
class Main_Activity : ExtendAppCompatActivity() {
    //设置Activity的布局文件
    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navHeadBind: NavViewHeadMainBinding
    //设置Viewmodel
    private val viewModel: BookShelfDataViewModel by viewModels()
    private val navController by lazy { (supportFragmentManager.findFragmentById(binding.navContainerFragment.id) as NavHostFragment).navController }

    private val configManager = ConfigManager.getInstance(this)
    private var config = configManager.getOtherConfig()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(config.dayTheme){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        //设置Activity的布局文件
        binding = ActivityMainBinding.inflate(layoutInflater)
        //设置Activity的布局视图
        setContentView(binding.root)


        //设置顶部ActionBar
        val toolbar = binding.mainToolbar
        setSupportActionBar(toolbar)

        //设置左侧导航栏
        val navView = binding.navViewMain

        //获取头部视图
        val navHead = navView.getHeaderView(0)
        //绑定头部视图
        navHeadBind = NavViewHeadMainBinding.bind(navHead)
        navHeadBind.EditYourNameET.setText(config.name)
        //设置编辑框失焦 保存姓名
        navHeadBind.EditYourNameET.onFocusChangeListener = View.OnFocusChangeListener{v,hasFocus->
            if (!hasFocus){
                config.name = navHeadBind.EditYourNameET.text.toString()
            }
        }



        appBarConfiguration = AppBarConfiguration(
            navController.graph,binding.drawerLayoutMain
        )
        //设置ActionBar和NavController的关联
        setupActionBarWithNavController(navController, appBarConfiguration)
        //启动底部导航栏
        navView.setupWithNavController(navController)

        //获取view Model的数据 设置顶部状态栏的 显示与隐藏
        viewModel.isHideActionBar.launchLifeScopeCollectLatest {
            if (it){
                supportActionBar?.hide()
            }else{
                supportActionBar?.show()
            }
        }
        mkdirAll()

    }

    override fun onPostCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onPostCreate(savedInstanceState, persistentState)

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)

    }



    /**
     *  - 创建所有需要的文件夹
     *  - 包括 文件夹目录， txt书籍文件，pdf书籍文件，epub书籍文件
     */
    fun mkdirAll(){
        mkDir(InsideFolderName.FOLDERSCOVERFOLDER.displayName)
        mkDir(InsideFolderName.TXTBOOKSFOLDER.displayName)
        mkDir(InsideFolderName.PDFBOOKSFOLDER.displayName)
        mkDir(InsideFolderName.EPUBBOOKSFOLDER.displayName)
    }
    //创建内部存储的文件夹
    private fun mkDir(string: String){
        val folder = File(filesDir, string)
        if (!folder.exists()){
            folder.mkdirs()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        config = configManager.getOtherConfig()
    }

    override fun onPause() {
        super.onPause()
        configManager.saveOtherConfig(config)
    }
}