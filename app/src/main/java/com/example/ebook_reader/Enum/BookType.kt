package com.example.ebook_reader.Enum

enum class BookType {
    TXT,  PDF;
    companion object{
        fun toType(type: Int): BookType {
            return when(type){
                0 -> TXT
                2 -> PDF
                else -> TXT
            }
        }
    }
}
public fun BookType.toInt(): Int {
    return when(this){
        BookType.TXT -> 0
        BookType.PDF -> 2
    }
}