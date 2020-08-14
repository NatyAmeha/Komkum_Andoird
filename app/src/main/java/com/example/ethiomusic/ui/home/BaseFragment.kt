package com.example.ethiomusic.ui.home

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.MainActivityViewmodel
import com.example.ethiomusic.OnboardingActivity

import com.example.ethiomusic.R
import com.example.ethiomusic.databinding.FragmentBaseBinding
import com.example.ethiomusic.ui.setting.SettingsActivity
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_base.*

/**
 * A simple [Fragment] subclass.
 */
class BaseFragment : Fragment() {
    val mainactivityViewmodel : MainActivityViewmodel by activityViewModels()

    lateinit var binding: FragmentBaseBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBaseBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var activity = this.activity as AppCompatActivity

        toControllerActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            toControllerActivity().finishAndRemoveTask()
        }

        activity.setSupportActionBar(main_header_toolbar)
        var actionbar = activity.supportActionBar
        actionbar?.title = "EthioMusic"

        var tablayout = binding.mainTablayout
        var viewpager = binding.mainViewpager
        viewpager.isNestedScrollingEnabled = true
        viewpager.adapter = HomeViewPagerAdapter(requireActivity())
        TabLayoutMediator(tablayout, viewpager) { tab, position ->
            tab.text = when (position) {
                0 -> "Home"
                1 -> "Artist"
                2 -> "Library"
                3 -> "Download"
                else -> "Home"
            }
        }.attach()



    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.logout_menu_item -> {
                this.toControllerActivity().Logout(requireContext())
                mainactivityViewmodel.disconnectToService()
                sendIntent(OnboardingActivity::class.java)
                true
            }
            R.id.main_app_bar_search ->{
                findNavController().navigate(R.id.searchFragment)
                true
            }
            R.id.setting_menu_item ->{
                sendIntent(SettingsActivity::class.java)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
