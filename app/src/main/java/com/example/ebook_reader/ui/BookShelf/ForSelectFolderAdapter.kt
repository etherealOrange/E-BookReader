package com.example.ebook_reader.ui.BookShelf

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.compose.runtime.ScopeUpdateScope
import androidx.compose.runtime.currentRecomposeScope
import androidx.core.net.toUri
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import com.example.ebook_reader.InterfacePackage.BookShelf.FoldersAdapterSelectedControl
import com.example.ebook_reader.R
import com.example.ebook_reader.Repository.BookShelf.FolderAdapterUIState
import com.example.ebook_reader.databinding.CardviewForselectfolderBinding
import com.example.ebook_reader.entities.FolderView
import com.example.ebook_reader.entities.UIFolderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForSelectFolderAdapter (
    private val selectedControl: FoldersAdapterSelectedControl
)
    : ListAdapter<FolderAdapterUIState, ForSelectFolderAdapter.ViewHolder>(
    FolderDiffCallBack()
) {
    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewForselectfolderBinding): RecyclerView.ViewHolder(binding.root){
        //获取当前ViewHolder的位置的folderId
        private var thisItemId: Long = 0
        //创建作用域
        init{
            Log.d("FSFA init", "在Adapter ${this@ForSelectFolderAdapter} \n当前ViewHolder $this \n hashCode ${this.hashCode()}")
            binding.root.setOnClickListener {
                //设置点击文件夹选中 取消选中效果
                when(binding.CDSelectedCheckBox.isChecked){
                    true->selectedControl.clearMoveToFolderId()
                    false->selectedControl.prepareMoveToFolder(thisItemId)
                }
            }
        }
        fun setItemId(id: Long){
            thisItemId = id
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardviewForselectfolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    //绑定数据
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.setItemId(item.folder.folder.folderId)
        //设置文件夹的选中状态
        holder.binding.CDSelectedCheckBox.isChecked=item.isSelected
        //处理Folder视图的绑定
        bindFolder(holder, item.folder)
    }
    //绑定 FolderView  没有添加 头选项
    private fun bindFolder(
        holder: ViewHolder,
        view: UIFolderView
    ) {
        val binding = holder.binding
        //选择要显示 文件夹 视图
        binding.CDSelectedCheckBox.visibility = View.VISIBLE
        binding.CDSelectedCheckBox.isClickable = false
        binding.DefaultSelectFolderCardView.visibility = View.VISIBLE
        //图片加载逻辑
        val coverUrl = view.folder.coverUrl
        if(coverUrl.isNotEmpty()){
            binding.CDFolderCoverIV.load(coverUrl.toUri())
        }
        else{
            binding.CDFolderCoverIV.load(R.drawable.ic_launcher_foreground)
        }
        binding.CDFolderNameTV.text = view.folder.title
        val containBooksText = "共${view.booksNum}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
}

}