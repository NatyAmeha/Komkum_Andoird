package com.komkum.komkum.ui.account.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.SubscriptionPlan
import com.komkum.komkum.databinding.SubscriptionListItemBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RadioSpan
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

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
            binding.subscriptionPrice.text = "${data.priceInBirr.toString()} birr or $${data.priceInDollar.toString()}"
            binding.textView26.text = "Payment deducted based on your payment choise, renewed after ${data.dayLength} days"
            var features = data.features?.joinToString("  - ")?.toLowerCase()
            var spanner = Spanner(features).span("-" , Spans.custom { RadioSpan(binding.root.context , R.drawable.tab_item_not_active , binding.root.context.resources.getInteger(R.integer.bullet_margin)) })
            binding.subscriptionFeaturesTextview.text = spanner

            with(binding.root){
                setOnClickListener {
                    interaction.onItemClick(data, adapterPosition,null)
                }
            }
        }
    }

}