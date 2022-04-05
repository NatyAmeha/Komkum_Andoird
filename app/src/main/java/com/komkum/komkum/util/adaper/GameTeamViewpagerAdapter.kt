package com.komkum.komkum.util.adaper

import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.ui.store.team.GameTeamHomeFragment
import com.komkum.komkum.ui.store.team.GameTeamLederboardFragment

class GameTeamViewpagerAdapter(var fm: FragmentActivity, var teamData: Team<Product>) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> GameTeamHomeFragment().apply {
                arguments = bundleOf("TEAM_INFO" to teamData)
            }
            1 -> GameTeamLederboardFragment().apply {
                arguments = bundleOf("TEAM_INFO" to teamData)
            }
            else -> GameTeamHomeFragment().apply {
                arguments = bundleOf("TEAM_INFO" to teamData)
            }
        }
    }

}