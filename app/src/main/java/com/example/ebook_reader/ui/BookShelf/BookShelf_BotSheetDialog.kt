package com.example.ebook_reader.ui.BookShelf

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ebook_reader.databinding.BookshelfBotSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookShelf_BotSheetDialog(): BottomSheetDialogFragment() {
    private var _binding: BookshelfBotSheetDialogBinding? = null
    private val binding get() = _binding!!
    //获取Activity共享的ViewModel
    private val viewModel: BookShelfDataViewModel by activityViewModels()
    private var adapter: ForSelectFolderAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BookshelfBotSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(adapter==null){
            adapter = ForSelectFolderAdapter(viewModel)
        }
        binding.SheetDialogRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.SheetDialogRecyclerView.adapter = adapter
        lifecycleScope.launch {
            //更新文件夹信息
            launch {
                viewModel.foldersAdapterUIItem.collectLatest {
                    adapter?.submitList(it)
                }
            }
            launch {
                viewModel.isInFolder.collectLatest {
                    if (it){
                        binding.SheetDialogMoveOutFromFolder.visibility = View.VISIBLE
                    }else{
                        binding.SheetDialogMoveOutFromFolder.visibility = View.GONE
                    }
                }
            }
            launch {
                viewModel.moveToFolderId.collectLatest {
                    binding.SheetDialogMoveOutFromFolderCheckBox.isChecked = it==null
                }
            }

        }
        binding.SheetDialogMoveOutFromFolderCheckBox.isClickable=false
        binding.SheetDialogMoveOutFromFolder.isClickable=true
        binding.SheetDialogMoveOutFromFolder.setOnClickListener {
            when(binding.SheetDialogMoveOutFromFolderCheckBox.isChecked){
                true->{
                    Log.d("BSBSD", "SheetDialog MoveOutFromFolderCheckBox true 清楚其他选项, 因为要移动到主页")
                    viewModel.clearMoveToFolderId()
                }
                false->{
                    Log.d("BSBSD", "SheetDialog MoveOutFromFolderCheckBox false 可以选中其他选项, ")
                    viewModel.prepareMoveToFolder(null)
                }
            }
        }
        finishBTNonClick()

    }
    private fun finishBTNonClick() {
        binding.SheetDialogFinishBTN.setOnClickListener {
            viewModel.gotoMoveBooks()
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dismiss()
        binding.SheetDialogRecyclerView.adapter=null
        _binding=null
        adapter =null
    }

}