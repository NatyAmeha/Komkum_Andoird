package com.komkum.komkum.ui.library

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*

import com.komkum.komkum.databinding.LibraryFragmentBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.album.AlbumListViewModel
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.ui.setting.SettingsActivity
import com.komkum.komkum.ui.user.UserViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.adaper.UserAdapter
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.sendIntent
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.skydoves.balloon.createBalloon
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.data.model.MenuItem.Companion.musicMenuItem
import com.komkum.komkum.ui.home.BaseBottomTabFragment
import com.komkum.komkum.ui.podcast.PodcastListFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : BaseBottomTabFragment() , IRecyclerViewInteractionListener<BaseModel> {


    val libraryViewmodel : LibraryViewModel by viewModels()
    val userViewmodel : UserViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding : LibraryFragmentBinding
    lateinit var facebookcallbackManager : CallbackManager


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = LibraryFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = libraryViewmodel

        requireActivity().configureActionBar(binding.toolbar , getString(R.string.library))

        binding.download.libraryItemImageView.setImageResource(R.drawable.ic_file_download_black_24dp)
        binding.download.libraryItemTextview.text = getString(R.string.downloads)
        binding.download.root.setOnClickListener {findNavController().navigate(R.id.downloadListFragment)}

        libraryViewmodel.librarySummery.observe(viewLifecycleOwner ){
            it.data?.let {
                binding.group.visibility = View.VISIBLE
                binding.libraryLoadingProgressbar.visibility = View.GONE
                binding.libraryContainer.visibility = View.VISIBLE
                binding.textView61.visibility = View.GONE
                inflateView(it)
            }
        }


        showFbFriendsData()

//        binding.connectToFbBtn.setOnClickListener {
//            connectTofb()
//        }

//        binding.inviteFriendBtn.setOnClickListener {
//            ShareCompat.IntentBuilder.from(requireActivity())
//                .setChooserTitle("Invite Friends")
//                .setText("google play url link")
//                .setType("text/plain")
//                .startChooser()
//        }

        binding.moreFriendsBtn.setOnClickListener {
            findNavController().navigate(R.id.friendsListFragment)
        }

        libraryViewmodel.getError()
//
        libraryViewmodel.error.observe(viewLifecycleOwner){
            binding.libraryLoadingProgressbar.visibility = View.GONE
            it.handleError(requireContext()){
                binding.textView61.visibility = View.VISIBLE
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        facebookcallbackManager.onActivityResult(requestCode , resultCode , data)
    }


    fun getShareIntent() : Intent{
        return ShareCompat.IntentBuilder.from(requireActivity())
            .setChooserTitle("Invite Friends")
            .setText("google play url link")
            .setType("text/plain")
            .intent
    }



    fun inflateView(libraryData : Library?){
        binding.likedSongItem.libraryItemImageView.setImageResource(R.drawable.ic_audiotrack_black_24dp)
        binding.likedSongItem.libraryItemTextview.text = this.getString(R.string.songs)
        binding.likedSongItem.libraryItemAmountTextview.text = libraryData?.likedSong?.size.toString()
        binding.likedSongItem.root.setOnClickListener { (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_LIKED_SONG , loadFromCache = false)}


        binding.likedAlbumItem.libraryItemImageView.setImageResource(R.drawable.ic_album_black_24dp)
        binding.likedAlbumItem.libraryItemTextview.text = this.getString(R.string.albums)
        binding.likedAlbumItem.libraryItemAmountTextview.text = libraryData?.likedAlbums?.size.toString()
        binding.likedAlbumItem.root.setOnClickListener { (requireActivity() as MainActivity).moveToAlbumListFragment(getString(
                    R.string.favorite_albums) , AlbumListViewModel.LOAD_USER_FAV_ALBUM) }

        binding.playlistItem.libraryItemImageView.setImageResource(R.drawable.ic_baseline_queue_music_24)
        binding.playlistItem.libraryItemTextview.text = this.getString(R.string.playlists)
        binding.playlistItem.libraryItemAmountTextview.text =  libraryData?.likedPlaylist?.size.toString()
        binding.playlistItem.root.setOnClickListener {
            var bundle = bundleOf("DATA_TYPE" to Playlist.LOAD_USER_PLAYLIST)
            findNavController().navigate(R.id.playlistListFragment , bundle) }

        binding.likedRadio.libraryItemImageView.setImageResource(R.drawable.ic_baseline_graphic_eq_24)
        binding.likedRadio.libraryItemTextview.text = this.getString(R.string.radio_stations)
        binding.likedRadio.libraryItemAmountTextview.text = libraryData?.radioStations?.size.toString()
        binding.likedRadio.root.setOnClickListener { (requireActivity() as MainActivity).moveToRadioList("Stations" , null , isUserStation = true) }

        binding.favaoriteArtist.libraryItemImageView.setImageResource(R.drawable.ic_baseline_person_pin_24)
        binding.favaoriteArtist.libraryItemTextview.text = this.getString(R.string.artists)
        binding.favaoriteArtist.libraryItemAmountTextview.text = libraryData?.followedArtists?.size.toString()
        binding.favaoriteArtist.root.setOnClickListener { (requireActivity() as MainActivity).moveToArtistList("Favorite Artists" , Artist.LOAD_FAVORITE_ARTIST) }

        binding.favoriteAuthor.libraryItemImageView.setImageResource(R.drawable.ic_person_black_24dp)
        binding.favoriteAuthor.libraryItemTextview.text = getString(R.string.authors)
        binding.favoriteAuthor.libraryItemAmountTextview.text = libraryData?.favoriteAuthors?.size.toString()
        binding.favoriteAuthor.root.setOnClickListener { (requireActivity() as MainActivity).movetoAuthorsList(Author.FAVORITE_AUTHORS_LIST) }

        binding.favoritePodcast.libraryItemImageView.setImageResource(R.drawable.ic_baseline_mic_24)
        binding.favoritePodcast.libraryItemTextview.text = getString(R.string.your_podcasts)
        binding.favoritePodcast.libraryItemAmountTextview.text = libraryData?.likedPodcasts?.size.toString()
        binding.favoritePodcast.root.setOnClickListener {
            (requireActivity() as MainActivity).moveToPodcastList(PodcastListFragment.USER_PODCASTS)
        }

        binding.favoriteEpisode.libraryItemImageView.setImageResource(R.drawable.ic_baseline_graphic_eq_24)
        binding.favoriteEpisode.libraryItemTextview.text = getString(R.string.your_episodes)
        binding.favoriteEpisode.libraryItemAmountTextview.text = libraryData?.likedPodcastEpisods?.size.toString()
        binding.favoriteEpisode.root.setOnClickListener { findNavController().navigate(R.id.episodeListFragment)}


    }

    fun showFbFriendsData(){
        var userINteractionList =  object :IRecyclerViewInteractionListener<User>{
            override fun onItemClick(data: User, position: Int, option: Int?) {
                var bundle = bundleOf("USER_ID" to data._id , "USER_NAME" to data.username)
                findNavController().navigate(R.id.friendActivityFragment , bundle)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var pref = PreferenceHelper.getInstance(requireContext())
        var canaccessFbFriendsInfo = pref.get(AccountState.ACCESS_FACEBOOK_FRIENDS, false)
        if (canaccessFbFriendsInfo) {
            var userId = pref.get(AccountState.USER_ID, "")
            if(userId.isNotBlank()){
                userViewmodel.getUserFriendsData(userId).observe(viewLifecycleOwner , Observer{
                    if(it.isNotEmpty()){
                        var userINfo = RecyclerViewHelper(type = "USER" , interactionListener = userINteractionList , owner = viewLifecycleOwner)
                        var adapter = UserAdapter(userINfo , it)
                        binding.friendsListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                        binding.friendsListRecyclerview.adapter = adapter
                    }else{
                        binding.textView40.visibility = View.GONE
                        binding.moreFriendsBtn.visibility = View.GONE
//                        binding.inviteFriendBtn.visibility = View.GONE
                    }
                })
            }

        }
        else{
            binding.moreFriendsBtn.isVisible = false
            binding.textView40.isVisible = false
        }
    }


    fun connectTofb(){
        var permissions = listOf("user_friends")
        var loginManager = LoginManager.getInstance()
        facebookcallbackManager = CallbackManager.Factory.create()
        loginManager.registerCallback(facebookcallbackManager , object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                result?.let {
                    requestUserFriendFromFacebookAndSaveToDb(it.accessToken)
                }
            }

            override fun onCancel() {
                Toast.makeText(requireContext() , "Authentication Cancelled" , Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException) {
                Toast.makeText(requireContext() , "Authentication Error" , Toast.LENGTH_LONG).show()
            }
        })

        loginManager.logIn(this, permissions)
    }

    fun requestUserFriendFromFacebookAndSaveToDb(token : AccessToken){
        var request = GraphRequest.newMyFriendsRequest(token) { _, response ->
            var userFriendsId = mutableListOf<String>()
            var jsonResponse = response?.jsonObject?.getJSONArray("data")
            if(jsonResponse != null){
                for (i in 0 until jsonResponse.length()) {
                    var jsonObject = jsonResponse.getJSONObject(i)
                    var id = jsonObject.getString("id")
                    userFriendsId.add(id)
                }
            }

            var preference = PreferenceHelper.getInstance(requireContext())
            var loggedInUserId = preference.get(AccountState.USER_ID , "")
            Log.i("resultdatauserid", loggedInUserId)
            if(loggedInUserId.isNotBlank() && !userFriendsId.isNullOrEmpty()){
                userViewmodel.saveUserFriendsData(loggedInUserId , userFriendsId).observe(viewLifecycleOwner , Observer{
                    it?.let {
                        preference[AccountState.ACCESS_FACEBOOK_FRIENDS] = true
                        Toast.makeText(requireContext() , it.size.toString() , Toast.LENGTH_LONG).show()
                        (requireActivity() as MainActivity).showSnacbar("You can access friends activity")
                    }
                })
            }
            else{
                Toast.makeText(requireContext() , "no user friend found" , Toast.LENGTH_LONG).show()
            }

        }
        var parameter = bundleOf("fields" to "id , friends")
        request.parameters = parameter
        request.executeAsync()
    }

    override fun onItemClick(data: BaseModel, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_PLAYLIST , data.baseId , null , false)
    }

    override fun activiateMultiSelectionMode() {
    }

    override fun onSwiped(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onMoved(prevPosition: Int, newPosition: Int) {
        TODO("Not yet implemented")
    }

}
