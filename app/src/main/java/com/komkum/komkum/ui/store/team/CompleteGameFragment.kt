package com.komkum.komkum.ui.store.team

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.databinding.FragmentCompleteGameBinding
import com.komkum.komkum.util.extensions.configureActionBar


class CompleteGameFragment : Fragment() {

    lateinit var binding : FragmentCompleteGameBinding
    var result : Int? = null
    var adInfo: Ads? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            adInfo = it.getParcelable("AD_INFO")
            result = it.getInt("RESULT")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCompleteGameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")
        if(result != null && adInfo != null){
            if(adInfo!!.adType == Ads.AD_TYPE_GAME){
                binding.gameResultProgressbar.isVisible = true
                binding.gameResultProgressbar.isIndeterminate = false
                binding.gameResultProgressbar.max = adInfo?.trivias?.size ?: 0
                binding.gameResultProgressbar.progress = result ?: 0
                binding.gameResultTextview.text = "$result / ${adInfo?.trivias?.size}"
                binding.totalResultTextview.text = "You answered $result out of ${adInfo?.trivias?.size} questions"
            }
        }

        binding.goToLeaderboardBtn.setOnClickListener {
            findNavController().popBackStack(R.id.gameTeamFragment , false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
             findNavController().popBackStack(R.id.gameTeamFragment , false)
        }
    }

}