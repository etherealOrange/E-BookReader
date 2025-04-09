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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
            launch {
                viewModel.Folders.collectLatest {
                    adapter?.submitList(it.filter {
                        (viewModel.isInFolder.value==true && it.folderId !=viewModel.inWhichFolder.value)
                                || viewModel.isInFolder.value==false
                    }).also {
                        if (it==null){
                            Log.d("BSBSD", "onViewCreated: adapter 为NULL")
                        }
                    }
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
                    Log.d("BSBSD", "setOnClickListener true ")
                    viewModel.clearMoveToFolderId()
                }
                false->{
                    Log.d("BSBSD", "setOnClickListener false ")
                    viewModel.getOutOfFolderMoveToFolderId()
                }
            }
        }
        finishBTNonClick()

    }
    private fun finishBTNonClick() {
        binding.SheetDialogFinishBTN.setOnClickListener {
            when(binding.SheetDialogMoveOutFromFolderCheckBox.isChecked){
                true->{
                    viewModel.moveBooksToFolder(true)
                }
                false->{
                    viewModel.moveBooksToFolder()
                }
            }
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