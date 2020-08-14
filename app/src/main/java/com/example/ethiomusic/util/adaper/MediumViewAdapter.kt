package com.example.ethiomusic.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.IMainActivity
import com.example.ethiomusic.MainActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.BaseModel
import com.example.ethiomusic.data.model.Library
import com.example.ethiomusic.databinding.MediumViewPageBinding
import com.example.ethiomusic.ui.album.AlbumListViewModel
import com.example.ethiomusic.ui.song.SongListFragment
import com.example.ethiomusic.util.viewhelper.RoundImageTransformation
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.squareup.picasso.Picasso


class MediumViewAdapter<T : BaseModel>(var type : String ) : ListAdapter<T, MediumViewAdapter<T>.MediumViewHolder>(
    AdapterDiffUtil<T>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        iMainActivity = recyclerView.context as MainActivity
        var dat = ExoDatabaseProvider(recyclerView.context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediumViewHolder {
        var binding = MediumViewPageBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return MediumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediumViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class MediumViewHolder(var binding : MediumViewPageBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : T){
            binding.tittleTextview.text = data.baseTittle
            binding.subtittleTextview.text = data.baseSubTittle ?: ""
              data.baseImagePath?.let {
                if(type == "LIBRARY") {
                    Picasso.get().load(it.toInt()).placeholder(R.drawable.backimage).transform(RoundImageTransformation())
                        .fit().centerInside().into(binding.mediumThumbnailImageview)
                }
                else{
                   var image =  it.replace("localhost" , "192.168.43.166")
                    Picasso.get().load(image).placeholder(R.drawable.backimage).fit().centerCrop().into(binding.mediumThumbnailImageview)
                }
            }
            setupclickListener(data)


        }


        fun setupclickListener(data : T){
            with(binding.root){
                setOnClickListener {
                    if(type == "ALBUM") iMainActivity.movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false)
                    else if(type == "LIBRARY" && data.baseType == Library.ALBUM_LIBRARY) iMainActivity.moveToAlbumListFragment("Favorite Albums" , AlbumListViewModel.LOAD_USER_FAV_ALBUM)
                    else if(type == "LIBRARY" && data.baseType == Library.SONG_LIBRARY) iMainActivity.movetoSongListFragment(SongListFragment.LOAD_LIKED_SONG , null)
                    else if(type == "LIBRARY" && data.baseType == Library.ARTIST_LIBRARY) iMainActivity.moveToArtistList()
                }
            }
        }
    }

}