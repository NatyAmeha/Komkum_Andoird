package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.komkum.komkum.R
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.OrderedItem
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.ProductListItemBinding
import com.komkum.komkum.databinding.ProductMiniListItemBinding
import com.komkum.komkum.databinding.ProductlistListItemBinding
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.math.roundToInt

class ProductAdapter(var info: RecyclerViewHelper<Product> , var productList : List<Product> , var additionalQty : Int = 0) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        const val MAIN_PRODUCT_LIST_ITEM = 0
        const val MINI_PRODUCT_LIST_ITEM = 1
        const val VERTICAL_PRODUCT_LIST_ITEM = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var mainBinding = ProductListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        var miniBinding = ProductMiniListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        var verticalBinding = ProductlistListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)

        return when(info.listItemType){
            MAIN_PRODUCT_LIST_ITEM -> MainProductViewHolder(mainBinding)
            MINI_PRODUCT_LIST_ITEM -> MiniProductViewHolder(miniBinding)
            VERTICAL_PRODUCT_LIST_ITEM -> VerticalProductViewHolder(verticalBinding)
            else -> MainProductViewHolder(mainBinding)
       }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       when(info.listItemType){
           MAIN_PRODUCT_LIST_ITEM -> (holder as MainProductViewHolder).bind(productList[position])
           MINI_PRODUCT_LIST_ITEM -> (holder as MiniProductViewHolder).bind(productList[position])
           VERTICAL_PRODUCT_LIST_ITEM -> (holder as VerticalProductViewHolder).bind(productList[position])
           else -> (holder as MainProductViewHolder).bind(productList[position])
       }
    }

    override fun getItemCount(): Int {
      return productList.size
    }

    inner class MainProductViewHolder(var binding : ProductListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(product : Product){
            Picasso.get().load(product.gallery?.first()?.path).placeholder(R.drawable.product_placeholder).fit().centerCrop().into(binding.productImageview)
            var priceSpanner = Spanner("${product.dscPrice} ${product.stdPrice}").span("${product.stdPrice}" , Spans.strikeThrough())
            if(product.refund == true) binding.returnEligiibleCardView.isVisible = true

            var activeTeams = product.teams?.filter{team ->
                if(team.expDate == null) true
                else team.expDate!! > Calendar.getInstance().time
            }
            binding.productTitleTextview.text = product.title
            binding.productPriceTextview.text = "${binding.root.context.resources.getString(R.string.birr)} ${product.dscPrice?.roundToInt()}"
//            if(!activeTeams.isNullOrEmpty()){
//                binding.activeTeamAvatarview.isVisible = true
//                binding.textView200.isVisible= true
//                binding.activeTeamAvatarview.displayAvatar(null , activeTeams.size.toString() , size = 30 , textSize = 10f)
//            }
//            else binding.teamIndicatorGroup.isVisible = false

            binding.returnEligiibleCardView.setOnClickListener {
                MaterialDialog(binding.root.context).show {
                    title(text = binding.root.context.getString(R.string.how_return_policy_works))
                    message(text = binding.root.context.getString(R.string.return_policy_description))
                    positiveButton(text = binding.root.context.getString(R.string.ok)){}
                }
            }

            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(product , absoluteAdapterPosition , -1) }
            }
        }
    }

    inner class MiniProductViewHolder(var binding : ProductMiniListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(product : Product){
            if(!product.gallery.isNullOrEmpty()){
                Picasso.get().load(product.gallery!!.first().path).placeholder(R.drawable.product_placeholder).fit().centerCrop().into(binding.prodImageview)
            }
            if(product.refund == true) binding.returnEligableTextview.isVisible = true
            binding.titleTextview.text = product.title
            binding.priceTextview.text = "${binding.root.context.resources.getString(R.string.birr)} ${product.dscPrice?.roundToInt()}"

            binding.returnEligableTextview.setOnClickListener {
                MaterialDialog(binding.root.context).show {
                    title(text = binding.root.context.getString(R.string.how_return_policy_works))
                    message(text = binding.root.context.getString(R.string.return_policy_description))
                    positiveButton(text = binding.root.context.getString(R.string.ok)){}
                }
            }

            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(product , absoluteAdapterPosition , -1) }
            }
        }
    }

    inner class VerticalProductViewHolder(var binding : ProductlistListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(product : Product){
            Picasso.get().load(product.gallery?.first()?.path).placeholder(R.drawable.product_placeholder)
                .fit().centerCrop().into(binding.pImageview)
            binding.pTitleTextview.text = product.title
            binding.pSubtitleTextview.text = "${binding.root.context.resources.getString(R.string.birr)} ${product.dscPrice?.roundToInt()}"
            if(product.refund == true) binding.returnEligableTextview.isVisible = true
            var teamListforProduct = product.teams?.filter { team ->
                if(team.expDate == null) true
                else team.expDate!! > Calendar.getInstance().time
            }
            binding.pSubtitle1Textview.text = "${binding.root.context.getString(R.string.minimum_order)}  ${product.minQty.plus(additionalQty)} ${product.unit ?: ""}"
            binding.pSubtitle1Textview.isVisible = true

            if (!teamListforProduct.isNullOrEmpty()) {
//                if(info.type == "ORDERED_PRODUCT"){
//                    binding.activeTeamAvatarview.isVisible = false
//                    binding.pSubtitle1Textview.isVisible = false
//                }
//                else{
//                    binding.activeTeamAvatarview.displayAvatar(null , teamListforProduct.size.toString(), textSize = 10f)
//                    binding.pSubtitle1Textview.text = binding.root.context.getString(R.string.teams_buying)
//                }
            }

            binding.returnEligableTextview.setOnClickListener {
                MaterialDialog(binding.root.context).show {
                    title(text = binding.root.context.getString(R.string.how_return_policy_works))
                    message(text = binding.root.context.getString(R.string.return_policy_description))
                    positiveButton(text = binding.root.context.getString(R.string.ok)){}
                }
            }

            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(product , absoluteAdapterPosition , -1) }
            }
        }
    }
}


class OrderedItemAdapter (var info: RecyclerViewHelper<OrderedItem<Product>> , var itemsList : List<OrderedItem<Product>>) : RecyclerView.Adapter<OrderedItemAdapter.OrderedItemViewholder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderedItemViewholder {
        var binding = ProductlistListItemBinding.inflate(LayoutInflater.from(parent.context) , parent , false)
        return OrderedItemViewholder(binding)
    }

    override fun onBindViewHolder(holder: OrderedItemViewholder, position: Int) {
        holder.bind(itemsList[position])
    }

    override fun getItemCount(): Int {
       return itemsList.size
    }

    inner class OrderedItemViewholder(var binding : ProductlistListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : OrderedItem<Product>){
            Picasso.get().load(item.image).placeholder(R.drawable.product_placeholder).fit().centerCrop().into(binding.pImageview)
            binding.pTitleTextview.text = item.product?.title
            binding.pSubtitleTextview.text = "${binding.root.context.resources.getString(R.string.birr)} ${item.price}"
            binding.pSubtitle1Textview.isVisible = true
            binding.pSubtitle1Textview.text = "${binding.root.context.getString(R.string.qty)} ${item.qty} ${item.product?.unit ?: ""}"
            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(item , absoluteAdapterPosition , -1) }
            }
        }
    }
}