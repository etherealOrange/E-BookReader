package com.example.ebook_reader.ui.ReadingBook

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.ebook_reader.databinding.FragmentTxtBottomDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TXTBottomDialog:BottomSheetDialogFragment() {
    private lateinit var bind: FragmentTxtBottomDialogBinding
    private val viewModel: ViewModelOnTXT by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTextSizeBTN()
        setLineHeightBTN()
        setLetterSpacingBTN()
        viewModel.config.launchLifeScopeCollectLatest {
            Log.d("TBD onViewCreated","更新了config $it")
            bind.TextSizeTV.text = it.textSize.toString()
            bind.LineHeightTV.text = it.lineSpacing.toString()
            bind.LetterSpacingTV.text = it.letterSpacing.toString()
        }





    }

    //设置设置变大变小的基础方法
    private fun minusOrPlus(v: View ,event: MotionEvent,minusOrPlus: () -> Unit): Boolean {
        val name = (v as? TextView)?.text?.toString()?:"无文本"
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("TBD minusOrPlus", "minusOrPlus $name \npressed")
                viewModel.keepDoThing(minusOrPlus)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                Log.d("TBD minusOrPlus", "minusOrPlus $name \nreleased or cancelled")
                viewModel.cancelJobAndDo(minusOrPlus)
                return false
            }
            else -> return false
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setTextSizeBTN() {
        //字体减小
        bind.minusTextSizeBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::minusTextSize)
        }
        //字体增大
        bind.plusTextSizeBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::plusTextSize)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setLetterSpacingBTN(){
        //字间距减小
        bind.minusLetterSpacingBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::minusLetterSpacing)
        }
        //字体增大
        bind.plusLetterSpacingBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::plusLetterSpacing)
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setLineHeightBTN(){
        //行间距减小
        bind.minusLineHeightBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::minusLineSpacing)
        }
        //行间距增大
        bind.plusLineHeightBtn.setOnTouchListener { v, e ->
            minusOrPlus(v, e, viewModel::plusLineSpacing)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind = FragmentTxtBottomDialogBinding.inflate(inflater, container, false)
        return bind.root
    }
    fun <T> Flow<T>.launchLifeScopeCollectLatest (doCollect: suspend (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectLatest {
                    doCollect(it)
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("TBD","onStart 开始")
    }

    override fun onStop() {
        super.onStop()
        Log.d("TBD","onStop 停止")
    }

    override fun onResume() {
        super.onResume()
        Log.d("TBD","onResume 恢复")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TBD","onResume 暂停")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("TBD","onDestroy 销毁")
    }

}