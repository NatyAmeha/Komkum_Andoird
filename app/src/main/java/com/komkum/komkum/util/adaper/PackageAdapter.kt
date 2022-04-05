package com.komkum.komkum.util.adaper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.ProductPackage
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.MainPackageListItemBinding
import com.komkum.komkum.databinding.MainTeamListItemBinding
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.internal.managers.ViewComponentManager
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import kotlin.math.roundToInt

class PackageAdapter (var info: RecyclerViewHelper<ProductPackage<Product>>, var packageList: List<ProductPackage<Product>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val PACKAGE_MAIN_LIST_ITEM = 0
        const val PACKAGE_GRID_LIST_ITEM = 1
    }

    lateinit var fragmentActivity: FragmentActivity
    var timers: MutableList<Timer> = mutableListOf()


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        var fragmentContextWrapper: ViewComponentManager.FragmentContextWrapper =
            recyclerView.context as ViewComponentManager.FragmentContextWrapper
        fragmentActivity = fragmentContextWrapper.fragment.requireActivity()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var mainPacckageBinding = MainTeamListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var gridListBinding = MainPackageListItemBinding.inflate(LayoutInflater.from(parent.context) ,parent , false)
        return when(info.listItemType){
            PACKAGE_MAIN_LIST_ITEM -> PackageMainViewholder(mainPacckageBinding)
            PACKAGE_GRID_LIST_ITEM -> PackageGridListViewholder(gridListBinding)
            else -> PackageMainViewholder(mainPacckageBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(info.listItemType){
            PACKAGE_MAIN_LIST_ITEM -> (holder as PackageAdapter.PackageMainViewholder).bind(packageList[position])
            PACKAGE_GRID_LIST_ITEM -> (holder as PackageAdapter.PackageGridListViewholder).bind(packageList[position])
        }
    }

    override fun getItemCount(): Int {
        return packageList.size
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
    }

    inner class PackageMainViewholder(var binding: MainTeamListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(packageInfo: ProductPackage<Product>) {

            packageInfo.products?.filter { !it.gallery.isNullOrEmpty() }?.map { product ->
                product.gallery!!.first().path ?: R.drawable.product_placeholder.toString()
            }?.let { binding.productImage.loadImage(it) }


            binding.teamNameTextview.text = packageInfo.name

            if (!packageInfo.products.isNullOrEmpty()) {
                var stdProductPrices = packageInfo.products!!
                    .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(packageInfo.additionalQty ?: 0)) }


                var discountPRices = packageInfo.products!!
                    .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(packageInfo.additionalQty ?: 0)) }

                var discountedPercent =
                    ((1 - (discountPRices / stdProductPrices)) * 100).roundToInt()
                var discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()
                binding.teamDescTextview.text =
                    "${discountedPercent}% (${binding.root.context.getString(R.string.birr)} ${discountedAmount}) ${binding.root.context.getString(R.string.discount)}"
            }


            var teamMemberTExt = "${packageInfo.tags?.joinToString("span")}"
            var spanner = Spanner(teamMemberTExt).span("span", Spans.custom {
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
            binding.tMemberTextview.text = spanner
            binding.productNumberBtn.text = "${packageInfo.products?.size ?: 1} ${binding.root.context.getString(R.string.products)}"
            binding.remainingDateCardview.isVisible = false
            binding.packageCashOnDeliveryTextview.isVisible = true

            with(binding.root) {
                setOnClickListener {
                    info.interactionListener?.onItemClick(packageInfo, absoluteAdapterPosition, -1)
                }
            }
        }
    }

    inner class PackageGridListViewholder(var binding : MainPackageListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(packageInfo: ProductPackage<Product>) {

            packageInfo.products?.filter { !it.gallery.isNullOrEmpty() }?.map { product ->
                product.gallery!!.first().path ?: R.drawable.product_placeholder.toString()
            }?.let { binding.productImage.loadImage(it) }


            binding.teamNameTextview.text = packageInfo.name
            if (!packageInfo.products.isNullOrEmpty()) {
                var stdProductPrices = packageInfo.products!!
                    .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(packageInfo.additionalQty ?: 0)) }

                var discountPRices = packageInfo.products!!
                    .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(packageInfo.additionalQty ?: 0)) }

                var discountedPercent =
                    ((1 - (discountPRices / stdProductPrices)) * 100).roundToInt()
                var discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()
                binding.teamDescTextview.text =
                    "${discountedPercent}% (${binding.root.context.getString(R.string.birr)} $discountedAmount) ${binding.root.context.getString(R.string.discount)}"
            }

            binding.cashOnDeliveryPackageTextview.isVisible = true

            var teamMemberTExt = "${packageInfo.tags?.joinToString("span")}"
            var spanner = Spanner(teamMemberTExt).span("span", Spans.custom {
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
            binding.tMemberTextview.text = spanner
            binding.productNumberBtn.text = "${packageInfo.products?.size ?: 1} ${binding.root.context.getString(R.string.products)}"
            binding.remainingDateCardview.isVisible = false

            with(binding.root) {
                setOnClickListener {
                    info.interactionListener?.onItemClick(packageInfo, absoluteAdapterPosition, -1)
                }
            }
        }

    }
}