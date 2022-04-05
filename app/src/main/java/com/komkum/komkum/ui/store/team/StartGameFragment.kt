package com.komkum.komkum.ui.store.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.databinding.FragmentStartGameBinding
import com.komkum.komkum.util.extensions.configureActionBar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class StartGameFragment : Fragment() {

    lateinit var binding : FragmentStartGameBinding
    var adInfo : Ads? = null
    var questionIndex : Int = 0
    var teamId : String? = null
    var totalResult : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            adInfo = it.getParcelable("AD_INFO")
            questionIndex = it.getInt("QUESTION_INDEX")
            teamId = it.getString("TEAM_ID")
            var totalResult : Int = 0
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentStartGameBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "")
        binding.startQuizBtn.setOnClickListener {
            adInfo?.let {
                if(teamId != null) (requireActivity() as MainActivity).moveToQuiz(it , questionIndex , teamId!! , totalResult)
            }
        }
        binding.quizGuideTextview.text = "${getString(R.string.quiz_game_instruction)} ${adInfo?.ownerName?.joinToString(", ")}"
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            findNavController().popBackStack(R.id.gameTeamFragment , false)
        }
    }
}