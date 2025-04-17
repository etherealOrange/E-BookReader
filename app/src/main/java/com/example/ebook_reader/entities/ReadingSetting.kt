package com.example.ebook_reader.entities

/**
 * @param textSize 文字大小 范围 20sp - 35sp
 * @param letterSpacing 字间距 范围 0 - 30 应该转为 0.00f - 0.30f
 * @param lineSpacing 行间距 范围 0sp - 30sp
 */
data class ReadingSetting(
    var textSize: Int = 20,
    var letterSpacing: Int = 0,
    var lineSpacing: Int = 0,
)
