package com.example.ebook_reader.ui.BookShelf

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.lifecycle.lifecycleScope
import com.example.ebook_reader.R
import com.example.ebook_reader.databinding.FragmentBookShelfBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookShelf : Fragment() {
    private var _binding: FragmentBookShelfBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookShelfViewModel by viewModels()

    companion object {
        fun newInstance() = BookShelf()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bookAdapter = BookAdapter()
        binding.BookRecyclerView.adapter = bookAdapter
        lifecycleScope.launch {
            viewModel.books.collectLatest { newList ->
                bookAdapter.submitList(newList)
            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}