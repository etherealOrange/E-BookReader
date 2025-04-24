package com.example.ebook_reader.InterfacePackage.ReadingBook

import coil3.Bitmap


interface GetPDFPage {
    fun getPage(pageIndex: Long): Bitmap?
}