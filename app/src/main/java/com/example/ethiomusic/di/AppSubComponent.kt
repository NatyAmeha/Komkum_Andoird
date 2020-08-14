package com.example.ethiomusic.di

import com.example.ethiomusic.di.auth.AuthSubComponent
import dagger.Module

@Module(subcomponents = [AuthSubComponent::class])
interface AppSubComponent {
}