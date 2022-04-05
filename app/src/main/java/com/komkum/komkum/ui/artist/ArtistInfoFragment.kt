package com.komkum.komkum.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.R
import com.komkum.komkum.data.model.ArtistMetaData
import com.komkum.komkum.databinding.FragmentArtistInfoBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso
import com.komkum.komkum.MainActivity
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.RoundImageTransformation

class ArtistInfoFragment : Fragment() {

    lateinit var binding : FragmentArtistInfoBinding
    var artistMetadata : ArtistMetaData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            artistMetadata = it.getParcelable<ArtistMetaData>("ARTIST_METADATA")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArtistInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        artistMetadata?.let {
            configureActionBar(binding.toolbar, it.artist.name)
            var imageUrl = it.artist.profileImagePath?.get(0)
            Picasso.get().load(imageUrl).placeholder(R.drawable.music_placeholder).fit().centerCrop().into(binding.artistProfileImageview)
            binding.artistNameTextview.text = it.artist.name
            binding.artistFollowerTextview.text = it.artist.followersCount.toString()
            binding.totalSongTextview.text = it.totalSongs.toString()
            binding.artistBioTextview.text = it.artist.description
            binding.monthlyStreamTextview.text = it.artist.monthlyStreamCount.toString()
            binding.totalStreamTextview.text = it.artist.totalStreamCount.toString()
            binding.monthlyDownloadTextview.text = it.monthlyDownload.toString()
            binding.totalDownloadTextview.text = it.totalDownload.toString()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return  when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}