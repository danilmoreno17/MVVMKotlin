package com.wolvec.dimn.mvvmkotlin.ui.common

import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.wolvec.dimn.mvvmkotlin.AppExecutors

abstract class DataBoundListAdapter<T,V: ViewDataBinding>(//adapter para nuestro recyclerview cn databinding
    appExecutors: AppExecutors,
    diffCallback: DiffUtil.ItemCallback<T>): ListAdapter<T, DataBoundViewHolder<V>>(// DiffUtil.ItemCallback=compara dos listas y si son diferente mostrar la ultima
    AsyncDifferConfig.Builder<T>(diffCallback)
        .setBackgroundThreadExecutor(appExecutors.diskIO())
        .build()
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBoundViewHolder<V> {
        val binding = createBinding(parent)
        return DataBoundViewHolder(binding)
    }

    protected abstract fun createBinding(parent: ViewGroup): V

    override fun onBindViewHolder(holder: DataBoundViewHolder<V>, position: Int) {
        bind(holder.binding, getItem(position))
        holder.binding.executePendingBindings()
    }

    protected abstract fun bind(binding: V, item: T)
}