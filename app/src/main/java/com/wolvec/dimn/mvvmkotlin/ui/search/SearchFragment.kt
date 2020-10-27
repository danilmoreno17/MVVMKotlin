package com.wolvec.dimn.mvvmkotlin.ui.search

import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wolvec.dimn.mvvmkotlin.AppExecutors
import com.wolvec.dimn.mvvmkotlin.R
import com.wolvec.dimn.mvvmkotlin.binding.FragmentDataBindingComponent
import com.wolvec.dimn.mvvmkotlin.databinding.FragmentSearchBinding
import com.wolvec.dimn.mvvmkotlin.di.Injectable
import com.wolvec.dimn.mvvmkotlin.ui.common.RepoListAdapter
import com.wolvec.dimn.mvvmkotlin.ui.common.RetryCallback
import com.wolvec.dimn.mvvmkotlin.utils.autoCleared
import javax.inject.Inject

class SearchFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewmodelFactory: ViewModelProvider.Factory//nos va a permitir injectar el view model

    @Inject
    lateinit var appExecutors: AppExecutors

    var dataBindingComponent: DataBindingComponent = FragmentDataBindingComponent(this)//es una interfaz q se genera en tiempo de compilacion y contiene los getter para los biunding adapters de intancias utilizadas

    var binding by autoCleared<FragmentSearchBinding>()//autamaticamente nos limpia las referencias si el fragment ha sido destruido....evita el memory leak

    var adapter by autoCleared<RepoListAdapter>()//limpia las referencias para la lista de repositorios en caso de que este sea destruido

    val searchViewModel: SearchViewModel by viewModels{
        viewmodelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_search, container, false)
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_search,
            container,
            false,
            dataBindingComponent)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.setLifecycleOwner (viewLifecycleOwner)
        initRecyclerView()
        val rvAdapter = RepoListAdapter(
            dataBindingComponent = dataBindingComponent,
            appExecutors = appExecutors,
            showFullName = true
        ){
                repo->
            findNavController().navigate(
                SearchFragmentDirections.actionSearchFragmentToRepoFragment(repo.name, repo.owner.login)
            )
        }
        binding.query = searchViewModel.queryLD
        binding.repoList.adapter = rvAdapter
        adapter = rvAdapter

        initSearchInputListener()

        binding.callback = object: RetryCallback {
            override fun retry() {
                searchViewModel.refresh()
            }
        }
    }

    private fun initSearchInputListener(){
        binding.input.setOnEditorActionListener{view: View, actionId: Int, _: KeyEvent?->
            if(actionId == EditorInfo.IME_ACTION_SEARCH){
                doSearch(view)
                true
            } else {
                false
            }
        }

        binding.input.setOnKeyListener{view: View, keyCode: Int, event: KeyEvent->
            if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
                doSearch(view)
                true
            }else{
                false
            }
        }
    }

    private fun doSearch(v: View){
        val query = binding.input.text.toString()

        dismissKeyboard(v.windowToken)
        searchViewModel.setQuery(query)
    }

    private fun dismissKeyboard(windowToken: IBinder){
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun initRecyclerView(){
        binding.repoList.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                if(lastPosition == adapter.itemCount - 1){
                    searchViewModel.loadNextPage()
                }
            }
        })

        binding.searchResult = searchViewModel.result
        searchViewModel.result.observe(viewLifecycleOwner, Observer{
                result->
            adapter.submitList(result?.data)
        })

        searchViewModel.loadMoreStatus.observe(viewLifecycleOwner, Observer {
                loadinMore->
            if(loadinMore == null){
                binding.loadingMore = false
            } else {
                binding.loadingMore = loadinMore.isRunning
                val error = loadinMore.errorMessageIfNotHandled
                if(error != null){
                    Log.d("TAG1", "Error")
                }
            }
        })
    }

}