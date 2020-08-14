package com.example.ethiomusic.di.auth

import com.example.ethiomusic.ControllerActivity
import com.example.ethiomusic.OnboardingActivity
import com.example.ethiomusic.di.ActivityScope
import com.example.ethiomusic.ui.account.MainAccountFragment
import com.example.ethiomusic.ui.onboarding.CategorySelectionFragment
import dagger.Subcomponent

@ActivityScope
@Subcomponent(modules = [AuthModule::class])
interface AuthSubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create() : AuthSubComponent
    }

    fun inject(activity : ControllerActivity)
    fun inject(activity : OnboardingActivity)
    fun inject(fragment : MainAccountFragment)
    fun inject(fragment : CategorySelectionFragment)
}