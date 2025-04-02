package com.example.ebook_reader.ui.BookShelf

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    private val bottomSheetDialog = BookShelf_BotSheetDialog()

    //获取Activity共享的ViewModel
    private val viewModel: BookShelfViewModel by activityViewModels()

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
        when (viewModel.isInFolder.value){
            true ->{
                topICD.DefaultTopBar.visibility = View.GONE
                topICD.InFolderTopBar.visibility = View.VISIBLE
                topICD.EditModuleTopBar.visibility = View.GONE
            }
            false ->{
                when(viewModel.isEditModel.value){
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
        when(viewModel.isInFolder.value or viewModel.isEditModel.value){
            true->{
                bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
                when(viewModel.isInFolder.value){
                    true->bottomICD.BookShelfRenameFolderBTN.visibility = View.GONE
                    false->bottomICD.BookShelfRenameFolderBTN.visibility = View.VISIBLE
                }
            }
            false->bottomICD.BookShelfBottomBarTotal.visibility = View.GONE
        }
    }
    //切换编辑模式 并显示对应的TopBar和BottomBar
    private fun switchEditModule(){
        when(viewModel.isEditModel.value){
            true -> viewModel.setEditModel(false)
            false ->viewModel.setEditModel(true)
        }
        setTopBarVisibility()
        setBottomBarVisibility()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //设置书本的ListAdapter
        val bookAdapter = BookAdapter(viewModel)
        //设置layoutManager和adapter
        binding.BookRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.BookRecyclerView.adapter = bookAdapter
        lifecycleScope.launch {
            //动态更新RecyclerView数据逻辑
            launch {
                viewModel.items.collectLatest { newList ->
                    bookAdapter.submitList(newList)
                }
            }
            //只有在选中一个文件夹时才可以重命名文件夹
            launch {
                viewModel.isSingleSelectedFolder.collectLatest {
                    bottomICD.BookShelfRenameFolderBTN.isEnabled = it
                }
            }
            //动态更新是否可以移动书本的状态
            launch {
                viewModel.canMoveBooks.collectLatest {
                    bottomICD.BookShelfMoveBTN.isEnabled = it
                }
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
            when(viewModel.isInFolder.value) {
                true -> {
                    viewModel.setInFolder(false)
                    viewModel.showActionBar()
                }
                false -> {
                    viewModel.setInFolder(true)
                    viewModel.hideActionBar()
                }
            }
            setTopBarVisibility()
            setBottomBarVisibility()
        }
        //设置在文件夹内 返回默认页面按钮
        topICD.BookshelfBackToDefaultBTN.setOnClickListener {
            viewModel.setInFolder(false)
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
        topICD.BookShelfRenameFolderInFolderBTN.setOnClickListener {

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
            var yourChoice: Boolean? = null
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("删除")
                .setMessage("确定删除选中的书本和文件夹吗？(此过程不可逆!)")
                .setPositiveButton("确定") { _, _ ->

                    deleteSelectedItems()
                }
                .setNegativeButton("取消") { _, _ ->
                    yourChoice=false
                }
                .create()
            dialog.show()
            Log.d("DeleteDialog","$yourChoice")

        }
        //TODO:设置在编辑模式下 在主页编辑模式 重命名文件夹按钮 在文件夹内取消其使用
        bottomICD.BookShelfRenameFolderBTN.setOnClickListener {
            if(viewModel.isInFolder.value)return@setOnClickListener
            if(viewModel.isSingleSelectedFolder.value){
                //viewModel.renameFolder()
                //TODO:重命名文件夹 需要设置输入框
            }
            else {
                Log.d("isSingleSelectedFolder","false")
            }
        }

    }

    /**
     * 在文件夹内只删除书本, 在主页删除书本和文件夹
     */
    private fun deleteSelectedItems(){
        when(viewModel.isInFolder.value){
            true -> viewModel.deleteSelectedBooks()
            false -> viewModel.deleteSelectedAll()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.setEditModel(false)
        viewModel.setInFolder(false)
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

