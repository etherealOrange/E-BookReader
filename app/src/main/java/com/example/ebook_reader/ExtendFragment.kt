package com.example.ebook_reader

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class ExtendFragment: Fragment() {
    fun <T>  Flow<T>.launchLifeScopeCollect (doCollect: suspend (T) -> Unit){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect {  //这是扩展函数, 所以直接使用其中的方法
                    doCollect(it)
                }
            }
        }
    }
    fun <T> Flow<T>.launchLifeScopeCollectLatest (doCollect: suspend (T) -> Unit) {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectLatest {
                    doCollect(it)
                }
            }
        }
    }
}