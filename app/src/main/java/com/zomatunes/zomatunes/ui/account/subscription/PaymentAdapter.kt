package com.zomatunes.zomatunes.ui.account.subscription

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.zomatunes.zomatunes.databinding.FragmentPaymentDialogListItemBinding
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener

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
                     Toast.makeText(it.context , data , Toast.LENGTH_SHORT).show()
                     interaction.onItemClick(data, adapterPosition, null)
                 }
             }
         }
    }
}