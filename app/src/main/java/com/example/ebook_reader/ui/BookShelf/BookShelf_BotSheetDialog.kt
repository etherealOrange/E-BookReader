package com.example.ebook_reader.ui.BookShelf

import android.os.Bundle
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
        val adapter = ForSelectFolderAdapter(viewModel)
        binding.SheetDialogRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.SheetDialogRecyclerView.adapter = adapter
        lifecycleScope.launch {
            viewModel.Folders.collectLatest {
                adapter.submitList(it)
            }
        }
        lifecycleScope.launch {
            viewModel.isInFolder.collectLatest {
                if (it){
                    binding.SheetDialogMoveOutFromFolder.visibility = View.VISIBLE
                }else{
                    binding.SheetDialogMoveOutFromFolder.visibility = View.GONE
                }
            }
        }

        binding.SheetDialogFinishBTN.setOnClickListener {
            dismiss()
        }

    }


}