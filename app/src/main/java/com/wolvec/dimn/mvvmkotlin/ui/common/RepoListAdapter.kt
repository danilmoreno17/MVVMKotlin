package com.wolvec.dimn.mvvmkotlin.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.R
import com.wolvec.dimn.mvvmkotlin.databinding.RepoItemBinding
import com.wolvec.dimn.mvvmkotlin.model.Repo

class RepoListAdapter(//clase q se va implementar en el recyclerview para mostrar una lista de com.wolvec.dimn.mvvmkotlin.model.Repo
    private val dataBindingComponent: DataBindingComponent,
    appExecutors: AppExecutors,
    private val showFullName: Boolean,
    private val repoClickCallback: ((Repo)->Unit)?//cuando pulse en un repo nos diriga al mismo
) : DataBoundListAdapter<Repo, RepoItemBinding>(
    appExecutors = appExecutors,
    diffCallback = object : DiffUtil.ItemCallback<Repo>(){
        override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.owner == newItem.owner && oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
            return oldItem.description == newItem.description && oldItem.stars == newItem.stars
        }

    }
) {
    override fun createBinding(parent: ViewGroup): RepoItemBinding {
        val bindind = DataBindingUtil.inflate<RepoItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.repo_item,
            parent,
            false,
            dataBindingComponent
        )
        bindind.showFullName = showFullName
        bindind.root.setOnClickListener{
            bindind.repo?.let {
                repoClickCallback?.invoke(it)
            }
        }
        return bindind
    }

    override fun bind(binding: RepoItemBinding, item: Repo) {
        binding.repo = item
    }
}