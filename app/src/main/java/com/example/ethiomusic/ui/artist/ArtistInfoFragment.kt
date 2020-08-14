package com.example.ethiomusic.ui.artist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.ArtistMetaData
import com.example.ethiomusic.databinding.FragmentArtistInfoBinding
import com.example.ethiomusic.util.viewhelper.CircleTransformation
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
            var imageUrl = it.artist.profileImagePath[0].replace("localhost" , "192.168.43.166")
            Picasso.get().load(imageUrl).placeholder(R.drawable.backimg).fit().centerCrop().transform(CircleTransformation()).into(binding.artistProfileImageview)
            binding.artistFollowerTextview.text = "${it.artist.followersCount} Followers"
            binding.totalSongTextview.text = "${it.totalSongs} Total Songs"
            binding.artistBioTextview.text = it.artist.description
            binding.monthlyStreamTextview.text = "${it.monthlyStream} Monthly Stream"
            binding.totalStreamTextview.text = "${it.totalStream} Total Stream"
            binding.monthlyDownloadTextview.text = "${it.monthlyDownload} Monthly Downloads"
            binding.totalDownloadTextview.text = "${it.totalDownload} Total Downloads"
        }
    }

}