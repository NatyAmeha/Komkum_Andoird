package com.komkum.komkum.di

import com.komkum.komkum.usecase.IPayment
import com.komkum.komkum.usecase.IWallet
import com.komkum.komkum.usecase.WalletPayment
import com.komkum.komkum.usecase.YenePayVendor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Qualifier


@InstallIn(ApplicationComponent::class)
@Module
abstract class UsecaseModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Yenepay

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Wallet

    @Binds
    abstract fun walletProvider(payment : WalletPayment) : IWallet


    @Binds
    @Yenepay
    abstract fun yenepayProvider(yenepayPayment : YenePayVendor) : IPayment
}