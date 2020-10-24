package com.wolvec.dimn.mvvmkotlin.ui.repo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wolvec.dimn.mvvmkotlin.R
import com.wolvec.dimn.mvvmkotlin.di.Injectable

class RepoFragment : Fragment(), Injectable {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_repo, container, false)
    }

}