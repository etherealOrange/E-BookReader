package com.example.ebook_reader.ui.BookShelf

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ebook_reader.databinding.FragmentBookShelfBinding
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookShelf : Fragment() {
    //ViewBinding
    private var _binding: FragmentBookShelfBinding? = null
    private val binding get() = _binding!!
    //TopBar和BottomBar include布局 和底部抽屉的布局
    private val topICD get() = binding.BookShelfTopBarICD
    private val bottomICD get() = binding.BookshelfBottomBarICD
    private val bottomSheetDialog = BookShelf_BotSheetDialog(this)

    //获取Activity共享的ViewModel
    private val viewModel: BookShelfViewModel by activityViewModels()


    //编辑状态 和 在文件夹内 Boolean 状态
    //TODO:把所有的状态都放到ViewModel中
    private var _isEditModule = MutableStateFlow(false)
    val isEditModel : StateFlow<Boolean> get() = _isEditModule
    private var  _isInFolder = MutableStateFlow(false)
    val isInFolder : StateFlow<Boolean> get() = _isInFolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookShelfBinding.inflate(inflater, container, false)
        return binding.root
    }
    //设置Topbar的可见性模式 根据isInfolder 和 isEditModel
    private fun setTopBarVisibility(){
        topICD.BookShelfTopBarTotal.visibility = View.VISIBLE
        when (isInFolder.value){
            true ->{
                topICD.DefaultTopBar.visibility = View.GONE
                topICD.InFolderTopBar.visibility = View.VISIBLE
                topICD.EditModuleTopBar.visibility = View.GONE
            }
            false ->{
                when(isEditModel.value){
                    true->{
                        topICD.DefaultTopBar.visibility = View.GONE
                        topICD.InFolderTopBar.visibility = View.GONE
                        topICD.EditModuleTopBar.visibility = View.VISIBLE
                    }
                    false->{
                        topICD.DefaultTopBar.visibility = View.VISIBLE
                        topICD.InFolderTopBar.visibility = View.GONE
                        topICD.EditModuleTopBar.visibility = View.GONE
                    }
                }
            }
        }
    }
    //设置BotBar的可见性模式 根据isInfolder 和 isEditModel
    private fun setBottomBarVisibility(){
        when(isInFolder.value or isEditModel.value){
            true->{
                bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
                when(isInFolder.value){
                    true->bottomICD.BookShelfRenameFolderBTN.visibility = View.GONE
                    false->bottomICD.BookShelfRenameFolderBTN.visibility = View.VISIBLE
                }
            }
            false->bottomICD.BookShelfBottomBarTotal.visibility = View.GONE
        }
    }
    //切换编辑模式 并显示对应的TopBar和BottomBar
    private fun switchEditModule(){
        when(isEditModel.value){
            true -> _isEditModule.value=false
            false -> _isEditModule.value=true
        }
        setTopBarVisibility()
        setBottomBarVisibility()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置书本的ListAdapter
        val bookAdapter = BookAdapter(this)
        //设置layoutManager和adapter
        binding.BookRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.BookRecyclerView.adapter = bookAdapter
        //动态更新RecyclerView数据逻辑
        lifecycleScope.launch {
            viewModel.items.collectLatest { newList ->
                bookAdapter.submitList(newList)
            }
        }
        setTopBarVisibility()
        setBottomBarVisibility()
        initAllTopICD(bookAdapter)
        initAllBottomICD()
    }

    /**
     * 初始化所有TopBar的点击事件
     */
    private fun initAllTopICD(bookAdapter: BookAdapter){
        //设置在默认页面 Edit模式 进入按钮
        topICD.BookShelfEditBTN.setOnClickListener {
            switchEditModule()
        }
        //设置在文件夹内 Edit模式 进入按钮
        topICD.BookShelfEditInFolderBTN.setOnClickListener {
            switchEditModule()
        }
        //设置两种页面下 Edit模式 退出按钮
        topICD.BookshelfAllDownBTN.setOnClickListener {
            switchEditModule()
        }
        //暂时使用导入按钮代替文件夹按钮 ！！！
        //TODO:记得修改按钮为真正的导入按钮
        topICD.BookshelfBookImportBTN.setOnClickListener {
            when(isInFolder.value) {
                true -> {
                    _isInFolder.value = false
                    viewModel.showActionBar()
                }
                false -> {
                    _isInFolder.value = true
                    viewModel.hideActionBar()
                }
            }
            setTopBarVisibility()
            setBottomBarVisibility()
        }
        //设置在文件夹内 返回默认页面按钮
        topICD.BookshelfBackToDefaultBTN.setOnClickListener {
            _isInFolder.value = false
            viewModel.showActionBar()
            setTopBarVisibility()
            setBottomBarVisibility()
        }
        //设置TopBar的全选按钮
        topICD.BookShelfAllSelectBTN.setOnClickListener {
            if (topICD.BookShelfAllSelectBTN.isVisible)
                bookAdapter.selectedAllSelected()
        }
        //设置TopBar的取消全选按钮
        topICD.BookshelfCancelSelectBTN.setOnClickListener {
            if(topICD.BookshelfCancelSelectBTN.isVisible)
                bookAdapter.cancelAllSelected()
        }
    }
    /**
     * 初始化所有BottomBar的点击事件
     */
    private fun initAllBottomICD(){
        //设置在编辑模式下 两种页面 移动按钮 呼叫底部抽屉
        //通过parentFragmentManager管理父 Fragment 或 Activity 中的 Fragment 事务
        //启动另一个 Fragment（如 DialogFragment）
        bottomICD.BookShelfMoveBTN.setOnClickListener {
            bottomSheetDialog.show(parentFragmentManager,bottomSheetDialog.tag )

        }
        //TODO:设置在编辑模式下 两种页面 删除按钮  添加删除确认弹窗
        bottomICD.BookShelfDeleteBTN.setOnClickListener {
            when(isInFolder.value){
                true -> {}
                false -> {}
            }
        }
        //TODO:设置在编辑模式下 在主页编辑模式 重命名文件夹按钮 在文件夹内取消其使用
        bottomICD.BookShelfRenameFolderBTN.setOnClickListener {
            if(isInFolder.value)return@setOnClickListener

        }

    }

    override fun onStart() {
        super.onStart()

        _isEditModule.value =false
        _isInFolder.value =false
        Log.d("BookShelf onStart","success")
    }
    override fun onStop() {
        super.onStop()
        Log.d("BookShelf onStop","success")
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        Log.d("BookShelf onDestroy","success")
    }
}