package com.example.ebook_reader.ui.DataStatistic

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import coil3.load
import com.example.ebook_reader.ConfigManager
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.Repository.DataStatistic.BookDataUI
import com.example.ebook_reader.Tools
import com.example.ebook_reader.databinding.FragmentDataStatisticBinding
import com.example.ebook_reader.databinding.StatisticBookItemBinding
import com.example.ebook_reader.ui.ReadingBook.Reading_EPUB
import com.example.ebook_reader.ui.ReadingBook.Reading_PDF
import com.example.ebook_reader.ui.ReadingBook.Reading_TXT
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.math.ceil

@AndroidEntryPoint
class DataStatistic : ExtendFragment() {
    private val viewModel: DataStatisticViewModel by viewModels()
    private lateinit var bind: FragmentDataStatisticBinding
    private lateinit var longTimeBind: StatisticBookItemBinding
    private lateinit var longPerPageBind: StatisticBookItemBinding
    private lateinit var mostMarksNumBing: StatisticBookItemBinding



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.oneMonth()
        val dayToCreate = ConfigManager.getInstance(requireContext()).getDayToCreate()
        val s = "${ceil((System.currentTimeMillis()-dayToCreate)/1000/60/60/24.0).toLong()}天"
        bind.days.text = s
        viewModel.totalTime.launchLifeScopeCollectLatest{
            val s = "${Tools.toMinute(it)}分钟(${Tools.toHour(it)}小时)"
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
            val s = "${Tools.toSecond(it)}秒(${Tools.toMinute(it)}分钟)"
            bind.avgPerPage.text = s
        }
        viewModel.longTimeBook.launchLifeScopeCollectLatest {
            if(it!=null && it.book!=null){
                longTimeBind.showSpecial.text = Tools.longBookTimeS(it.long?:0L)
                bindICD(longTimeBind,it)
            }
        }
        viewModel.longPerPageTimeBook.launchLifeScopeCollectLatest {
            if(it!=null && it.book!=null){
                longPerPageBind.showSpecial.text = Tools.longPerPageTimeS(it.perPageTime?:0L)
                bindICD(longPerPageBind,it)
            }
        }
        viewModel.bookMarksMostBook.launchLifeScopeCollectLatest {
            if(it!=null && it.book!=null){
                mostMarksNumBing.showSpecial.text = Tools.bookMarkNumS(it.bookMarkNum?:0L)
                bindICD(mostMarksNumBing,it)
            }
        }

        bind.toggleTimeStart.addOnButtonCheckedListener {
                group, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    bind.oneMonthBtn.id->viewModel.oneMonth()
                    bind.oneYearBtn.id->viewModel.oneYear()
                    bind.sixMonthBtn.id->viewModel.sixMonth()
                }
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