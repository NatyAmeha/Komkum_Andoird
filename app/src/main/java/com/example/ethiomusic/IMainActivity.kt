package com.example.ethiomusic

import com.example.ethiomusic.data.model.*

interface IMainActivity {
    fun movefrombaseFragmentToAlbumFragment(albumId : String , loadFromCache : Boolean , isNavigationFromDownload : Boolean)
    fun moveToAlbumListFragment(fragmentHeaderInfo : String, dataType : Int)
    fun movetoSongListFragment(songListType : Int? , songList : List<Song<String,String>>?)

    fun moveToArtist(artistId : String)
    fun moveToArtistList()
    fun movetoArtistBio(metadata : ArtistMetaData)

    fun movefromArtisttoAlbum(albumId: String)
    fun moveFromAlbumListtoAlbumFragment(albumId: String)

    fun movetoPlaylist(playlistDataType : Int, playlistId : String? = null, playlistData : Playlist<Song<String, String>>? = null , loadFromCache: Boolean)

    //playlist info doesn't containt full song info .. another request send to get full song response
    fun movetoPlaylistBeta(playlistDataType: Int , playlistId: String? = null , playlistInfo : Playlist<String>? = null , loadFromCache: Boolean? = false)
    fun movetoAddSongFragment(playlistId: String?)
    fun moveToAddtoFragment(data : List<BaseModel> , dataType: Int)

    fun movetoSingleRadioStation(genre : String? , radioData : Radio?)
}