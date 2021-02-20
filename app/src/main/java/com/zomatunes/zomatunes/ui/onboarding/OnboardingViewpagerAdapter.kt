package com.zomatunes.zomatunes.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.R
import com.squareup.picasso.Picasso


data class OnboardingModel(var title : String , var body : String , var image : Int)

class OnboardingViewpagerAdapter(var onboardingLIst : List<OnboardingModel>) : RecyclerView.Adapter<OnboardingViewpagerAdapter.OnboardingViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewholder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.onboarding_viewpager_list_item , parent , false)
        return OnboardingViewholder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewholder, position: Int) {
        holder.bind(onboardingLIst[position])
    }

    override fun getItemCount(): Int {
        return onboardingLIst.size
    }

    inner class OnboardingViewholder(var view : View) : RecyclerView.ViewHolder(view){

        fun bind(model : OnboardingModel){
            var imageView = view.findViewById<ImageView>(R.id.onboarding_imageview)
            var headerTextview = view.findViewById<TextView>(R.id.onboarding_header_textview)
            var bodyTextview = view.findViewById<TextView>(R.id.onboarding_body_textview)
            Picasso.get().load(model.image).fit().centerCrop().into(imageView)
            headerTextview.text = model.title
            bodyTextview.text = model.body

        }
    }

}