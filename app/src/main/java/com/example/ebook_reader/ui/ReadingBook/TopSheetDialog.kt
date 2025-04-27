package com.example.ebook_reader.ui.ReadingBook

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import androidx.core.view.GravityCompat
import com.example.ebook_reader.R
import com.example.ebook_reader.Tools
import com.example.ebook_reader.databinding.TopSlideDialogBinding
import com.example.ebook_reader.ui.TimeRecorder.NotifyContent

class TopSheetDialog(context: Context,val notify: NotifyContent?):Dialog(context) {
    private val bind: TopSlideDialogBinding by lazy {
        TopSlideDialogBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setGravity(Gravity.TOP)
            requestFeature(Window.FEATURE_NO_TITLE)
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setCanceledOnTouchOutside(false)
            setWindowAnimations(R.style.TopSheetAnimation)
        }

        setContentView(bind.root)
        if(notify != null){
            val s = if(notify.time<=1000*60){
                Tools.toSecond(notify.time).toString()+"秒"
            }else{
               Tools.toMinute(notify.time).toString()+"分钟"
            }
            bind.LongTime.text = s
            val p = "平均每页:"+Tools.toSecond(notify.perPageTime).toString()+"秒"
            bind.perPageTime.text = p
        }
        bind.root.setOnClickListener {
            dismiss()
        }

    }


}