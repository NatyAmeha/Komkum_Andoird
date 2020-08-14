package com.example.ethiomusic.di

import android.content.Context
import com.example.ethiomusic.Downloader.MediaDownloaderService
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.di.auth.AuthSubComponent
import com.example.ethiomusic.media.PlaybackService
import com.example.ethiomusic.ui.album.AlbumFragment
import com.example.ethiomusic.ui.album.AlbumListFragment
import com.example.ethiomusic.ui.artist.ArtistFragment
import com.example.ethiomusic.ui.artist.ArtistListFragment
import com.example.ethiomusic.ui.artist.ArtistsHomeFragment
import com.example.ethiomusic.ui.browse.BrowseMusicFragment
import com.example.ethiomusic.ui.download.DownloadListFragment
import com.example.ethiomusic.ui.home.AddToFragment
import com.example.ethiomusic.ui.home.HomeFragment
import com.example.ethiomusic.ui.library.LibraryFragment
import com.example.ethiomusic.ui.onboarding.ArtisSelectiontListFragment
import com.example.ethiomusic.ui.playlist.CreatePlaylistDialogFragment
import com.example.ethiomusic.ui.playlist.PlaylistFragment
import com.example.ethiomusic.ui.radio.RadioFragment
import com.example.ethiomusic.ui.radio.RadioHomeFragment
import com.example.ethiomusic.ui.search.SearchFragment
import com.example.ethiomusic.ui.song.AddSongFragment
import com.example.ethiomusic.ui.song.SongListFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class , DownloadModule::class  , PlayerModule::class , AppSubComponent::class])
interface AppComponent {

    @Component.Factory
    interface Factory{
        fun create(@BindsInstance context : Context) : AppComponent
    }

    fun inject(activity : MainActivity)
    fun inject(fragment: HomeFragment)
    fun inject(fragment: AlbumFragment)
    fun inject(fragment : LibraryFragment)
    fun inject(fragment : PlaylistFragment)
    fun inject(fragment : ArtistFragment)
    fun inject(fragment : ArtistsHomeFragment)
    fun inject(fragment : AlbumListFragment)
    fun inject(fragment : SongListFragment)
    fun inject(fragment  : CreatePlaylistDialogFragment)
    fun inject(fragment : AddToFragment)
    fun inject(fragment : ArtistListFragment)
    fun inject(fragment : ArtisSelectiontListFragment)
    fun inject(fragment : SearchFragment)
    fun inject(fragment : DownloadListFragment)
    fun inject(fragment : AddSongFragment)

    fun inject(fragment : RadioHomeFragment)
    fun inject(fragment : RadioFragment)

    fun inject(service : PlaybackService)
    fun inject(service : MediaDownloaderService)

    fun inject(fragment: BrowseMusicFragment)

    fun authSubcomponent() :  AuthSubComponent.Factory




}