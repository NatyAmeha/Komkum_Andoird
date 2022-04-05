package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.AddressListItemBinding
import com.komkum.komkum.util.viewhelper.RadioSpan
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

class AddressAdapter(var info : RecyclerViewHelper<Address> , var addressList : List<Address>) : RecyclerView.Adapter<AddressAdapter.AddressViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
       var binding = AddressListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
       holder.bind(addressList[position])
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

    inner class AddressViewHolder(var binding : AddressListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(address : Address){
            var addressNamePhoneText = "${address.name} span ${address.phone}"
            var spanner = Spanner(addressNamePhoneText).span("span", Spans.custom { RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin)) })
            binding.addressNamePhoneTextview.text = spanner
            binding.addressLocationTextview.text = "${address.city} , ${address.address}"

            with(binding.root){
                setOnClickListener {
                    info.interactionListener?.onItemClick(address , absoluteAdapterPosition , -1)
                }
            }
        }
    }
}