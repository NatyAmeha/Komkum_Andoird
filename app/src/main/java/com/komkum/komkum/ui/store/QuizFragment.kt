package com.komkum.komkum.ui.store

import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.data.model.Trivia
import com.komkum.komkum.databinding.FragmentQuizBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.store.team.TeamViewModel
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.delete
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.showDialog
import dagger.hilt.android.AndroidEntryPoint

@ExperimentalPagerApi
@AndroidEntryPoint
class QuizFragment : Fragment() {

    lateinit var binding: FragmentQuizBinding
    lateinit var timer : CountDownTimer
    val mainActivityViewmodel: MainActivityViewmodel by activityViewModels()
    val teamViewModel : TeamViewModel by viewModels()

    lateinit var gameIdentifier : String

    var adInfo: Ads? = null
    var selectedQuesIndex: Int = 0
    var selectedQuestion : Trivia? = null
    var teamId : String? = null
    var isUserPlayedTheGame : Boolean? = null
    var totalResult : Int = 0
    var totalQuestions : Int = 0

    var showAsMainError = true

    var isInitialLaunch = MutableLiveData(true)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            adInfo = it.getParcelable("AD_INFO")
            selectedQuesIndex = it.getInt("QUESTION_INDEX")
            teamId = it.getString("TEAM_ID")
            isUserPlayedTheGame = it.getBoolean("IS_USER_PLAYED")
            totalResult = it.getInt("RESULT")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentQuizBinding.inflate(inflater)
        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> findNavController().popBackStack(R.id.gameTeamFragment , false)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        teamId?.let { gameIdentifier = it+userId }

        binding.composeView.setContent {
            ZomaTunesTheme(true) {
                adInfo?.let {
                    triviaQuestionsSection(it._id, selectedQuesIndex , pref , gameIdentifier)
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(teamId !=null && selectedQuesIndex < totalQuestions -1){
                gameIdentifier = teamId!!+userId
                requireActivity().showDialog(getString(R.string.exit) , getString(R.string.exit_game_msg) ,
                    showNegative = true, positiveButtonText = getString(R.string.exit) , autoDismiss = false){
                    if(::timer.isInitialized) timer.cancel()
                    findNavController().popBackStack(R.id.gameTeamFragment , false)
//                    teamId?.let {
//                        selectedQuestion?.let { sq ->
//                            pref[gameIdentifier] = selectedQuesIndex + 1
//                            saveTriviaAnswer(it , sq._id!! , -1){
//                                mainActivityViewmodel.remainingTime.value = null
//
//                            }
//                        }
//                    }
                }
            }
            else findNavController().popBackStack(R.id.gameTeamFragment , false)
        }


        teamViewModel.getError().observe(viewLifecycleOwner){}
        teamViewModel.error.observe(viewLifecycleOwner){
            binding.quizProgressbar.isVisible = false

            Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            if(!showAsMainError){
                binding.errorView.gotoDownloadBtn.isVisible = false
                binding.errorView.errorImageview.visibility = View.INVISIBLE
                binding.errorView.errorTextview.text =
                    "Something went wrong.\n Please try again"
                binding.errorView.tryagainBtn.setOnClickListener {
                    adInfo?.let {}
                }
            }
        }
    }




    override fun onPause(){
        var pref = PreferenceHelper.getInstance(requireContext())
        pref[gameIdentifier] = selectedQuesIndex + 1
        super.onPause()
    }

    override fun onDestroyView() {
        if(::timer.isInitialized) timer.cancel()
        teamId?.let {
            selectedQuestion?.let { sq ->
                var pref = PreferenceHelper.getInstance(requireContext())
                pref[gameIdentifier] = selectedQuesIndex + 1
                saveTriviaAnswer(it , sq._id!! , -1){
                    mainActivityViewmodel.remainingTime.value = null
//                    findNavController().popBackStack(R.id.gameTeamFragment , false)
                }
            }
        }
        super.onDestroyView()
    }


    @Composable
    fun triviaQuestionsSection(adId: String, questionIndex: Int = 0 , pref : SharedPreferences , gameId : String) {
        val questions by mainActivityViewmodel.getTriviaQuestions(adId).observeAsState()
        var selectedQuestionIndex by remember { mutableStateOf(questionIndex) }
        var selectedChoice by remember { mutableStateOf(-1)}

        Log.i("quizquestion" , "${questions?.size} $selectedQuestionIndex " )

        questions?.let {totalQ ->
            totalQuestions = totalQ.size
            binding.quizProgressbar.isVisible = false
            showAsMainError = false
            if(selectedQuestionIndex == 1000 || selectedQuestionIndex >= totalQ.size){
                if(isUserPlayedTheGame == null){
                    Toast.makeText(requireContext() , "Not played $isUserPlayedTheGame" , Toast.LENGTH_LONG).show()
                    teamId?.let {
                        teamViewModel.completeTriviaGame(it).observe(viewLifecycleOwner){
                            it?.let {
                                pref.delete(gameId)
                                if(adInfo != null) (requireActivity() as MainActivity).movetoCompleteGame(it , adInfo!!)
                            }
                        }
                    }
                }

                requireActivity().showDialog(getString(R.string.sorry) , stringResource(R.string.you_already_game) , autoDismiss = false){
                    findNavController().popBackStack(R.id.gameTeamFragment , false)
                }
            }
            else{
                selectedQuestion = totalQ[selectedQuestionIndex]
                timer = mainActivityViewmodel.controlTimerForGames(selectedQuestion!!.time?.times(1000)?.toLong() ?: 30000L)
                triviaQuestionComponent(
                    index = selectedQuestionIndex,
                    totalQuestions = totalQ.size,
                    selectedQuestion!!,
                    selectedChoice
                ){answer ->
                    timer.cancel()
//                    Toast.makeText(requireContext() , "$answer" , Toast.LENGTH_SHORT).show()
                    binding.quizProgressbar.isVisible = true
                    //call api to save answe
                    teamId?.let {
                        saveTriviaAnswer(it , selectedQuestion!!._id!! , answer){
                            selectedQuesIndex = selectedQuestionIndex + 1
                            if(selectedQuestionIndex < totalQ.size -1){
                                selectedQuestionIndex +=1
                            }
                            else if(selectedQuestionIndex == totalQ.size -1){
                                teamViewModel.completeTriviaGame(it).observe(viewLifecycleOwner){
                                    it?.let {
                                        pref.delete(gameId)
                                        if(adInfo != null) (requireActivity() as MainActivity).movetoCompleteGame(it , adInfo!!)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }



    fun saveTriviaAnswer(teamId : String , qId : String , answerIndex : Int , onSuccess : ()-> Unit){
        binding.quizProgressbar.isVisible = true
        teamViewModel.saveTriviaAnswer(teamId , qId , answerIndex).observe(viewLifecycleOwner){
            binding.quizProgressbar.isVisible= false
            if(it == true) onSuccess()
            else Toast.makeText(requireContext() , "Unable to save quiz answer" , Toast.LENGTH_SHORT).show()
        }
    }



    @Composable
    fun triviaQuestionComponent(
        index: Int,
        totalQuestions: Int,
        question: Trivia,
        selectedChoice : Int,
        onContinue: (answer: Int) -> Unit
    ) {
        Box(Modifier.fillMaxSize()) {
            var selectedChoiceIndex by remember { mutableStateOf(selectedChoice) }
            val remainingTime by mainActivityViewmodel.remainingTime.observeAsState()
            Column(
                Modifier
                    .fillMaxSize()
                    .alpha(if (remainingTime?.toInt() == 0) 0.3f else 1f)
                    .padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.question_number)} ${index+1} / $totalQuestions",
                        style = TextStyle(color = Color.White , fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                    remainingTime?.let {
                        Text(
                            text = DateUtils.formatElapsedTime(it.div(1000)),
                            style = TextStyle(color = Color.Green , fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                Text(
                    style = TextStyle(color = Color.White , fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp),
                    text = question.question!!
                )
                Column() {
                    question.choice?.let { choice ->
                        repeat(choice.size) {
                            QuestionChoiceComponent(
                                index = it,
                                selectedChoiceIndex,
                                choice[it],
                                onChoiceSelected = {
                                    if(remainingTime!! > 0L)
                                    selectedChoiceIndex = it
                                })
                        }
                    }
                }
            }

            if(remainingTime?.equals(0L) == true){
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.Center)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp) , horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(painter = painterResource(id = R.drawable.ic_outline_emoji_events_24), contentDescription = "",
                            modifier = Modifier
                                .width(100.dp)
                                .height(100.dp))
                        Text(text = stringResource(R.string.run_out_of_time) , style = TextStyle(color = Color.White , fontWeight = FontWeight.Bold , fontSize = 18.sp))
                    }
                }
            }


            if(selectedChoiceIndex > -1 || remainingTime?.equals(0L) == true){
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
                ) {
                    Button(onClick = {
                        onContinue(selectedChoiceIndex)
                        selectedChoiceIndex = -1
                    }) {
                        Text(
                            text = stringResource(R.string.continue_text),
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}




@Composable
fun QuestionChoiceComponent(index: Int, selectedChoice : Int = -1, choice: String, onChoiceSelected: (answer: Int) -> Unit) {

    var list = listOf("A", "B", "C", "D", "E", "F", "G", "H")
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (index == selectedChoice) Color.Green else Color.Black)
            .clickable { onChoiceSelected(index) }) {
        Row(
            Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = list[index],
                style = TextStyle(color = Color.White , fontSize = 18.sp)
            )
            Text(color = Color.White , text = choice, style = TextStyle(fontSize = 18.sp))

        }

    }
}

