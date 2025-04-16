package com.example.ebook_reader.ui.ReadingBook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.ebook_reader.ExtendFragment
import com.example.ebook_reader.databinding.FragmentTxtPageBinding
import kotlin.getValue

class Page_TXT: ExtendFragment() {
    private lateinit var bind: FragmentTxtPageBinding

    private val viewModel: ViewModelOnTXT by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentTxtPageBinding.inflate(inflater)
        return bind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }
}