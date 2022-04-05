package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.data.model.Playlist
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.databinding.PaymentMethodListItemBinding
import com.komkum.komkum.ui.album.adapter.SongAdapter
import com.squareup.picasso.Picasso

class PaymentMethodAdapter(var recyclerviewInfo : RecyclerViewHelper<PaymentMethod>, var paymentMethodList : List<PaymentMethod> ) : RecyclerView.Adapter<PaymentMethodAdapter.PaymentMethodViewholder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentMethodViewholder {
        var binding = PaymentMethodListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return  PaymentMethodViewholder(binding)
    }

    override fun onBindViewHolder(holder: PaymentMethodViewholder, position: Int) {
       holder.bind(paymentMethodList[position])
    }

    override fun getItemCount(): Int {
       return paymentMethodList.size
    }


    inner class PaymentMethodViewholder(var binding : PaymentMethodListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(paymentMethod : PaymentMethod){
            binding.paymentMethodNameTview.text = paymentMethod.name
            binding.paymentMethodDescTextview.text = paymentMethod.description
            Picasso.get().load(paymentMethod.image).placeholder(paymentMethod.image).fit().centerCrop().into(binding.paymentMethodImage)

            with(binding.root){
                setOnClickListener {
                    recyclerviewInfo.interactionListener?.onItemClick(paymentMethod , absoluteAdapterPosition , SongAdapter.NO_OPTION)
                }
            }
        }
    }
}
