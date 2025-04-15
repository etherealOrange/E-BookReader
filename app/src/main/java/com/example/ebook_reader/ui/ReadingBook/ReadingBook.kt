package com.example.ebook_reader.ui.ReadingBook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.ebook_reader.ExtendAppCompatActivity
import com.example.ebook_reader.databinding.ActivityReadingBinding
import com.example.ebook_reader.ui.ReadingBook.ReadingBookFragment

class ReadingBook : ExtendAppCompatActivity() {
    private lateinit var bind : ActivityReadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(bind.root)



    }
}