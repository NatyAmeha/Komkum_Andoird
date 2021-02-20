package com.zomatunes.zomatunes

import com.zomatunes.zomatunes.data.model.*

interface IMainActivity {
    fun movefrombaseFragmentToAlbumFragment(albumId : String , loadFromCache : Boolean , isNavigationFromDownload : Boolean)
    fun moveToAlbumListFragment(toolbarTitle : String, dataType : Int? = null ,  albumList : List<Album<String , String>>? = null)
    fun movetoSongListFragment(songListType : Int? , songList : List<Song<String,String>>?)

    fun moveToArtist(artistId : String)
    fun moveToArtistList(toolbarTitle: String , dataType: Int? = null , artistList : List<Artist<String , String>>? = null)
    fun movetoArtistBio(metadata : ArtistMetaData)

    fun movefromArtisttoAlbum(albumId: String)
    fun moveFromAlbumListtoAlbumFragment(albumId: String)

    fun movetoPlaylist(playlistDataType : Int, playlistId : String? = null, playlistData : Playlist<Song<String, String>>? = null , loadFromCache: Boolean , fromAddtoFragment : Boolean? = false)

    //playlist info doesn't containt full song info .. another request send to get full song response
    fun movetoPlaylistBeta(playlistDataType: Int , playlistId: String? = null , playlistInfo : Playlist<String>? = null , loadFromCache: Boolean? = false)
    fun movetoAddSongFragment(playlistId: String?)
    fun moveToAddtoFragment(data : List<BaseModel> , dataType: Int)

    fun moveToRadioList(name : String , category : String? = null , isUserStation : Boolean? = false)
    fun movetoSingleRadioStation(genre : String? = null , radioData : Radio? = null , songId : String? = null , artistId: String? = null)

    fun movetoAuthor(authorId : String)

    fun showBookList(bookList : List<Book<String>> , title : String , showFilter : Boolean = false)

}