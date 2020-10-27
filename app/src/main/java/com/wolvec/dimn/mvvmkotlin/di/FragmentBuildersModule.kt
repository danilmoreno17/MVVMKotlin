package com.wolvec.dimn.mvvmkotlin.di

import com.wolvec.dimn.mvvmkotlin.ui.repo.RepoFragment
import com.wolvec.dimn.mvvmkotlin.ui.search.SearchFragment
import com.wolvec.dimn.mvvmkotlin.ui.user.UserFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {

    @ContributesAndroidInjector
    abstract fun contributeRepoFragment(): RepoFragment

    @ContributesAndroidInjector
    abstract fun contributeUserFragment(): UserFragment

    @ContributesAndroidInjector
    abstract fun contributeSearchFragment(): SearchFragment
}