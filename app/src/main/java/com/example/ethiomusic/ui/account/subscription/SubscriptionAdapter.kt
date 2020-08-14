package com.example.ethiomusic.ui.account.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.R
import com.example.ethiomusic.data.model.SubscriptionPlan
import com.example.ethiomusic.databinding.SubscriptionListItemBinding
import com.example.ethiomusic.util.adaper.AdapterDiffUtil
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener

class SubscriptionAdapter(var interaction : IRecyclerViewInteractionListener<SubscriptionPlan>) : ListAdapter<SubscriptionPlan , SubscriptionAdapter.SubscriptionViewholder>(
    AdapterDiffUtil<SubscriptionPlan>()
) {

    var gradient = listOf(
        R.drawable.subscription_gradient,
        R.drawable.gradent2,
        R.drawable.gradient3,
        R.drawable.gradient4
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewholder {
        var binding = SubscriptionListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return SubscriptionViewholder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionViewholder, position: Int) {
        holder.bind(getItem(position))
    }


    inner class SubscriptionViewholder(var binding : SubscriptionListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : SubscriptionPlan){
            binding.cardContainer.setBackgroundResource(gradient[adapterPosition % gradient.size])
            binding.subscriptionName.text = data.name
            binding.subsDaylengthTextview.text = "${data.dayLength.toString()} Days"
            binding.subscriptionPrice.text = "${data.priceInBirr.toString()} Birr or $${data.priceInDollar.toString()}"
            binding.subscriptionFeaturesTextview.text = data.features?.joinToString("  - ")?.toLowerCase()

            with(binding.root){
                setOnClickListener {
                    interaction.onItemClick(data, adapterPosition,null)
                }
            }
        }
    }

}