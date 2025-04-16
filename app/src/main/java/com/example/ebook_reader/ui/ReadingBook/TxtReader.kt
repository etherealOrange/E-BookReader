package com.example.ebook_reader.ui.ReadingBook

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.lang.Exception

class TxtReader(
    private val file: File
)
{
    private var line = String()
    private var currentIndex: Long = 0
    private val builder = StringBuilder()
    private var reader: BufferedReader? = null
    init {
        if(file.isFile){
            reader = file.bufferedReader()
        }
    }

    fun readNextLines(len: Long): String? {
        if(reader==null) return ""
        builder.clear()
        for(i in 0 until len){
            reader!!.readLine()?.also { line=it } ?:throw Exception("读取 到$currentIndex 遇到EOF")
            line = "    "+line.trim()+"\n"
            if(i/100>0) {
                Log.d("TR readNextLines", "读取到第 $currentIndex 行: $line")
            }
            builder.append(line)
            currentIndex++
        }
        return builder.toString()
    }

    fun getCurrentIndex(): Long {
        return currentIndex
    }

    fun close() {
        if(reader==null) return
        reader!!.close()
    }

}