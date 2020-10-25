package com.wolvec.dimn.mvvmkotlin.binding

import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingComponent
//primer escribe esta linea de codigo en el gradle.properties (android.databinding.enableV2=true)

class FragmentDataBindingComponent(fragment: Fragment): DataBindingComponent {

    private val adapter = FragmentBindingAdapters(fragment)

    override fun getFragmentBindingAdapters(): FragmentBindingAdapters = adapter
}
