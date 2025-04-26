package com.example.ebook_reader

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.ceil

class Tools {
    companion object{
        fun toMinute(time: Long): Long{
            return (time.toDouble()/1000/60).toLong()
        }
        fun toHour(time: Long): Long{
            return (time.toDouble()/1000/60/60).toLong()
        }
        fun toSecond(time: Long): Long{
            return (time.toDouble()/1000).toLong()
        }

        /**
         * @param pas 表示前多少年 1 表示前一年:例如今年为2002年, 那么前一年从2001.1.1:00:00:00开始 到 2002.1.1:00:00:00结束
         */
        fun makeYearGap(pos:Int): Gap {
            if (pos < 0) {
                return Gap(
                    startY = LocalDate.now().withDayOfYear(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli(),
                    endY = LocalDate.now().plusYears(1L).withDayOfYear(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
            }
            return Gap(
                startY = LocalDate.now().minusYears(pos.toLong()).withDayOfYear(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli(),
                endY = LocalDate.now().minusYears(pos.toLong() - 1L).withDayOfYear(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )
        }
        fun yearOfMillis(long: Long): Int{
            return Instant.ofEpochMilli(long).atZone(ZoneId.systemDefault()).year
        }

        fun longBookTimeS(long: Long): String{
            if(long<=1000*60){
                return "共阅读:${toSecond(long)}秒"
            }
            return "共阅读:${toMinute(long)}分钟(${toHour(long)}小时)"
        }

        fun longPerPageTimeS(long: Long): String{
            return "平均每页:${toSecond(long)}秒(${toMinute(long)}分钟)"
        }
        fun bookMarkNumS(long: Long): String{
            return "创建书签:${long}个"
        }
    }
}
data class Gap(
    val startY: Long=0L,
    val endY: Long=0L
)