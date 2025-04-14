package com.example.ebook_reader.ui.BookShelf

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import com.example.ebook_reader.Enum.BookShelfState
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.entities.BookTypesName
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookShelf : ExtendFragment() {
    //ViewBinding
    private var _binding: FragmentBookShelfBinding? = null
    private val binding get() = _binding!!
    //TopBar和BottomBar include布局 和底部抽屉的布局
    private val topICD get() = binding.BookShelfTopBarICD
    private val bottomICD get() = binding.BookshelfBottomBarICD
    private var _bottomSheetDialog: BookShelf_BotSheetDialog? = BookShelf_BotSheetDialog()
    private val bottomSheetDialog get() = _bottomSheetDialog!!

    private var bookAdapter: BookAdapter? = null
    private var alertDialog: AlertDialog?=null


    //获取Activity共享的ViewModel
    private val viewModel: BookShelfDataViewModel by activityViewModels()
    private val UIVM : BookShelfUIViewModel by viewModels()

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
        UIVM.state_now
            .launchLifeScopeCollectLatest {
                when(it){
                    BookShelfState.InEditInFolder,BookShelfState.NotInEditInFolder->{
                        topICD.DefaultTopBar.visibility = View.GONE
                        topICD.InFolderTopBar.visibility = View.VISIBLE
                        topICD.EditModuleTopBar.visibility = View.GONE
                    }
                    else -> {
                        topICD.InFolderTopBar.visibility = View.GONE
                    }
                }
                when(it){
                    BookShelfState.InEditInFolder -> {
                        topICD.BookShelfEditInFolderBTN.visibility = View.GONE
                        topICD.BookShelfFinishInFolderBTN.visibility = View.VISIBLE
                    }
                    BookShelfState.InEditNotInFolder -> {
                        topICD.EditModuleTopBar.visibility = View.VISIBLE
                    }
                    BookShelfState.NotInEditInFolder -> {
                        topICD.BookShelfEditInFolderBTN.visibility = View.VISIBLE
                        topICD.BookShelfFinishInFolderBTN.visibility = View.GONE
                    }
                    BookShelfState.NotInEditNotInFolder -> {
                        topICD.DefaultTopBar.visibility= View.VISIBLE
                    }
                }

        }
        //更新TopBar的 现在文件夹名称
        UIVM.inFolderName.launchLifeScopeCollectLatest {
            Log.d("BS 更新顶部TopBar","更新TopBar的文件夹名称 $it")
            topICD.BookShelfFolderNameTV.text = it
        }

    }


    /**
     * 更新BottomBar的可见性模式
     */
    private fun updateBottomBarVisibility(){
        //更新BottomBar的总体可见性模式
        UIVM.state_now.launchLifeScopeCollectLatest {
            when(it){
                BookShelfState.NotInEditNotInFolder -> {bottomICD.BookShelfBottomBarTotal.visibility = View.GONE}
                BookShelfState.InEditInFolder,
                BookShelfState.NotInEditInFolder
                         -> {
                    bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
                    bottomICD.BookShelfRenameFolderBTN.visibility = View.GONE
                }
                BookShelfState.InEditNotInFolder -> {
                    bottomICD.BookShelfBottomBarTotal.visibility = View.VISIBLE
                    bottomICD.BookShelfRenameFolderBTN.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        //只有在选中一个文件夹时才可以重命名文件夹
        viewModel.isSingleSelectedFolder.launchLifeScopeCollectLatest {
            Log.d("BS onViewCreated","更新BottomBar的重命名按钮可用性模式 $it")
            bottomICD.BookShelfRenameFolderBTN.isEnabled = it
        }
        //动态更新是否可以移动书本可用性模式
        viewModel.canMoveBooks.launchLifeScopeCollectLatest {
            Log.d("BS onViewCreated","更新BottomBar的移动按钮可用性模式 $it")
            bottomICD.BookShelfMoveBTN.isEnabled = it
        }
        //更新BottomBar的删除按钮可用性模式
        viewModel.isSelectThings.launchLifeScopeCollectLatest {
            Log.d("BS onViewCreated","更新BottomBar的删除按钮可用性模式 $it")
            bottomICD.BookShelfDeleteBTN.isEnabled = it
        }

        updateTopBarVisibility()
        updateBottomBarVisibility()
        initAllTopICD()
        initAllBottomICD()
    }

    private fun initAdapter(){
        //设置书本的ListAdapter
        if(bookAdapter==null){
            bookAdapter = BookAdapter(viewModel,viewModel)
        }
        //设置layoutManager和adapter
        binding.BookRecyclerView.layoutManager = GridLayoutManager(context, 3)
        binding.BookRecyclerView.adapter = bookAdapter
        //动态更新RecyclerView数据逻辑
        viewModel.booksAdapterUIItems.launchLifeScopeCollectLatest {
            Log.d("BS 初始化Adapter","initAdapter 更新RecyclerView的显示数据")
            bookAdapter!!.submitList(it)
        }


    }

    /**
     * 选择图片  ActivityResultLauncher
     * - 复制到内部存储
     * - 把路径存储到viewModule中
     */
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
            uri->
        if (uri != null) {
            Log.d("BS 进行文件夹封面保存","pickImageLauncher 选择的图片URI: $uri")
            saveImageToFolder(uri,InsideFolderName.FOLDERSCOVERFOLDER.displayName)
        } else {
            UIVM.clearCoverDir()
            Log.d("BS 没有图片","pickImageLauncher 没有选择图片")
        }
    }

    /**
     *保存图片到指定文件夹
     */
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
            Log.d("BS saveImageToFolder","saveImageToFolder 保存图片成功")
            UIVM.updateCoverDir(outputFile.path)
            Log.d("BS saveImageToFolder","saveImageToFolder 保存图片路径: ${outputFile.path}")
        }catch (e: RuntimeException){
            UIVM.clearCoverDir()
            Log.d("BS saveImageToFolder","saveImageToFolder 保存图片失败 原因:$e")
        }
    }

    /**
     * 导入书籍的 ActivityResultLauncher
     */
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        if(it !=null){
            copyFileToFolder(it)
        }
        else{
            Log.d("BS filePickerLauncher","没有选择文件")
        }

    }
    /**
     * 处理选中的书籍
     * - 复制到对应内部存储
     * - 数据库要插入书籍
     * - 插入txt pdf epub是不同的
     * - 插入txt时需要解析txt文件
     * - 得到章节信息 后还需要插入章节
     * - pdf 需要得到总页数
     * - epub 需要解析得到章节和多媒体信息
     * - 多媒体可能需要存储到另外的文件夹
     * - 还需要解析章节 插入章节信息
     */
    private fun copyFileToFolder(uri: Uri) {
        try {
            val fileExtend:String? = getFileExtendFromUri(uri)
            if(fileExtend==null||!BookTypesName.isInBookTypesName(fileExtend)){
                Log.d("BS filePickerLauncher","文件格式不支持")
                return
            }
            var folderPath =""
            when(fileExtend){
                BookTypesName.TXT.extension ->{
                    folderPath = InsideFolderName.TXTBOOKSFOLDER.displayName
                    Log.d("BS filePickerLauncher","选择的文件格式是TXT")
                }
                BookTypesName.EPUB.extension ->{
                    folderPath = InsideFolderName.EPUBBOOKSFOLDER.displayName
                    Log.d("BS filePickerLauncher","选择的文件格式是EPUB")
                }
                BookTypesName.PDF.extension ->{
                    folderPath = InsideFolderName.PDFBOOKSFOLDER.displayName
                    Log.d("BS filePickerLauncher","选择的文件格式是PDF")
                }
            }
            val customFolder = File(requireContext().filesDir, folderPath)
            if (!customFolder.exists()) {
                customFolder.mkdirs()
            }
            val fileName = "${System.currentTimeMillis()}."+fileExtend
            val outputFile = File(customFolder, fileName)
            requireContext().contentResolver.openInputStream(uri)?.use{
                inputStream ->
                //将inputStream中的数据复制到outputStream中
                inputStream.copyTo(FileOutputStream(outputFile))
            }
            //TODO三种文件解析
            Log.d("BS filePickerLauncher","filePickerLauncher 保存书籍路径: ${outputFile.path}")

        }
        catch (e: RuntimeException){
            Log.d("BS filePickerLauncher","copyFileToFolder 复制文件失败 原因:$e")
        }
    }

    /**
     * 找到文件的扩展名
     */
    private fun getFileExtendFromUri(uri: Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        if (cursor == null)
            Log.d("BS getFileExtendFromUri", "cursor is null")
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    Log.d("CursorContent", "未找到 columnIndex: $columnIndex")
                }
                if (columnIndex >= 0) {
                    return it.getString(columnIndex).substringAfterLast('.',"")
                }
            }
        }
        return null
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
     * - 新建文件夹按钮
     *
     */
    private fun initAllTopICD(){
        //设置在默认页面 Edit模式 进入按钮
        topICD.BookShelfEditBTN.setOnClickListener {
            viewModel.switchEditModel(bookAdapter)
        }
        //设置在文件夹内 Edit模式 进入按钮
        topICD.BookShelfEditInFolderBTN.setOnClickListener {
            viewModel.switchEditModel(bookAdapter)
        }
        //设置两种页面下 Edit模式 退出按钮   完成按钮
        topICD.BookshelfAllDownBTN.setOnClickListener {
            viewModel.switchEditModel(bookAdapter)
        }
        topICD.BookShelfFinishInFolderBTN.setOnClickListener {
            viewModel.switchEditModel(bookAdapter)
        }
        //书本导入按钮
        topICD.BookshelfBookImportBTN.setOnClickListener {
            if (Environment.isExternalStorageManager()) {
                Log.d("BS filePickerLauncher","权限已经授予")
                filePickerLauncher.launch(arrayOf(
                    "text/plain","application/pdf","application/epub+zip"
                ))
            } else {
                // 打开系统的权限管理页面
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
        //设置在文件夹内 返回默认页面按钮
        topICD.BookshelfBackToDefaultBTN.setOnClickListener {
            Log.d("BS BookshelfBackToDefaultBTN","点击返回主页")
            viewModel.getOutOfFolder()
        }
        //设置TopBar的全选按钮
        topICD.BookShelfAllSelectBTN.setOnClickListener {
            Log.d("BS BookshelfAllSelectBTN","点击全选")
            viewModel.selectAllBooksAndFolders()
        }
        //设置TopBar的取消全选按钮
        topICD.BookshelfCancelSelectBTN.setOnClickListener {
            Log.d("BS BookshelfCancelSelectBTN","点击取消全选")
            viewModel.clearAllSelected()
        }
        //设置在文件夹内 重命名文件夹按钮
        topICD.BookShelfRenameFolderInFolderBTN.setOnClickListener {
            Log.d("BS BookShelfRenameFolderInFolderBTN","before BookShelfRenameFolderInFolderBTN in Folder ${UIVM.isInFolder.value}")
            if(!UIVM.isInFolder.value)return@setOnClickListener
            Log.d("BS BookShelfRenameFolderInFolderBTN","start BookShelfRenameFolderInFolderBTN in Folder")
            showRenameFolderDialog(BookShelfDataViewModel::renameFolderInFolder)
        }
        //设置在主页 新建文件夹
        topICD.BookshelfNewFolderBTN.setOnClickListener {
            //新建文件夹的视图 和 按钮 逻辑
            val inputNewFolderBoxBinding = InputNewFolderBoxBinding.inflate(LayoutInflater.from(requireContext()))
            inputNewFolderBoxBinding.addFolderCoverBTN.setOnClickListener {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                lifecycleScope.launch {
                    UIVM.CoverDir.collectLatest {
                        Log.d("BS BookshelfNewFolderBTN Top", "切换为: ${it.toUri()}")
                        if (it != "") {
                            inputNewFolderBoxBinding.imageShow.setImageURI(it.toUri())
                        }
                    }
                }
            }

            alertDialog?.dismiss()
            //弹窗
            alertDialog = dialogBuilderFactory("新建文件夹",inputNewFolderBoxBinding.root,
                {_,_->
                    Log.d("BS 新建文件夹","点击确认")
                    if(inputNewFolderBoxBinding.editTextInput.text?.isEmpty() == true){
                        viewModel.insertNewFolder("新文件夹",UIVM.CoverDir.value)
                    }
                    else
                    {
                        viewModel.insertNewFolder(inputNewFolderBoxBinding.editTextInput.text.toString(),UIVM.CoverDir.value)
                    }
                    UIVM.clearCoverDir()
                },
                { _, _ ->
                    Log.d("BS 新建文件夹","点击取消")
                }
            ).also { it.show() }

        }
    }
    /**
     * 初始化所有BottomBar的点击事件
     */
    private fun initAllBottomICD(){
        //设置书本移动按钮
        moveBTNClickListener()
        //设置在编辑模式下 两种页面 删除按钮  添加删除确认弹窗

        bottomICD.BookShelfDeleteBTN.setOnClickListener {
            alertDialog?.dismiss()
            var yourChoice: Boolean? = null
            alertDialog = dialogBuilderFactory("删除书本或文件夹",
                "确定删除选中的书本和文件夹吗？(此过程不可逆!)",
                { _, _ ->
                    deleteSelectedItems()
                    yourChoice = true
                    Log.d("BS DeleteBooksAndFolder","成功")
                },
                { _, _ ->
                    yourChoice = false
                    Log.d("BS DeleteBooksAndFolder","取消删除")
                }
            ).also {
                it.show()
            }
            Log.d("BS DeleteBooksAndFolder","$yourChoice")

        }
        //设置在编辑模式下 在主页编辑模式 重命名文件夹按钮 在文件夹内取消其使用
        bottomICD.BookShelfRenameFolderBTN.setOnClickListener {
//            Log.d("BS BookShelfRenameFolderBTN","before renameFolder out of Folder ${viewModel.isInFolder.value}")
            if(UIVM.isInFolder.value)return@setOnClickListener
//            Log.d("BS BookShelfRenameFolderBTN","start renameFolder out of Folder")
            showRenameFolderDialog(BookShelfDataViewModel::renameFolder)

        }
    }
    /**
     * 移动书本按钮逻辑
     * 1. 打开底部抽屉
     * 2. 显示可以移动的文件夹
     * 3. 选择一个文件夹
     *
     *    ├─ 不选择 选中文件夹为空
     *
     *    └─ 选择文件夹 选中文件夹为那个folderId
     * 4. 点击完成按钮
     * 5. 把选中的书本修改数据库
     *
     *    ├─ 修改后的folderId不存在 不修改数据库
     *
     *    └─ 修改后的folderId不存在 修改数据库folderId
     * 6. 刷新Adapter
     * 7. 关闭底部抽屉
     */
    private fun moveBTNClickListener(){
        bottomICD.BookShelfMoveBTN.setOnClickListener {
            bottomSheetDialog.show(parentFragmentManager,bottomSheetDialog.tag )

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
    private fun showRenameFolderDialog(_renameFolder: BookShelfDataViewModel.(String)->Unit ){
        val inputBoxBinding: InputTextboxBinding =
            InputTextboxBinding.inflate(LayoutInflater.from(requireContext()))
        alertDialog?.dismiss()
        alertDialog = dialogBuilderFactory("文件夹新名称",inputBoxBinding.root,
            {_,_->
                val newName = inputBoxBinding.editTextInput.text.toString()
                if (newName.isNotEmpty()){
                    viewModel._renameFolder(newName)
                    Log.d("BS RenameFolder","重命名成功")
                }else{
                    Log.d("BS RenameFolder","文件夹名称不能为空")
                }
            },
            { _, _ ->
                Log.d("BS RenameFolder","取消重命名")
            }
        ).also {
            it.show()
        }
    }

    /**
     * MaterialAlertDialogBuilder 对话框创建工厂
     */
    private fun dialogBuilderFactory(
        title: String,
        view: View,
        positiveButtonClickListener: (DialogInterface, Int) -> Unit,
        negativeButtonClickListener: (DialogInterface, Int) -> Unit,
        positiveButtonText: String = "确定",
        negativeButtonText: String = "取消",
    ): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(positiveButtonText, positiveButtonClickListener)
            .setNegativeButton(negativeButtonText, negativeButtonClickListener)
            .create()
    }
    /**
     * MaterialAlertDialogBuilder 对话框创建工厂
     */
    private fun dialogBuilderFactory(
        title: String,
        message: String,
        positiveButtonClickListener: (DialogInterface, Int) -> Unit,
        negativeButtonClickListener: (DialogInterface, Int) -> Unit,
        positiveButtonText: String = "确定",
        negativeButtonText: String = "取消",
    ): AlertDialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText, positiveButtonClickListener)
            .setNegativeButton(negativeButtonText, negativeButtonClickListener)
            .create()
    }

    /**
     * 在文件夹内只删除书本, 在主页删除书本和文件夹
     */
    private fun deleteSelectedItems(){
        when(UIVM.isInFolder.value){
            true -> viewModel.deleteSelectedBooks()
            false -> viewModel.deleteSelectedAll()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("BS onStart","success")
    }
    override fun onStop() {
        super.onStop()
        Log.d("BS onStop","success")
    }
    override fun onDestroy() {
        super.onDestroy()

        binding.BookRecyclerView.adapter=null
        _binding = null
        bookAdapter = null
        _bottomSheetDialog?.dismiss()
        _bottomSheetDialog = null
        alertDialog?.dismiss()
        alertDialog=null
        Log.d("BS onDestroy","success")
    }
}

