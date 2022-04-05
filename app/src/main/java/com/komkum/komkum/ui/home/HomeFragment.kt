package com.komkum.komkum.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.DialogBehavior
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.android.billingclient.api.BillingClient
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.komkum.komkum.*
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*

import com.komkum.komkum.databinding.HomeFragmentBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.album.AlbumListViewModel
import com.komkum.komkum.ui.album.adapter.AlbumViewPagerAdapter
import com.komkum.komkum.ui.book.BookViewModel
import com.komkum.komkum.ui.component.TriviaCard
import com.komkum.komkum.ui.component.donationListViewpager
import com.komkum.komkum.ui.component.imageComposable
import com.komkum.komkum.ui.component.leaderboardList
import com.komkum.komkum.ui.playlist.PlaylistFragment
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.Resource
import com.komkum.komkum.util.adaper.PlaylistAdapter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.requestAndAccessLocationBeta
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.concurrent.schedule


@ExperimentalPagerApi
@AndroidEntryPoint
class HomeFragment : Fragment() {


    lateinit var binding: HomeFragmentBinding
    lateinit var adapter: AlbumViewPagerAdapter

    val homeViewmodel: HomeViewModel by viewModels()
    val rStatemanager: RecyclerviewStateManager<Artist<String, String>> by lazy {
        RecyclerviewStateManager<Artist<String, String>>()
    }

    var showHistory = true

    var games: List<Ads>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            games = it.getParcelableArrayList("GAMES")
        }
        showHistory = PreferenceHelper.getInstance(requireContext()).getBoolean("history", true)
        binding = HomeFragmentBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.donationCoposeView.setContent {
            ZomaTunesTheme(true) {
                donationLeaderboard()
            }
        }
        games?.let {
            binding.gamelistComposeview.setContent {
                ZomaTunesTheme(true) {
                    gameListSection()
                }
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeViewmodel = homeViewmodel
        binding.showingHistory = showHistory

        var pref = PreferenceHelper.getInstance(requireContext())
        pref[AccountState.IS_REDIRECTION] = true


        homeViewmodel.homeDataBeta.observe(viewLifecycleOwner) {
            if (it.data != null) {
                var isFirstTimeForRecommendationDialog =
                    pref.get(PreferenceHelper.FIRST_TIME_FOR_RECOMMENDATION_DIALOG, true)
                if (isFirstTimeForRecommendationDialog) {
                    Timer().schedule(5000) {
                        requireActivity().runOnUiThread {
                            showImproveREcommendationDialog()
                            pref[PreferenceHelper.FIRST_TIME_FOR_RECOMMENDATION_DIALOG] = false
                        }
                    }
                }

                var newEntertainmentPreference =
                    pref.get(PreferenceHelper.NEW_ENTERTAINMENT_PREFERENCE, false)
                if (newEntertainmentPreference) {
                    pref[PreferenceHelper.NEW_ENTERTAINMENT_PREFERENCE] = false
                    findNavController().navigate(R.id.baseFragment)
                }
            }
        }



        var artistInteractionLIstener =
            object : IRecyclerViewInteractionListener<Artist<String, String>> {
                override fun onItemClick(
                    data: Artist<String, String>,
                    position: Int,
                    option: Int?
                ) {
                    (requireActivity() as MainActivity).moveToArtist(data._id)
                }

                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

        var playlistInteractionLIstener =
            object : IRecyclerViewInteractionListener<Playlist<String>> {
                override fun onItemClick(data: Playlist<String>, position: Int, option: Int?) {
                    if (data.type == "CHART") (requireActivity() as MainActivity).movetoPlaylistBeta(
                        PlaylistFragment.LOAD_PLAYLIST,
                        data._id!!,
                        null,
                        false
                    )
                    else (requireActivity() as MainActivity).movetoPlaylistBeta(
                        PlaylistFragment.LOAD_PLAYLIST_SONGS,
                        null,
                        data,
                        false
                    )
                }

                override fun activiateMultiSelectionMode() {}
                override fun onSwiped(position: Int) {}
                override fun onMoved(prevPosition: Int, newPosition: Int) {}
            }

        var radioINteractionList = object : IRecyclerViewInteractionListener<Radio> {
            override fun onItemClick(data: Radio, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoSingleRadioStation(null, data)
            }

            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }

        var artistInfo = RecyclerViewHelper(
            "ARTIST",
            rStatemanager,
            artistInteractionLIstener,
            viewLifecycleOwner
        )
        var playlistInfo = RecyclerViewHelper(
            "PLAYLIST", interactionListener = playlistInteractionLIstener,
            owner = viewLifecycleOwner, listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL
        )

        var chartInfo = RecyclerViewHelper(
            "CHART", interactionListener = playlistInteractionLIstener,
            owner = viewLifecycleOwner, listItemType = PlaylistAdapter.HORIZONTAL_LIST_ITEM,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL
        )

        var radioLIstInfo = RecyclerViewHelper(
            "RADIO", interactionListener = radioINteractionList, owner = viewLifecycleOwner,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_LINEAR_HORIZONTAL
        )
        binding.artistInfo = artistInfo
        binding.playlistInfo = playlistInfo
        binding.chartInfo = chartInfo
        binding.radioInfo = radioLIstInfo


//        binding.albumArtistNameTextview.setOnClickListener {
//            findNavController().navigate(R.id.authorFragment)
//        }

        binding.moreChartsTextview.setOnClickListener {
            findNavController().navigate(R.id.chartListFragment)
        }

        binding.seeMoreGames.setOnClickListener {
            findNavController().navigate(R.id.gameListFragment)
        }


//        binding.moreNewAlbumTextview.setOnClickListener{
//            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_NEW_SONG , null)
//        }
//        binding.moreNewAlbumTextview.setOnClickListener {
//            (requireActivity() as MainActivity).movetoSongListFragment(SongListFragment.LOAD_POPULAR_SONG , null)
//        }

        binding.moreNewAlbumTextview.setOnClickListener {
            (requireActivity() as MainActivity).moveToAlbumListFragment(
                "New Albums",
                AlbumListViewModel.LOAD_NEW_ALBUM
            )
        }


        homeViewmodel.getError().observe(viewLifecycleOwner) {}
        homeViewmodel.error.observe(viewLifecycleOwner, Observer { error ->
            binding.homeLoadingProgressbar.visibility = View.GONE
            if (error == Resource.UN_AUTHORIZED_ACCESS) {
                binding.errorView.errorTextview.text =
                    "Something went wrong. \nTry again or listen your downloads."
                binding.errorView.gotoDownloadBtn.setOnClickListener {
                    findNavController().navigate(R.id.downloadListFragment)
                }
                binding.errorView.tryagainBtn.setOnClickListener {
                    var bundle = bundleOf("SELECTED_PAGE" to 0)
                    findNavController().navigate(R.id.baseFragment , bundle)
                }
                binding.errorView.root.isVisible = true
            }

            error.handleError(requireContext()) {
                binding.errorView.root.visibility = View.VISIBLE
                binding.errorView.errorTextview.text =
                    "Something went wrong. \nTry again or listen your downloads."
                binding.errorView.gotoDownloadBtn.setOnClickListener {
                    findNavController().navigate(R.id.downloadListFragment)
                }
                binding.errorView.tryagainBtn.setOnClickListener {
                    findNavController().navigate(R.id.baseFragment)
                }
            }
        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    fun showImproveREcommendationDialog() {
        var pref = PreferenceHelper.getInstance(requireContext())
        var dialog = MaterialDialog(requireContext(), BottomSheet(LayoutMode.WRAP_CONTENT))
            .cancelOnTouchOutside(false)
            .cornerRadius(literalDp = 14f)
            .customView(R.layout.recommendation_guide_dialog)
        var customview = dialog.getCustomView()
        var improveREcommendationBtn =
            customview.findViewById<Button>(R.id.improve_recommendation_btn)
        var bannerImage = customview.findViewById<ImageView>(R.id.imageView26)
        var cancelBtn = customview.findViewById<Button>(R.id.recommendation_cancel_btn)

        Picasso.get().load(R.drawable.onboardingcover).placeholder(R.drawable.onboardingcover).fit().centerCrop().into(bannerImage)

        improveREcommendationBtn.setOnClickListener {
            dialog.dismiss()
            findNavController().navigate(R.id.recommendationFragment)
        }
        cancelBtn.setOnClickListener {
            pref[PreferenceHelper.FIRST_TIME_FOR_RECOMMENDATION_DIALOG] = false
            dialog.dismiss()
        }
        dialog.show()
    }


//    fun handleGooglePlaybillingPUrchase() {
//        bookViewmodel.purchaseState.observe(viewLifecycleOwner) {
//            it?.let { resource ->
//                when (resource) {
//                    is Resource.Success -> {
//                        Toast.makeText(requireContext(), "purchase verified", Toast.LENGTH_LONG)
//                            .show()
//                        bookViewmodel.consumePurchase(resource.data!!.purchaseToken)
//                            .observe(viewLifecycleOwner) {
//                                if (it.responseCode == BillingClient.BillingResponseCode.OK) {
//                                    (requireActivity() as MainActivity).showSnacbar(
//                                        "Download added to the task",
//                                        "view"
//                                    ) {
//                                        var bundle = bundleOf("SELECTED_PAGE" to 1)
//                                        findNavController().navigate(
//                                            R.id.downloadListFragment,
//                                            bundle
//                                        )
//                                    }
//                                } else (requireActivity() as MainActivity).showSnacbar("Unable to start download")
//                            }
//                    }
//                    is Resource.Error -> Toast.makeText(
//                        requireContext(),
//                        resource.message,
//                        Toast.LENGTH_LONG
//                    ).show()
//
//                    else -> {
//                        Toast.makeText(requireContext(), "resource type unkown", Toast.LENGTH_LONG)
//                            .show()
//                    }
//                }
//            }
//        }
//        bookViewmodel.purchaseUsingGooglePlay("sample_subscription_id", BillingClient.SkuType.SUBS)
//    }


    @ExperimentalPagerApi
    @Composable
    fun donationLeaderboard() {
        val homeREsult by homeViewmodel.homeDataBeta.observeAsState()
        homeREsult?.data?.topCreatorByDonation?.let { donationLeaderboards ->
            var sortedLeaderboard =
                donationLeaderboards.sortedByDescending { board -> board.totalAmount?.toInt() }
            donationListViewpager(donations = sortedLeaderboard){artistId ->
                (requireActivity() as MainActivity).moveToArtist(artistId)
            }
        }
    }

    @Composable
    fun gameListSection() {
        if(!games.isNullOrEmpty()){
            LazyRow(Modifier.fillMaxWidth()) {
                itemsIndexed(games!!) { key, ad ->
                    TriviaCard(Modifier.width(dimensionResource(R.dimen.game_card_width)).height(dimensionResource(R.dimen.game_card_height)), ad) {
                        (requireActivity() as MainActivity).moveToGameTeamDetails(ad._id , backstackId = R.id.baseFragment)
                    }
                }
            }
        }
        else {
            binding.seeMoreGames.isVisible = false
            binding.gameHeaderTxtview.isVisible = false
            binding.gameHeaderDescTextview.isVisible = false
        }
    }
}
