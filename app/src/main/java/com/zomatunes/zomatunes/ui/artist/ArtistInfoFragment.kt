package com.zomatunes.zomatunes.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.ArtistMetaData
import com.zomatunes.zomatunes.databinding.FragmentArtistInfoBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.squareup.picasso.Picasso

class ArtistInfoFragment : Fragment() {

    lateinit var binding : FragmentArtistInfoBinding
    var artistMetadata : ArtistMetaData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            var imageUrl = it.artist.profileImagePath[0].replace("localhost" , AdapterDiffUtil.URL)
            Picasso.get().load(imageUrl).placeholder(R.drawable.backimg).fit().centerCrop().transform(CircleTransformation()).into(binding.artistProfileImageview)
            binding.artistFollowerTextview.text = it.artist.followersCount.toString()
            binding.totalSongTextview.text = it.totalSongs.toString()
            binding.artistBioTextview.text = it.artist.description
            binding.monthlyStreamTextview.text = it.monthlyStream.toString()
            binding.totalStreamTextview.text = it.totalStream.toString()
            binding.monthlyDownloadTextview.text = it.monthlyDownload.toString()
            binding.totalDownloadTextview.text = it.totalDownload.toString()
        }
    }

}