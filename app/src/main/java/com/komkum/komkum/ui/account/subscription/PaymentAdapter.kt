package com.komkum.komkum.ui.account.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.databinding.FragmentPaymentDialogListItemBinding
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener

class PaymentAdapter (val paymentMethods: List<String> , var interaction : IRecyclerViewInteractionListener<String>)
    : RecyclerView.Adapter<PaymentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentAdapter.ViewHolder {
        var binding = FragmentPaymentDialogListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentAdapter.ViewHolder, position: Int) {
       holder.bind(paymentMethods[position])
    }

    override fun getItemCount(): Int {
        return paymentMethods.size
    }

     inner class ViewHolder (var binding : FragmentPaymentDialogListItemBinding) : RecyclerView.ViewHolder(binding.root) {
//        internal val text: TextView = itemView.text

         fun bind(data : String){
             binding.paymentMethodNameTextview.text = data
             with(binding.root){
                 setOnClickListener {
                     interaction.onItemClick(data, adapterPosition, null)
                 }
             }
         }
    }
}