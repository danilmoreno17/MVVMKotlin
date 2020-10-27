package com.wolvec.dimn.mvvmkotlin.binding

import android.view.View
import androidx.databinding.BindingAdapter

object BindingAdapter {//permite que la vista esta visible o no
    @JvmStatic
    @BindingAdapter("visibleGone")
    fun showHide(view : View, show: Boolean){
        view.visibility = if(show) View.VISIBLE else View.GONE
    }
}