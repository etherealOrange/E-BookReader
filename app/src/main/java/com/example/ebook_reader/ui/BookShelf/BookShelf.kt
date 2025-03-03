package com.example.ebook_reader.ui.BookShelf

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ebook_reader.databinding.FragmentBookShelfBinding
import com.example.ebook_reader.ui.BookShelf.BookShelf_BotSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookShelf : Fragment() {
    private var _binding: FragmentBookShelfBinding? = null
    private val binding get() = _binding!!
    private val topICD get() = binding.BookShelfTopBarICD
    private val bottomICD get() = binding.BookshelfBottomBarICD
    private val viewModel: BookShelfViewModel by activityViewModels()
    private val bottomSheetDialog = BookShelf_BotSheetDialog()

    private var isEditModule = false
    private var isInFolder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookShelfBinding.inflate(inflater, container, false)
        return binding.root
    }
    //设置TopBar的可见性 -》 默认TopBar
    private fun setTopBarToDefaultTopBar(){
        topICD.BookShelfTopBarTotal.visibility = View.VISIBLE
        topICD.DefaultTopBar.visibility = View.VISIBLE
        topICD.InFolderTopBar.visibility = View.GONE
        topICD.EditModuleTopBar.visibility = View.GONE
    }
    //设置TopBar的可见性 -》 编辑模式TopBar
    private fun setTopBarToEditModuleTopBar(){
        topICD.BookShelfTopBarTotal.visibility = View.VISIBLE
        topICD.DefaultTopBar.visibility = View.GONE
        topICD.InFolderTopBar.visibility = View.GONE
        topICD.EditModuleTopBar.visibility = View.VISIBLE
    }
    //设置TopBar的可见性 -》 在文件夹内TopBar
    private fun setTopBarToInFolderTopBar(){
        topICD.BookShelfTopBarTotal.visibility = View.VISIBLE
        topICD.DefaultTopBar.visibility = View.GONE
        topICD.InFolderTopBar.visibility = View.VISIBLE
        topICD.EditModuleTopBar.visibility = View.GONE
    }
    //设置BottomBar的可见性 -》 无BottomBar
    private fun setBottomBarToNoneBottomBar() {
        bottomICD.BookShelfBottomBarTotal.visibility = View.GONE
    }
    //设置BottomBar的可见性 -》 编辑模式BottomBar
    private fun setBottomBarToEditModuleBottomBar() {
        bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
        bottomICD.BookShelfRenameFolderBTN.visibility = View.VISIBLE
    }
    //设置TopBar的可见性 -》 在文件夹内TopBar
    private fun setBottomBarToInFolderBottomBar() {
        bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
        bottomICD.BookShelfRenameFolderBTN.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //实例化ListAdapter
        val bookAdapter = BookAdapter()
        //设置layoutManager和adapter
        binding.BookRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.BookRecyclerView.adapter = bookAdapter
        //动态更新数据逻辑
        lifecycleScope.launch {
            viewModel.items.collectLatest { newList ->
                bookAdapter.submitList(newList)
            }
        }



        setTopBarToDefaultTopBar()
        setBottomBarToNoneBottomBar()
        //设置在默认页面 Edit模式 进入按钮
        topICD.BookShelfEditBTN.setOnClickListener {
            if (!isEditModule)
            {
                isEditModule = true
                setTopBarToEditModuleTopBar()
                setBottomBarToEditModuleBottomBar()
            }
        }
        //设置在文件夹内 Edit模式 进入按钮
        topICD.BookShelfEditInFolderBTN.setOnClickListener {
            if (!isEditModule)
            {
                isEditModule = true
                setTopBarToEditModuleTopBar()
                setBottomBarToInFolderBottomBar()
            }
        }
        //设置两种页面下 Edit模式 退出按钮
        topICD.BookshelfAllDownBTN.setOnClickListener {
            if (isEditModule)
            {
                when(isInFolder)
                {
                    true -> {
                        setTopBarToInFolderTopBar()
                    }
                    false -> {
                        setTopBarToDefaultTopBar()
                    }
                }
                isEditModule = false
                setBottomBarToNoneBottomBar()
            }
        }
        //设置在编辑模式下 两种页面 移动按钮 呼叫底部抽屉
        bottomICD.BookShelfMoveBTN.setOnClickListener {
            bottomSheetDialog.show(parentFragmentManager,bottomSheetDialog.tag )

        }




        //上拉 的下抽屉 触发按钮 设置
//        bottomICD.BookShelfMoveBTN.setOnClickListener {
//            when(bottomSheetDialog.isVisible){
//                true ->
//                    bottomSheetDialog.dismiss()
//                false ->
//                    bottomSheetDialog.show(parentFragmentManager,bottomSheetDialog.tag )
//            }
//        }

    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}