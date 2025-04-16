package com.example.ebook_reader.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.ebook_reader.Enum.BookType
import com.example.ebook_reader.Enum.toInt
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Entity(tableName = "BooksInfo",
    foreignKeys = [
        ForeignKey(
            entity = FolderView::class,
            parentColumns = arrayOf("folderId"),
            childColumns = arrayOf("folderId"),
            onDelete = SET_NULL
        )
    ],
    indices = [
        Index(value = ["folderId"], unique = false)
    ]
    )
@Parcelize
data class BookView(
    @PrimaryKey(autoGenerate = true)
    val bookId: Long,
    val title: String,
    val bookType: BookType,
    val currentPage:Long,
    val totalPages:Long,
    val coverUrl: String,
    val bookUrl: String,
    val folderId: Long?
):BookAndFolderItem(), Parcelable{
    private companion object : Parceler<BookView>{
        override fun BookView.write(
            parcel: Parcel,
            flags: Int
        ) {
            parcel.writeLong(bookId)
            parcel.writeString(title)
            parcel.writeInt(bookType.toInt())
            parcel.writeLong(currentPage)
            parcel.writeLong(totalPages)
            parcel.writeString(coverUrl)
            parcel.writeString(bookUrl)
            if(folderId == null){
                parcel.writeLong(-1)
            }
            else{
                parcel.writeLong(folderId)
            }
        }

        override fun create(parcel: Parcel): BookView {
            return BookView(
                bookId = parcel.readLong(),
                title = parcel.readString().toString(),
                bookType = BookType.toType(parcel.readInt()),
                currentPage = parcel.readLong(),
                totalPages = parcel.readLong(),
                coverUrl = parcel.readString().toString(),
                bookUrl = parcel.readString().toString(),
                folderId = parcel.readLong().let {
                    if (it == -1L) null else it
                }
            )
        }

    }
}



