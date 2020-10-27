package com.wolvec.dimn.mvvmkotlin.ui.repo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.R
import com.wolvec.dimn.mvvmkotlin.binding.FragmentDataBindingComponent
import com.wolvec.dimn.mvvmkotlin.databinding.FragmentRepoBinding
import com.wolvec.dimn.mvvmkotlin.di.Injectable
import com.wolvec.dimn.mvvmkotlin.ui.common.RetryCallback
import com.wolvec.dimn.mvvmkotlin.utils.autoCleared
import javax.inject.Inject

class RepoFragment : Fragment(), Injectable {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val repoViewModel: RepoViewModel by viewModels {
        viewModelFactory
    }

    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)
    var binding by autoCleared<FragmentRepoBinding>()

    private val params by navArgs<RepoFragmentArgs>()
    private var adapter by autoCleared<ContributorAdapter>()

    private fun initContributorList(viewModel: RepoViewModel){
        viewModel.contributors.observe(viewLifecycleOwner, Observer {
                listResource->
            if(listResource?.data != null){
                adapter.submitList(listResource.data)
            } else {
                adapter.submitList(emptyList())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val dataBinding = DataBindingUtil.inflate<FragmentRepoBinding>(
            inflater,
            R.layout.fragment_repo,
            container,
            false
        )

        dataBinding.retryCallback = object : RetryCallback {
            override fun retry() {
                repoViewModel.retry()
            }
        }

        binding = dataBinding
        //sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(R.transition.move)

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val params = RepoFragmentArgs.fromBundle(requireArguments())
        repoViewModel.setId(params.owner, params.name)
        binding.setLifecycleOwner (viewLifecycleOwner)
        binding.repo = repoViewModel.repo

        val adapter = ContributorAdapter(dataBindingComponent, appExecutors){
                contributor ->
            findNavController().navigate(
                RepoFragmentDirections.actionRepoFragmentToUserFragment(contributor.avatarUrl, contributor.login)
            )
        }

        this.adapter = adapter
        binding.contributorList.adapter = adapter
        //postponeEnterTransition()

        /*binding.contributorList.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }*/
        initContributorList(repoViewModel)
    }

}