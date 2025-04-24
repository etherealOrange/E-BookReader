package com.example.ebook_reader.ui.DataStatistic

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import coil3.load
import coil3.toUri
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Repository.DataStatistic.Duration
import com.example.ebook_reader.databinding.FragmentDataStatisticBinding
import com.example.ebook_reader.databinding.StatisticBookItemBinding
import com.example.ebook_reader.ui.ReadingBook.Reading_EPUB
import com.example.ebook_reader.ui.ReadingBook.Reading_PDF
import com.example.ebook_reader.ui.ReadingBook.Reading_TXT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.last
import java.io.File
import java.nio.file.Files

@AndroidEntryPoint
class DataStatistic : ExtendFragment() {
    private val viewModel: DataStatisticViewModel by viewModels()
    private lateinit var bind: FragmentDataStatisticBinding
    private lateinit var longTimeBind: StatisticBookItemBinding
    private lateinit var longPerPageBind: StatisticBookItemBinding
    private lateinit var mostMarksNumBing: StatisticBookItemBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.totalTime.launchLifeScopeCollectLatest{
            val s = "${((it/1000/60)%60).toLong()}分钟(${(it/1000/60/60).toLong()}小时)"
            bind.totalTimes.text = s
        }
        viewModel.totalPages.launchLifeScopeCollectLatest {
            val s = "${it}页"
            bind.totalPages.text = s
        }
        viewModel.totalBookMarks.launchLifeScopeCollectLatest {
            val s = "${it}个"
            bind.totalBookMarks.text = s
        }
        viewModel.totalPerPageTime.launchLifeScopeCollectLatest {
            val s = "${(it/1000).toLong()}秒(${(it/1000/60).toLong()}分钟)"
            bind.avgPerPage.text = s
        }
        viewModel.longTimeBook.launchLifeScopeCollectLatest {
            if(it!=null && it.long!=null && it.book!=null){
                val s = "共阅读:${((it.long/1000/60)%60).toLong()}分钟(${(it.long/1000/60/60).toLong()}小时)"
                longTimeBind.showSpecial.text = s
                bindICD(longTimeBind,it)
            }
        }
        viewModel.longPerPageTimeBook.launchLifeScopeCollectLatest {
            if(it!=null && it.perPageTime!=null && it.book!=null){
                val s = "平均每页:${(it.perPageTime/1000).toLong()}秒(${(it.perPageTime/1000/60).toLong()}分钟)"
                longPerPageBind.showSpecial.text = s
                bindICD(longPerPageBind,it)
            }
        }
        viewModel.bookMarksMostBook.launchLifeScopeCollectLatest {
            if(it!=null && it.bookMarkNum!=null && it.book!=null){
                val s = "创建书签:${it.bookMarkNum}个"
                mostMarksNumBing.showSpecial.text = s
                bindICD(mostMarksNumBing,it)
            }
        }



    }

    private fun bindICD(bind: StatisticBookItemBinding,book: BookDataUI){
        if(book.book==null) return
        bind.title.text = book.book.title
        //这里需要看一下
        val f = File(requireContext().filesDir,book.book.coverUrl)
        if(f.isFile){
            bind.image.load(f.path.toUri())
        }
        val c = "章节数:${book.book.currentPage}/${book.book.totalPages}"
        bind.chapter.text = c
        bind.root.setOnClickListener {
            var intent: Intent? = null
            when(book.book.bookType){
                BookType.TXT ->{ intent = Intent(requireContext(), Reading_TXT::class.java)}
                BookType.PDF -> { intent = Intent(requireContext(), Reading_PDF::class.java)}
                BookType.EPUB -> { intent = Intent(requireContext(), Reading_EPUB::class.java)}
            }
            intent.putExtra("book", book.book)
            startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentDataStatisticBinding.inflate(inflater, container, false)
        longTimeBind = bind.longTimeBook
        longPerPageBind = bind.longPerPageBook
        mostMarksNumBing = bind.bookMarkMostBook
        return bind.root
    }
}