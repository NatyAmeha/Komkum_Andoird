package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.data.model.Radio
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.RadioViewpagerListitemBinding
import dagger.hilt.android.internal.managers.ViewComponentManager
import java.util.*

class RadioViewpagerAdapter(var info : RecyclerViewHelper<Radio> , var radioMap : Map<String , List<Radio>>) : RecyclerView.Adapter<RadioViewpagerAdapter.RadioViewpagerViewholder>() {

    lateinit var iMainActivity: IMainActivity

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        var fragmentContextWrapper : ViewComponentManager.FragmentContextWrapper = recyclerView.context as ViewComponentManager.FragmentContextWrapper
        iMainActivity = fragmentContextWrapper.fragment.requireActivity() as MainActivity
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RadioViewpagerViewholder {
        var binding = RadioViewpagerListitemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return RadioViewpagerViewholder(binding)
    }

    override fun onBindViewHolder(holder: RadioViewpagerViewholder, position: Int) {
        var key = radioMap.keys.toList()[position]
        radioMap[key]?.let {
            holder.bind(key , it)
        }
    }

    override fun getItemCount(): Int {
        return radioMap.size
    }


    inner class RadioViewpagerViewholder(var binding : RadioViewpagerListitemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(key : String , radioList : List<Radio>){
            binding.statioTitileTextview.text = "Featured ${key.toLowerCase(Locale.ROOT)} stations"
            var radioAdapter = RadioAdapter(info)
            binding.stationListRecyclerview.layoutManager = LinearLayoutManager(binding.root.context)
            binding.stationListRecyclerview.adapter = radioAdapter
            radioAdapter.submitList(radioList)

            binding.seeMoreRadioBtn.setOnClickListener {
                iMainActivity.moveToRadioList(key.toLowerCase(Locale.ROOT) , key , false)
            }
        }
    }
}