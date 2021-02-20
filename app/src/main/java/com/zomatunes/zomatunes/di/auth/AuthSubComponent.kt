package com.zomatunes.zomatunes.di.auth

import com.zomatunes.zomatunes.ControllerActivity
import com.zomatunes.zomatunes.OnboardingActivity
import com.zomatunes.zomatunes.di.ActivityScope
import com.zomatunes.zomatunes.ui.account.MainAccountFragment
import com.zomatunes.zomatunes.ui.onboarding.CategorySelectionFragment
import dagger.Subcomponent

//@ActivityScope
//@Subcomponent(modules = [AuthModule::class])
//interface AuthSubComponent {
//
//    @Subcomponent.Factory
//    interface Factory {
//        fun create() : AuthSubComponent
//    }
//
//    fun inject(activity : ControllerActivity)
//    fun inject(activity : OnboardingActivity)
//    fun inject(fragment : MainAccountFragment)
//    fun inject(fragment : CategorySelectionFragment)
//}