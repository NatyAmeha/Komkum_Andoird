package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Artist
import com.komkum.komkum.data.model.BaseModel
import com.komkum.komkum.data.model.Library
import com.komkum.komkum.databinding.MediumViewPageBinding
import com.komkum.komkum.ui.album.AlbumListViewModel
import com.komkum.komkum.ui.song.SongListFragment
import com.komkum.komkum.util.viewhelper.RoundImageTransformation
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import dagger.hilt.android.internal.managers.ViewComponentManager


class MediumViewAdapter<T : BaseModel>(var type : String ) : ListAdapter<T, MediumViewAdapter<T>.MediumViewHolder>(
    AdapterDiffUtil<T>()) {

    lateinit var iMainActivity: IMainActivity
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        var fragmentContextWrapper : ViewComponentManager.FragmentContextWrapper = recyclerView.context as ViewComponentManager.FragmentContextWrapper
        iMainActivity = fragmentContextWrapper.fragment.requireActivity() as MainActivity
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
                    Picasso.get().load(it.toInt()).placeholder(R.drawable.music_placeholder).transform(RoundImageTransformation())
                        .fit().centerCrop().into(binding.mediumThumbnailImageview)
                }
                else{
                   var image =  it.replace("localhost" , AdapterDiffUtil.URL)
                    Picasso.get().load(image).placeholder(R.drawable.album_placeholder).fit().centerCrop().into(binding.mediumThumbnailImageview)
                }
            }
            setupclickListener(data)


        }


        fun setupclickListener(data : T){
            with(binding.root){
                setOnClickListener {
                    if(type == "ALBUM") iMainActivity.movefrombaseFragmentToAlbumFragment(data.baseId!! , false , false)
                    else if(type == "LIBRARY" && data.baseType == Library.ALBUM_LIBRARY) iMainActivity.moveToAlbumListFragment(binding.root.context.getString(R.string.favorite_album) , AlbumListViewModel.LOAD_USER_FAV_ALBUM)
                    else if(type == "LIBRARY" && data.baseType == Library.SONG_LIBRARY) iMainActivity.movetoSongListFragment(SongListFragment.LOAD_LIKED_SONG , null)
                    else if(type == "LIBRARY" && data.baseType == Library.ARTIST_LIBRARY) iMainActivity.moveToArtistList("Favorite ARtists" , Artist.LOAD_FAVORITE_ARTIST)
                }
            }
        }
    }

}