package com.komkum.komkum.util.adaper

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.komkum.komkum.R
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.TeamMember
import com.komkum.komkum.databinding.TeamMemberListItemBinding

class TeamMemberAdapter(var info: RecyclerViewHelper<TeamMember>, var memberList: List<TeamMember> , var totalProductPrice : Int = 10) : RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewholder>() {

    var sortedMemberList = memberList.sortedByDescending { it.reward?.size }
        .sortedByDescending { member->
        var invitedMembersWhoPlacedOrders = memberList.filter { member.reward?.contains(it.user) == true && it.ordered == true }
        var totalCommission = ((totalProductPrice.times(5)).div(100)).times(invitedMembersWhoPlacedOrders.size)
        totalCommission
    }
    var backgroundList = listOf(R.drawable.circle , R.drawable.circle_2 , R.drawable.circle_3)

    inner class TeamMemberViewholder(var binding: TeamMemberListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(member: TeamMember){
           binding.teamMemberImageview.displayAvatar(null, member.username?.first().toString() , 17f , backgroundColor = backgroundList[absoluteAdapterPosition % backgroundList.size])
            binding.teamMemberNameTextview.text = member.username
            var rank = if(info.type == "GAME_TEAM_MEMBERS"){
                binding.teamRankTextview.isVisible = true
                binding.teamRankTextview.text = "${absoluteAdapterPosition + 4}"
//                var invitedMembersWhoPlayedGame = memberList.filter { member.reward?.contains(it.user) == true}

                "${(member.reward?.size ?: 0).plus(member.result ?: 0)} ${binding.root.context.getString(R.string.point)}"
            }
            else{
                var invitedMembersWhoPlacedOrders = memberList.filter { member.reward?.contains(it.user) == true && it.ordered == true }
                var totalCommission = ((totalProductPrice.times(5)).div(100)).times(invitedMembersWhoPlacedOrders.size)
                "$totalCommission Birr"
            }
            binding.teamMemberRewardTextview.text = rank

            with(binding.root){
                setOnClickListener {
                    if(info.type != "GAME_TEAM_MEMBERS")
                        info.interactionListener?.onItemClick(member , absoluteAdapterPosition , -1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamMemberViewholder {
        var binding = TeamMemberListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TeamMemberViewholder(binding)
    }

    override fun onBindViewHolder(holder: TeamMemberViewholder, position: Int) {
        holder.bind(sortedMemberList[position])
    }

    override fun getItemCount(): Int {
        return memberList.size
    }


}