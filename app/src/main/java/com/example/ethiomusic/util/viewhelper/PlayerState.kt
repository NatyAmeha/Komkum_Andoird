package com.example.ethiomusic.util.viewhelper

sealed class PlayerState {
    class PlaylistState : PlayerState()
    class RadioState : PlayerState()
    class DownloadState : PlayerState()
    class PodcastState : PlayerState()
}