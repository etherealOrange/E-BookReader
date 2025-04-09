package com.example.ebook_reader.entities

enum class BookTypesName(internal val extension: String) {
    TXT("txt"),
    EPUB("epub"),
    PDF("pdf");

    companion object {
        fun fromExtension(extension: String): BookTypesName? {
            return BookTypesName.entries.find { it.extension == extension }
        }
        fun isInBookTypesName(extension: String): Boolean {
            return BookTypesName.entries.any { it.extension == extension }
        }
    }

}