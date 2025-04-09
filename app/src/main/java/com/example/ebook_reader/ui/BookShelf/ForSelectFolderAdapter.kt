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
import com.example.ebook_reader.R
import com.example.ebook_reader.databinding.CardviewForselectfolderBinding
import com.example.ebook_reader.entities.FolderView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ForSelectFolderAdapter (
    private val viewModel: BookShelfDataViewModel): ListAdapter<FolderView, ForSelectFolderAdapter.ViewHolder>(
    FolderDiffCallBack()
) {
    private val lifecycleScope = CoroutineScope(
    SupervisorJob() + Dispatchers.Main.immediate
    )

    //ViewHolder内部类, 创建ViewHolder实例
    inner class ViewHolder(val binding: CardviewForselectfolderBinding): RecyclerView.ViewHolder(binding.root){
        //获取当前ViewHolder的位置的folderId
        private var thisItemId: Long = 0
        //创建作用域
        init{
            Log.d("FSFA init", "在Adapter ${this@ForSelectFolderAdapter} \n当前ViewHolder $this \n hashCode ${this.hashCode()}")
            lifecycleScope.launch {
                viewModel.moveToFolderId.collectLatest {
                    if(it==null){
                        Log.d("FSFA init", "现在ViewHolder的 在主页 id $thisItemId  -> null")
                        binding.CDSelectedCheckBox.isChecked = false
                        return@collectLatest
                    }
                    //如果当前文件夹id和选中的文件夹id相同
                    when(thisItemId==it){
                        true->{
                            Log.d("FSFA init", "现在ViewHolder的 id $thisItemId  -> true")
                            binding.CDSelectedCheckBox.isChecked = true
                        }
                        false->{
                            Log.d("FSFA init", "现在ViewHolder的 id $thisItemId  -> false")
                            binding.CDSelectedCheckBox.isChecked = false
                        }
                    }
                }
            }

            binding.root.setOnClickListener {
                when(binding.CDSelectedCheckBox.isChecked){
                    true->viewModel.clearMoveToFolderId()
                    false->viewModel.updateMoveToFolderId(thisItemId)
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
        holder.setItemId(item.folderId)
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
        binding.CDSelectedCheckBox.isClickable = false
        binding.DefaultSelectFolderCardView.visibility = View.VISIBLE
        binding.root.post {
            viewModel.moveToFolderId.value.let {
                binding.CDSelectedCheckBox.isChecked= it == view.folderId
            }
        }


        //图片加载逻辑
        val coverUrl = view.coverUrl
        if(coverUrl.isNotEmpty()){
            binding.CDFolderCoverIV.setImageURI(coverUrl.toUri())
        }
        else{
            binding.CDFolderCoverIV.setImageResource(R.drawable.ic_launcher_foreground)
        }
        binding.CDFolderNameTV.text = view.title
        val containBooksText = "共${viewModel.getBooksNumInFolder(view.folderId)}本书"
        binding.CDFolderContainBooksTV.text = containBooksText
}

}