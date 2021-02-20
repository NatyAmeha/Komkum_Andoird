package com.zomatunes.zomatunes.util.viewhelper

sealed class PlayerState {
    class PlaylistState : PlayerState()
    class RadioState : PlayerState()
    class DownloadState : PlayerState()
    class AudiobookState : PlayerState()
    class PodcastState : PlayerState()
}