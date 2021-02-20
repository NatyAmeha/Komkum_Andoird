package com.zomatunes.zomatunes.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.*
import com.zomatunes.zomatunes.databinding.FragmentFriendActivityBinding
import com.zomatunes.zomatunes.ui.playlist.PlaylistFragment
import com.zomatunes.zomatunes.util.adaper.PlaylistAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendActivityFragment : Fragment() {

    lateinit var binding : FragmentFriendActivityBinding
    val userViewmodel : UserViewModel by viewModels()

    var userId : String? = null
    var userName : String? = null


    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                userId = it.getString("USER_ID")
                userName = it.getString("USER_NAME")
                userViewmodel.getUserFbFriendActivity(userId!!)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFriendActivityBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , userName)
        var playlistInteractionLIstener = object : IRecyclerViewInteractionListener<Playlist<String>> {
            override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoPlaylistBeta(PlaylistFragment.LOAD_PLAYLIST_SONGS , null , data , false)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var artistInteractionListener  = object : IRecyclerViewInteractionListener<Artist<String, String>> {
            override fun onItemClick(data: Artist<String, String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).moveToArtist(data._id)
            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var authorInteractionListener = object : IRecyclerViewInteractionListener<Author<String, String>>{
            override fun onItemClick(data: Author<String,String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoAuthor(data._id!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var bookInteraction = object : IRecyclerViewInteractionListener<Book<String>> {
            override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
                var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
                if(data.format == Book.EbookFormat) findNavController().navigate(R.id.EBookSellFragment , bundle)
                else findNavController().navigate(R.id.audiobookSellFragment , bundle)            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }

        var playlistInfo = RecyclerViewHelper("PLAYLIST" , interactionListener = playlistInteractionLIstener ,
            owner = viewLifecycleOwner , listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM , layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL )

        var artistInfo  = RecyclerViewHelper(type = "Artist" , interactionListener = artistInteractionListener)

        var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = bookInteraction , owner = viewLifecycleOwner,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)

        var authorInfo = RecyclerViewHelper<Author<String, String>>(interactionListener = authorInteractionListener , owner = viewLifecycleOwner ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.playlistInfo = playlistInfo
        binding.artistInfo = artistInfo
        binding.bookInfo = bookInfo
        binding.authorInfo = authorInfo
        binding.userViewmodel = userViewmodel

//        creatorId?.let {
//            userViewmodel.getUserPublicPlaylist(it)
//            userViewmodel.getFavoriteArtist(it)
//        }

    }

}