package com.example.ebook_reader.ui.BookShelf

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ebook_reader.databinding.FragmentBookShelfBinding
import com.example.ebook_reader.databinding.InputNewFolderBoxBinding
import com.example.ebook_reader.databinding.InputTextboxBinding
import com.example.ebook_reader.entities.InsideFolderName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URI
import androidx.core.net.toUri

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


    /**
     * 自动更新TopBar的可见性模式
     *
     * 还有更新TopBar的 现在文件夹名称
    */
    private fun updateTopBarVisibility(){
        //更新TopBar的可见性模式
        lifecycleScope.launch {
            launch {
                combine(viewModel.isInFolder,viewModel.isEditModel) {
                        isInFolder, isEditModel ->
                    when (isInFolder){
                        true ->{
                            topICD.DefaultTopBar.visibility = View.GONE
                            topICD.InFolderTopBar.visibility = View.VISIBLE
                            topICD.EditModuleTopBar.visibility = View.GONE
                            when(isEditModel){
                                true->{
                                    topICD.BookShelfEditInFolderBTN.visibility = View.GONE
                                    topICD.BookShelfFinishInFolderBTN.visibility = View.VISIBLE
                                }
                                false->{
                                    topICD.BookShelfEditInFolderBTN.visibility = View.VISIBLE
                                    topICD.BookShelfFinishInFolderBTN.visibility = View.GONE
                                }
                            }
                        }
                        false ->{
                            topICD.InFolderTopBar.visibility = View.GONE
                            when(isEditModel){
                                true->{
                                    topICD.DefaultTopBar.visibility = View.GONE
                                    topICD.EditModuleTopBar.visibility = View.VISIBLE
                                }
                                false->{
                                    topICD.DefaultTopBar.visibility = View.VISIBLE
                                    topICD.EditModuleTopBar.visibility = View.GONE
                                }
                            }
                        }
                    }
                }.collectLatest {  }
            }
            //更新TopBar的 现在文件夹名称
            launch {
                viewModel.inFolderName.collectLatest {
                    topICD.BookShelfFolderNameTV.text = it

                }
            }
        }
    }

    /**
     * 更新BottomBar的可见性模式 和 按钮的可用性模式
     */
    private fun updateBottomBarVisibility(){
        lifecycleScope.launch {
            //更新BottomBar的总体可见性模式
            launch {
                combine(viewModel.isInFolder,viewModel.isEditModel) {
                        isInFolder, isEditModel ->
                    when(isInFolder or isEditModel){
                        true->{
                            bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
                            when(isInFolder){
                                true->bottomICD.BookShelfRenameFolderBTN.visibility = View.GONE
                                false->bottomICD.BookShelfRenameFolderBTN.visibility = View.VISIBLE
                            }
                        }
                        false->bottomICD.BookShelfBottomBarTotal.visibility = View.GONE
                    }
                }.collectLatest {  }
            }
            //更新BottomBar的移动按钮可用性模式
            launch {
                viewModel.canMoveBooks.collectLatest {
                    when(it){
                        true->{
                            bottomICD.BookShelfMoveBTN.isEnabled = true
                        }
                        false->{
                            bottomICD.BookShelfMoveBTN.isEnabled = false
                        }
                    }
                }
            }
            //更新BottomBar的删除按钮可用性模式
            launch {
                viewModel.isSelectThings.collectLatest {
                    when(it){
                        true->{
                            bottomICD.BookShelfDeleteBTN.isEnabled = true
                        }
                        false->{
                            bottomICD.BookShelfDeleteBTN.isEnabled = false
                        }
                    }
                }
            }
            //更新BottomBar的重命名按钮可用性模式
            launch {
                viewModel.isSingleSelectedFolder.collectLatest {
                    when(it){
                        true->{
                            bottomICD.BookShelfRenameFolderBTN.isEnabled = true
                        }
                        false->{
                            bottomICD.BookShelfRenameFolderBTN.isEnabled = false
                        }
                    }
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bookAdapter = initAdapter()
        lifecycleScope.launch {
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
        updateTopBarVisibility()
        updateBottomBarVisibility()
        initAllTopICD(bookAdapter)
        initAllBottomICD()
    }
    private fun initAdapter(): BookAdapter{
        //设置书本的ListAdapter
        val bookAdapter = BookAdapter(viewModel)
        //设置layoutManager和adapter
        binding.BookRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.BookRecyclerView.adapter = bookAdapter
        lifecycleScope.launch {
            //动态更新RecyclerView数据逻辑
            launch {
                viewModel.items.collectLatest { newList ->
                    Log.d("BS initAdapter","更新RecyclerView数据")
                    bookAdapter.submitList(newList)
                }
            }
            //每次修改EditModule都要更新RecyclerView
            launch {
                viewModel.isEditModel.collectLatest {
                    Log.d("BS initAdapter","更新RecyclerView数据")
                    bookAdapter.flashAllViews()
                }
            }
        }
        return bookAdapter
    }

    /**
     * 选择图片  ActivityResultLauncher
     * - 复制到内部存储
     * - 把路径存储到viewModule中
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            uri->
        if (uri != null) {
            Log.d("BS pickImageLauncher","选择的图片URI: $uri")
            saveImageToFolder(uri,InsideFolderName.FOLDERSCOVERFOLDER.displayName)
        } else {
            viewModel.clearCoverDir()
            Log.d("BS pickImageLauncher","没有选择图片")
        }
    }
    //保存图片到指定文件夹
    private fun saveImageToFolder(uri: Uri,folderName: String){
        try {
            //获取文件夹路径
            val customFolder = File(requireContext().filesDir, folderName)
            if (!customFolder.exists()) {
                customFolder.mkdirs()
            }
            //创建文件名
            val fileName = "IMG_${System.currentTimeMillis()}.jpg"
            val outputFile = File(customFolder, fileName)
            requireContext().contentResolver.openInputStream(uri)?.use{
                inputStream ->
                //将inputStream中的数据复制到outputStream中
                inputStream.copyTo(FileOutputStream(outputFile))
            }
            Log.d("BS saveImageToFolder","保存图片成功")
            viewModel.updateCoverDir(outputFile.path)
            Log.d("BS saveImageToFolder","保存图片路径: ${outputFile.path}")
        }catch (e: RuntimeException){
            viewModel.clearCoverDir()
            Log.d("BS saveImageToFolder","保存图片失败 原因:$e")
        }
    }
    /**
     * 初始化所有TopBar的点击事件
     *
     * 包括：
     *
     * - Edit模式 进入按钮
     * - Edit模式 退出按钮
     * - 书本导入按钮 （未完成）
     * - 返回默认页面按钮
     * - 全选按钮
     * - 取消全选按钮
     * - 重命名文件夹按钮
     *
     */
    private fun initAllTopICD(bookAdapter: BookAdapter){
        //设置在默认页面 Edit模式 进入按钮
        topICD.BookShelfEditBTN.setOnClickListener {
            viewModel.switchEditModel()
        }
        //设置在文件夹内 Edit模式 进入按钮
        topICD.BookShelfEditInFolderBTN.setOnClickListener {
            viewModel.switchEditModel()
        }
        //设置两种页面下 Edit模式 退出按钮   完成按钮
        topICD.BookshelfAllDownBTN.setOnClickListener {
            viewModel.switchEditModel()
            viewModel.clearSelectedFolderId()
            viewModel.clearSelectedBooksId()
        }
        topICD.BookShelfFinishInFolderBTN.setOnClickListener {
            viewModel.switchEditModel()
            viewModel.clearSelectedFolderId()
            viewModel.clearSelectedBooksId()
        }
        //书本导入按钮
        topICD.BookshelfBookImportBTN.setOnClickListener {


        }
        //设置在文件夹内 返回默认页面按钮
        topICD.BookshelfBackToDefaultBTN.setOnClickListener {
            viewModel.getOutOfFolder()

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
        //设置在文件夹内 重命名文件夹按钮
        topICD.BookShelfRenameFolderInFolderBTN.setOnClickListener {
            Log.d("BS BookShelfRenameFolderInFolderBTN","before BookShelfRenameFolderInFolderBTN in Folder ${viewModel.isInFolder.value}")
            if(!viewModel.isInFolder.value)return@setOnClickListener
            Log.d("BS BookShelfRenameFolderInFolderBTN","start BookShelfRenameFolderInFolderBTN in Folder")
            showRenameFolderDialog(BookShelfViewModel::renameFolderInFolder)
        }
        //设置在主页 新建文件夹
        topICD.BookshelfNewFolderBTN.setOnClickListener {
            //新建文件夹的视图 和 按钮 逻辑
            val inputNewFolderBoxBinding = InputNewFolderBoxBinding.inflate(LayoutInflater.from(requireContext()))
            inputNewFolderBoxBinding.addFolderCoverBTN.setOnClickListener {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                lifecycleScope.launch {
                    viewModel.CoverDir.collectLatest {
                        Log.d("BS pickImageLauncher", "切换为: ${it.toUri()}")
                        if (it != "") {
                            inputNewFolderBoxBinding.imageShow.setImageURI(it.toUri())
                        }
                    }
                }

            }
            //弹窗
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("新建文件夹")
                .setView(inputNewFolderBoxBinding.root)
                .setPositiveButton("确定") {dialog,_->
                    Log.d("BS ","点击确认")
                    if(inputNewFolderBoxBinding.editTextInput.text?.isEmpty() == true){
                        viewModel.insertNewFolder("新文件夹",viewModel.CoverDir.value)
                    }
                    else
                    {
                        viewModel.insertNewFolder(inputNewFolderBoxBinding.editTextInput.text.toString(),viewModel.CoverDir.value)
                    }
                    viewModel.clearCoverDir()
                }
                .setNegativeButton("取消") { _, _ ->
                    Log.d("BS ","点击取消")
                }
                .create()
                .show()


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
            AlertDialog.Builder(requireContext())
                .setTitle("删除")
                .setMessage("确定删除选中的书本和文件夹吗？(此过程不可逆!)")
                .setPositiveButton("确定") { _, _ ->
                    deleteSelectedItems()
                    Log.d("DeleteBooksAndFolder","成功")
                }
                .setNegativeButton("取消") { _, _ ->
                    yourChoice=false
                    Log.d("DeleteBooksAndFolder","取消删除")
                }
                .create()
                .show()
            Log.d("DeleteBooksAndFolder","$yourChoice")

        }
        //TODO:设置在编辑模式下 在主页编辑模式 重命名文件夹按钮 在文件夹内取消其使用
        bottomICD.BookShelfRenameFolderBTN.setOnClickListener {
            Log.d("BS BookShelfRenameFolderBTN","before renameFolder out of Folder ${viewModel.isInFolder.value}")
            if(viewModel.isInFolder.value)return@setOnClickListener
            Log.d("BS BookShelfRenameFolderBTN","start renameFolder out of Folder")
            showRenameFolderDialog(BookShelfViewModel::renameFolder)

        }
    }

    /**
     * 重命名文件夹按钮的功能
     * @param _renameFolder 传入BookShelfViewModel的两个的重命名函数
     *
     * 如果在文件夹内调用`BookShelfViewModel::renameFolderInFolder`
     *
     * 在主页面调用`BookShelfViewModel::renameFolder`
     */
    private fun showRenameFolderDialog(_renameFolder: BookShelfViewModel.(String)->Unit ){
        val inputBoxBinding: InputTextboxBinding =
            InputTextboxBinding.inflate(LayoutInflater.from(requireContext()))
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("文件夹新名称")
            .setView(inputBoxBinding.root)
            .setPositiveButton("确认") {_,_->
                val newName = inputBoxBinding.editTextInput.text.toString()
                if (newName.isNotEmpty()){
                    viewModel._renameFolder(newName)
                    Log.d("RenameFolder","重命名成功")
                }else{
                    Log.d("RenameFolder","文件夹名称不能为空")
                }
            }
            .setNegativeButton("取消") { _, _ ->
                Log.d("RenameFolder","取消重命名")
            }
            .create()
            .show()
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

