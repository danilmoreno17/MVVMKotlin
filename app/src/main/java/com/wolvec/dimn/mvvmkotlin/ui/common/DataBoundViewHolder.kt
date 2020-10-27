package com.wolvec.dimn.mvvmkotlin.ui.common

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
//viewholder generico que funciona cn databinding
class DataBoundViewHolder<out T: ViewDataBinding> constructor(val binding: T): RecyclerView.ViewHolder(binding.root)