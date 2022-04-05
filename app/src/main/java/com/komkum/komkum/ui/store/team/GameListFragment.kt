package com.komkum.komkum.ui.store.team

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Ads
import com.komkum.komkum.databinding.FragmentGameListBinding
import com.komkum.komkum.ui.component.TriviaCard
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GameListFragment : Fragment() {
    lateinit var binding : FragmentGameListBinding
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var games = MutableLiveData<List<Ads>>()

    init {
        lifecycleScope.launchWhenCreated {
            games.value = mainActivityViewmodel.getGameAds(50)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentGameListBinding.inflate(inflater)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.games))
        binding.gameListComposeview.setContent {
            ZomaTunesTheme(true) {
                gameListSection()
            }
        }

        mainActivityViewmodel.getError().observe(viewLifecycleOwner){ Log.i("billing or mainerror" , it)}
        mainActivityViewmodel.error.observe(viewLifecycleOwner){error ->
            binding.gameListProgressbar.isVisible = false
            binding.errorTextview.isVisible = true
            error?.handleError(requireContext() , {mainActivityViewmodel.removeOldErrors()},
                signupSource = mainActivityViewmodel.signUpSource){
                mainActivityViewmodel.removeOldErrors()
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                if(games.value == null) binding.errorTextview.isVisible = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    @Composable
    fun gameListSection(){
        val gameAds by games.observeAsState()
        gameAds?.let {games ->
            if(games.isNullOrEmpty()){
                binding.errorTextview.text = stringResource(R.string.stay_tuned_for_games)
                binding.errorTextview.isVisible= true
            }
            binding.gameListProgressbar.isVisible = false
            LazyColumn(modifier = Modifier.fillMaxWidth()){
                itemsIndexed(games){key , game ->
                    TriviaCard(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp) , game){
                        (requireActivity() as MainActivity).moveToGameTeamDetails(game._id , backstackId = R.id.gameListFragment )
                    }
                }
            }
        }
    }

}