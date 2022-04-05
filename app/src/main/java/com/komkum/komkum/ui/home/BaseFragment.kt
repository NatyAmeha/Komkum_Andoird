package com.komkum.komkum.ui.home


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.komkum.komkum.databinding.FragmentBaseBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads


@ExperimentalPagerApi
class BaseFragment : Fragment() {
    lateinit var binding: FragmentBaseBinding
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var gamesList = MutableLiveData<List<Ads>>()
    var selectedPage : Int? = null

    init {
        arguments?.let {
            selectedPage = it.getInt("SELECTED_PAGE")
        }
        lifecycleScope.launchWhenCreated {
            gamesList.value = mainActivityViewmodel.getGameAds(5)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (requireActivity() as MainActivity).showBottomViewBeta()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBaseBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var tablayout = binding.mainTablayout
        var viewpager = binding.mainViewpager

        viewpager.isUserInputEnabled = false
        gamesList.observe(viewLifecycleOwner){
            viewpager.adapter = HomeViewPagerAdapter(requireActivity() , it)
            selectedPage?.let {
                viewpager.currentItem = it
                selectedPage = null
            }
            TabLayoutMediator(tablayout, viewpager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.music)
                    1 -> getString(R.string.podcast)
                    2 -> getString(R.string.books)
                    else -> getString(R.string.music)
                }
            }.attach()

        }

        binding.browseImageview.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.main_menu , menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.itemId){
//            R.id.setting_menu_item ->{
//                sendIntent(SettingsActivity::class.java)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}
