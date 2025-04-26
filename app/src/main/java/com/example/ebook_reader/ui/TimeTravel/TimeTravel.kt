package com.example.ebook_reader.ui.TimeTravel

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.InterfacePackage.BookShelf.BooksAdapterOpenActivity
import com.example.ebook_reader.R
import com.example.ebook_reader.databinding.FragmentTimeTravelBinding
import com.example.ebook_reader.entities.BookView
import com.example.ebook_reader.ui.ReadingBook.Reading_EPUB
import com.example.ebook_reader.ui.ReadingBook.Reading_PDF
import com.example.ebook_reader.ui.ReadingBook.Reading_TXT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TimeTravel
    : ExtendFragment()
, BooksAdapterOpenActivity
{
    private val viewModel: TimeTravelViewModel by viewModels()
    private lateinit var bind : FragmentTimeTravelBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateCurrentMode(TimeTravelViewModel.Mode.Duration)

        val adapterOfYear = YearAdapter(viewModel)
        bind.yearRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = adapterOfYear
        }
        viewModel.yearList.launchLifeScopeCollectLatest {
            adapterOfYear.submitList(it)
        }

        //切换显示的统计内容
        bind.toggleModeChange.addOnButtonCheckedListener {
            group, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    bind.durationBtn.id->viewModel.updateCurrentMode(TimeTravelViewModel.Mode.Duration)
                    bind.perPageTimeBtn.id-> viewModel.updateCurrentMode(TimeTravelViewModel.Mode.PerPageTime)
                    bind.bookMarkBtn.id->viewModel.updateCurrentMode(TimeTravelViewModel.Mode.BookMark)
                }
            }
        }

        val adapterOfBooks = TopTenBookAdapter(this)

        bind.rankingRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = adapterOfBooks
        }
        viewModel.currentRangeBooks.launchLifeScopeCollectLatest {
            adapterOfBooks.submitList(it)
            bind.rankingRecyclerView.scrollTo(0,0)
        }

        viewModel.currentMode.launchLifeScopeCollectLatest {
            adapterOfBooks.switchModeTo(it)
        }


    }

    override fun openActivity(book: BookView) {
        val intent = when(book.bookType){
            BookType.TXT -> Intent(requireContext(), Reading_TXT::class.java)
            BookType.PDF -> Intent(requireContext(), Reading_PDF::class.java)
            BookType.EPUB -> Intent(requireContext(), Reading_EPUB::class.java)
        }
        intent.putExtra("book", book)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = FragmentTimeTravelBinding.inflate(inflater, container, false)
        return bind.root
    }
}