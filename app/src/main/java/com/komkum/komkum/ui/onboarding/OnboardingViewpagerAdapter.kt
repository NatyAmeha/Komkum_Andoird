package com.komkum.komkum.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.squareup.picasso.Picasso
import com.komkum.komkum.databinding.OnboardingViewpagerListItemBinding

data class OnboardingModel(var title : String , var body : String , var image : Int)

class OnboardingViewpagerAdapter(var onboardingLIst : List<OnboardingModel>) : RecyclerView.Adapter<OnboardingViewpagerAdapter.OnboardingViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewholder {
        var binding = OnboardingViewpagerListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return OnboardingViewholder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewholder, position: Int) {
        holder.bind(onboardingLIst[position])
    }

    override fun getItemCount(): Int {
        return onboardingLIst.size
    }

    inner class OnboardingViewholder(var binding : OnboardingViewpagerListItemBinding) : RecyclerView.ViewHolder(binding.root){

        fun bind(model : OnboardingModel){
            Picasso.get().load(model.image).placeholder(model.image).fit().centerCrop().into(binding.onboardingImageview)
            binding.onboardingHeaderTextview.text = model.title
            binding.onboardingBodyTextview.text = model.body

        }
    }

}