package com.komkum.komkum.di.auth

import com.komkum.komkum.ControllerActivity
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.di.ActivityScope
import com.komkum.komkum.ui.account.MainAccountFragment
import com.komkum.komkum.ui.onboarding.CategorySelectionFragment
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