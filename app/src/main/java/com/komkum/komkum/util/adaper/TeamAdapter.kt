package com.komkum.komkum.util.adaper

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.balloon.showAlignTop
import com.squareup.picasso.Picasso
import com.komkum.komkum.IMainActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.databinding.MainTeamListItemBinding
import com.komkum.komkum.databinding.TeamListItemBinding
import com.komkum.komkum.databinding.TeamMiniListItemBinding
import com.komkum.komkum.util.extensions.convertLocationToAddress
import com.komkum.komkum.util.extensions.getBalloon
import com.komkum.komkum.util.extensions.getLocation
import com.komkum.komkum.util.extensions.getRemainingDay
import com.komkum.komkum.util.isVisible
import com.komkum.komkum.util.loadImage
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.internal.managers.ViewComponentManager
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt

class TeamAdapter<P>(var info: RecyclerViewHelper<Team<P>>, var teamList: List<Team<P>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
     companion object{
         const val TEAM_WITH_PRODUCT = 0
         const val MINI_TEAM_LIST_ITEM = 1
         const val MAIN_TEAM_LIST_ITEM = 2
     }

    lateinit var fragmentActivity: FragmentActivity
    var timers : MutableList<Timer> = mutableListOf()


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        var fragmentContextWrapper : ViewComponentManager.FragmentContextWrapper = recyclerView.context as ViewComponentManager.FragmentContextWrapper
        fragmentActivity = fragmentContextWrapper.fragment.requireActivity()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       var teamWithProductBinding = TeamListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var miniTeamBinding = TeamMiniListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var mainTeamBinding = MainTeamListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when(info.listItemType){
            TEAM_WITH_PRODUCT -> TeamWithProductViewholder(teamWithProductBinding)
            MAIN_TEAM_LIST_ITEM -> MainTeamViewholder(mainTeamBinding)
            else -> MiniTeamViewHolder(miniTeamBinding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(info.listItemType){
            TEAM_WITH_PRODUCT -> (holder as TeamAdapter<*>.TeamWithProductViewholder).bind(teamList[position] as Team<Product>)
            MAIN_TEAM_LIST_ITEM -> (holder as TeamAdapter<*>.MainTeamViewholder).bind(teamList[position] as Team<Product>)
            else -> (holder as TeamAdapter<*>.MiniTeamViewHolder).bind(teamList[position] as Team<Product>)
        }
    }

    override fun getItemCount(): Int {
        return teamList.size
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        timers.forEach { timer -> timer.cancel() }
        super.onViewDetachedFromWindow(holder)
    }

    inner class TeamWithProductViewholder(var binding: TeamListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(team: Team<Product>){

            team.products?.filter { !it.gallery.isNullOrEmpty() }?.map { product ->
                product.gallery!!.first().path ?: R.drawable.product_placeholder.toString()
            }?.let {binding.productImage.loadImage(it) }



            binding.teamNameTextview.text = team.name
            if(!team.products.isNullOrEmpty()){


                var stdProductPrices = team.products!!
                    .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(team.additionalQty ?: 0)) }


                var discountPRices = team.products!!
                    .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(team.additionalQty ?: 0)) }


                var discountedPercent = ((1 - (discountPRices / stdProductPrices))*100).roundToInt()
                var discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()
                binding.teamDescTextview.text = "${discountedPercent}% (${binding.root.context.getString(R.string.birr)} ${discountedAmount}) ${binding.root.context.getString(R.string.discount)}"
            }

            var remainingmember = if(team.members?.size ?: 0 >= team.teamSize!!){
                if(team.type == Team.TRIVIA_TEAM) "Play now"
                else binding.root.context.getString(R.string.order_now)
            }
            else "${team.teamSize?.minus(team.members?.size ?: 0)} ${binding.root.context.getString(R.string.remaining)}"

            var teamMemberTExt = "${team.members?.size} ${binding.root.context.getString(R.string.joined)} span $remainingmember"
            var spanner = Spanner(teamMemberTExt).span("span", Spans.custom {
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
            binding.tMemberTextview.text = spanner

            if(team.type == Team.TRIVIA_TEAM){
                binding.productNumberTextview.isVisible = false
                binding.productImage.loadImage(emptyList() , placeholder = R.drawable.gameimage)
            }

            else if(team.type == Team.MULTI_PRODUCT_TEAM && !team.active){
                binding.teamContainer.setBackgroundColor(binding.root.context.resources.getColor(R.color.red))
                binding.tMemberTextview.text = "Not activated"
                binding.tMemberTextview.setTextColor(binding.root.context.resources.getColor(R.color.primaryDarkColor))
            }

            else{
                binding.productNumberTextview.displayAvatar(null , "${team.products?.size ?: 1}" , 10f)
            }

            team.endDate?.let {date ->
                var calendar = Calendar.getInstance()
                if(date > calendar.time)
                    calendar.time = date
                else
                    calendar.add(Calendar.DAY_OF_MONTH , team.duration ?: 3)

               var timer =  fixedRateTimer("timer" , false , period = 1000){
                    fragmentActivity.runOnUiThread {
                        binding.timeRemainingTextview.text = calendar.time.getRemainingDay(binding.root.context , includeSec = true)
                    }
                }
                timers.add(timer)
            }


//             binding.teamDescTextview.text = address.address?.split(",")?.get(1) ?: address.address


            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(team as Team<P> , absoluteAdapterPosition , -1) }
            }
        }
    }



    inner class MainTeamViewholder(var binding : MainTeamListItemBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(team: Team<Product>){
            team.products?.filter { !it.gallery.isNullOrEmpty() }?.map { product ->
                product.gallery!!.first().path ?: R.drawable.product_placeholder.toString()
            }?.let {binding.productImage.loadImage(it) }



            binding.teamNameTextview.text = team.name
            if(!team.products.isNullOrEmpty()){
                var stdProductPrices = team.products!!
                    .sumOf { product -> product.stdPrice!!.times(product.minQty.plus(team.additionalQty ?: 0)) }

                var discountPRices = team.products!!
                    .sumOf { product -> product.dscPrice!!.times(product.minQty.plus(team.additionalQty ?: 0)) }

                var discountedPercent = ((1 - (discountPRices / stdProductPrices))*100).roundToInt()
                var discountedAmount = stdProductPrices.roundToInt() - discountPRices.roundToInt()
                binding.teamDescTextview.text = "${discountedPercent}% (${binding.root.context.getString(R.string.birr)} ${discountedAmount}) ${binding.root.context.getString(R.string.discount)}"
            }

            var remainingmember = if(team.members?.size ?: 0 >= team.teamSize!!){
                if(team.type == Team.TRIVIA_TEAM) "Play now"
                else "Order now"
            }
            else "${team.teamSize?.minus(team.members?.size ?: 0)} Remaining"

            var teamMemberTExt = "${team.members?.size} joined span $remainingmember"
            var spanner = Spanner(teamMemberTExt).span("span", Spans.custom {
                RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(R.integer.bullet_margin))
            })
            binding.tMemberTextview.text = spanner
            binding.tMemberTextview.setTextColor(binding.root.context.resources.getColor(R.color.primaryColor))

            if(team.type == Team.TRIVIA_TEAM){
                binding.productImage.loadImage(emptyList() , placeholder = R.drawable.gameimage)
            }

            else if(team.type == Team.MULTI_PRODUCT_TEAM && !team.active){
                binding.teamContainer.setBackgroundColor(binding.root.context.resources.getColor(R.color.red))
                binding.tMemberTextview.text = "Not activated"
                binding.tMemberTextview.setTextColor(binding.root.context.resources.getColor(R.color.primaryDarkColor))
            }

            else{
                binding.productNumberBtn.text = "${team.products?.size ?: 1} products"
            }

            team.endDate?.let {date ->
                var calendar = Calendar.getInstance()
                calendar.time = date
                var timer =  fixedRateTimer("timer" , false , period = 1000){
                    fragmentActivity.runOnUiThread {
                        binding.timeRemainingTextview.text = calendar.time.getRemainingDay(binding.root.context , includeSec = true)
                    }
                }
                timers.add(timer)
            }


//             binding.teamDescTextview.text = address.address?.split(",")?.get(1) ?: address.address


            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(team as Team<P> , absoluteAdapterPosition , -1) }
            }
        }
    }



    inner class MiniTeamViewHolder(var binding: TeamMiniListItemBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(team: Team<Product>){
            var images = team.products?.map { team ->
                if(!team.gallery.isNullOrEmpty()) team.gallery!!.first().path ?: R.drawable.product_placeholder.toString()
                else ""
            }
            binding.teamTitleTextview.text = team.name
            binding.teamProductImageview.loadImage(images!!)
            if(!team.products.isNullOrEmpty()){
                binding.productNumberAvatarview.displayAvatar(null, team.products!!.size.toString()  , 11f)
            }

            var remainingmember = if(team.members?.size ?: 0 >= team.teamSize!!) "Order now"
            else "${team.teamSize?.minus(team.members?.size ?: 0)} remaining"
            var teamMemberTExt = "${team.members?.size} joined span $remainingmember"
            var spanner = Spanner(teamMemberTExt).span("span", Spans.custom { RadioSpan(binding.root.context, R.drawable.tab_item_not_active, binding.root.context.resources.getInteger(
                        R.integer.bullet_margin
                    )) })
            binding.teamSubtitleTextview.text = spanner


            team.endDate?.let {date ->
                var calendar = Calendar.getInstance()
                if(date > calendar.time) calendar.time = date
                else calendar.add(Calendar.DAY_OF_MONTH , team.duration ?: 3)
                var timer =  fixedRateTimer("timer" , false , period = 1000){
                    fragmentActivity.runOnUiThread {
                        binding.teamSubtitle1Textview.text = "${binding.root.context.getString(R.string.expire_in)} ${calendar.time.getRemainingDay(binding.root.context , includeSec = true)}"
                    }
                }
                timers.add(timer)

            }

            with(binding.root){
                setOnClickListener { info.interactionListener?.onItemClick(team as Team<P> , absoluteAdapterPosition , -1) }
            }
        }
    }
}