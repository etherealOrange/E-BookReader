package com.example.ebook_reader.ui.BookShelf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ebook_reader.databinding.CardviewForselectfolderBinding
import com.example.ebook_reader.entities.FolderView

class ForSelectFolderAdapter (
    private val viewModel: BookShelfDataViewModel): ListAdapter<FolderView, ForSelectFolderAdapter.ViewHolder>(
    FolderDiffCallBack()
) {
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewForselectfolderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardviewForselectfolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    //绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        //处理Folder视图的绑定
        bindFolder(holder, item)
    }
    //绑定 FolderView  没有添加 头选项
    private fun bindFolder(
        holder: ViewHolder,
        view: FolderView
    ) {
        val binding = holder.binding
        //选择要显示 文件夹 视图
        binding.CDSelectedCheckBox.visibility = View.VISIBLE
        binding.DefaultSelectFolderCardView.visibility = View.VISIBLE

        //图片加载逻辑
//        binding.CDFolderCoverIV
        binding.CDFolderNameTV.text = view.title
        val containBooksText = "共${viewModel.getBooksNumInFolder(view.folderId)}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
}

}