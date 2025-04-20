package com.example.ebook_reader.ui

import android.content.Context
import android.content.DialogInterface
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DialogBuilderFactory {
    companion object{
        /**
         * MaterialAlertDialogBuilder 对话框创建工厂
         */
        fun build(
            context: Context,
            title: String,
            view: View,
            positiveButtonClickListener: (DialogInterface, Int) -> Unit,
            negativeButtonClickListener: (DialogInterface, Int) -> Unit,
            positiveButtonText: String = "确定",
            negativeButtonText: String = "取消",
        ): AlertDialog {
            return MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton(positiveButtonText, positiveButtonClickListener)
                .setNegativeButton(negativeButtonText, negativeButtonClickListener)
                .create()
        }
        /**
         * MaterialAlertDialogBuilder 对话框创建工厂
         */
        fun build(
            context: Context,
            title: String,
            message: String,
            positiveButtonClickListener: (DialogInterface, Int) -> Unit,
            negativeButtonClickListener: (DialogInterface, Int) -> Unit,
            positiveButtonText: String = "确定",
            negativeButtonText: String = "取消",
        ): AlertDialog {
            return MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, positiveButtonClickListener)
                .setNegativeButton(negativeButtonText, negativeButtonClickListener)
                .create()
        }
    }
}