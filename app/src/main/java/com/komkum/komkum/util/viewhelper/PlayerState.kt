package com.komkum.komkum.util.viewhelper

sealed class PlayerState {
    class PlaylistState : PlayerState()
    class RadioState : PlayerState()
    class DownloadState : PlayerState()
    class AudiobookState : PlayerState()
    class PodcastState : PlayerState()

    companion object{
        const val PLAYLIST = 1
        const val AUDIOBOOK = 2
        const val RADIO = 3
        const val PODCAST_EPISODE = 4
    }
}