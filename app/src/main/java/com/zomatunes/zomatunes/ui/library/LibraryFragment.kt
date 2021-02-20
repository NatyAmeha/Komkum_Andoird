package com.zomatunes.zomatunes.ui.library

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*

import com.zomatunes.zomatunes.databinding.LibraryFragmentBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.ui.album.AlbumListViewModel
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.ui.setting.SettingsActivity
import com.zomatunes.zomatunes.ui.user.UserViewModel
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.PreferenceHelper.set
import com.zomatunes.zomatunes.util.adaper.FriendsAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.extensions.sendIntent
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : Fragment() , IRecyclerViewInteractionListener<BaseModel> {


    val libraryViewmodel : LibraryViewModel by viewModels()
    val userViewmodel : UserViewModel by viewModels()

    lateinit var binding : LibraryFragmentBinding
    lateinit var facebookcallbackManager : CallbackManager


    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LibraryFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = libraryViewmodel

        requireActivity().configureActionBar(binding.toolbar , "Library")

        showUserInfo(requireContext())

        (binding.download.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_file_download_black_24dp)
        (binding.download.findViewById(R.id.library_item_textview) as TextView).text = "Downloads"
        binding.download.setOnClickListener {findNavController().navigate(R.id.downloadListFragment) }

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

        binding.inviteFriendBtn.setOnClickListener {
            ShareCompat.IntentBuilder.from(requireActivity())
                .setChooserTitle("Invite Friends")
                .setText("google play url link")
                .setType("text/plain")
                .startChooser()
        }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.setting_menu_item ->{
                sendIntent(SettingsActivity::class.java)
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

    fun showUserInfo(context: Context) {
        var preference = PreferenceHelper.getInstance(context)
        var username = preference.getString(AccountState.USERNAME, "")
        var email = preference.getString(AccountState.EMAIL, "")
        var profilePicture = preference.getString(AccountState.PROFILE_IMAGE, "")
        binding.usernameTextview.text = username
        if (!profilePicture.isNullOrBlank()) Picasso.get().load(profilePicture)
            .placeholder(R.drawable.circularimg).transform(CircleTransformation())
            .into(binding.profileImageview)
    }


    fun inflateView(libraryData : Library?){

        binding.accountCardview.setOnClickListener {
            findNavController().navigate(R.id.userFragment)
        }

        (binding.likedSongItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_audiotrack_black_24dp)
        (binding.likedSongItem.findViewById(R.id.library_item_textview) as TextView).text = "Songs"
        (binding.likedSongItem.findViewById(R.id.library_item_amount_textview) as TextView).text = libraryData?.likedSong?.size.toString()
        binding.likedSongItem.setOnClickListener { (requireActivity() as MainActivity).movetoPlaylist(PlaylistFragment.LOAD_LIKED_SONG , loadFromCache = false)}


        (binding.likedAlbumItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_album_black_24dp)
        (binding.likedAlbumItem.findViewById(R.id.library_item_textview) as TextView).text = "Albums"
        (binding.likedAlbumItem.findViewById(R.id.library_item_amount_textview) as TextView).text = libraryData?.likedAlbums?.size.toString()
        binding.likedAlbumItem.setOnClickListener { (requireActivity() as MainActivity).moveToAlbumListFragment("Favorite Albums" , AlbumListViewModel.LOAD_USER_FAV_ALBUM) }

        (binding.playlistItem.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_queue_music_24)
        (binding.playlistItem.findViewById(R.id.library_item_textview) as TextView).text = "Playlists"
        (binding.playlistItem.findViewById(R.id.library_item_amount_textview) as TextView).text =  libraryData?.likedPlaylist?.size.toString()
        binding.playlistItem.setOnClickListener {
            var bundle = bundleOf("DATA_TYPE" to Playlist.LOAD_USER_PLAYLIST)
            findNavController().navigate(R.id.playlistListFragment , bundle) }

        (binding.likedRadio.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_graphic_eq_24)
        (binding.likedRadio.findViewById(R.id.library_item_textview) as TextView).text = "Radio Stations"
        (binding.likedRadio.findViewById(R.id.library_item_amount_textview) as TextView).text = libraryData?.radioStations?.size.toString()
        binding.likedRadio.setOnClickListener { (requireActivity() as MainActivity).moveToRadioList("Stations" , null , isUserStation = true) }

        (binding.favaoriteArtist.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_baseline_person_pin_24)
        (binding.favaoriteArtist.findViewById(R.id.library_item_textview) as TextView).text = "Artists"
        (binding.favaoriteArtist.findViewById(R.id.library_item_amount_textview) as TextView).text = libraryData?.followedArtists?.size.toString()
        binding.favaoriteArtist.setOnClickListener { (requireActivity() as MainActivity).moveToArtistList("Favorite Artists" , Artist.LOAD_FAVORITE_ARTIST) }

        (binding.favoriteAuthor.findViewById(R.id.library_item_image_view) as ImageView).setImageResource(R.drawable.ic_person_black_24dp)
        (binding.favoriteAuthor.findViewById(R.id.library_item_textview) as TextView).text = "Authors"
        (binding.favoriteAuthor.findViewById(R.id.library_item_amount_textview) as TextView).text = libraryData?.favoriteAuthors?.size.toString()

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
                        var adapter = FriendsAdapter(userINfo , it)
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
            binding.textView37.isVisible = true
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
            override fun onSuccess(result: LoginResult?) {
                result?.let {
                    requestUserFriendFromFacebookAndSaveToDb(it.accessToken)
                }
            }

            override fun onCancel() {
                Toast.makeText(requireContext() , "Authentication Cancelled" , Toast.LENGTH_LONG).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(requireContext() , "Authentication Error" , Toast.LENGTH_LONG).show()
            }
        })

        loginManager.logIn(this, permissions)
    }

    fun requestUserFriendFromFacebookAndSaveToDb(token : AccessToken){
        var request = GraphRequest.newMyFriendsRequest(token) { _, response ->
            var userFriendsId = mutableListOf<String>()
            var jsonResponse = response.jsonObject.getJSONArray("data")
            for (i in 0 until jsonResponse.length()) {
                var jsonObject = jsonResponse.getJSONObject(i)
                Log.i("resultdata", jsonObject.toString())
                var id = jsonObject.getString("id")
                Log.i("resultdataid", id)
                userFriendsId.add(id)
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
