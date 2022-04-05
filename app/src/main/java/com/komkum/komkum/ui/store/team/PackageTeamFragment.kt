package com.komkum.komkum.ui.store.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.skydoves.balloon.showAlignTop
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.data.model.Team
import com.komkum.komkum.databinding.FragmentPackageTeamBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.component.productListItemComposable
import com.komkum.komkum.ui.theme.ZomaTunesTheme
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.getBalloon
import com.komkum.komkum.util.extensions.handleError
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PackageTeamFragment : Fragment() {

    lateinit var binding : FragmentPackageTeamBinding
    val teamViewModel : TeamViewModel by viewModels()

    var teamId : String? = null

    var backstackId : Int? = null

    var showasMainError = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            teamId = it.getString("TEAM_ID")
            backstackId = it.getInt("BACKSTACK_ID")
        }
        (requireActivity() as MainActivity).hideBottomView()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      binding = FragmentPackageTeamBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "Your package")
        teamId?.let { teamViewModel.getTeamDetails(it) }

        binding.activateTaem.showAlignTop(requireActivity()
            .getBalloon("After you add products you want to buy, You must activate the team so that the team become visible for other peoples" ,
                "ACTIVATE_PACKAGE" , 0.5f , viewLifecycleOwner ))


        teamViewModel.teamDetails.observe(viewLifecycleOwner){
            it?.let {
                if(!it.products.isNullOrEmpty()){
                    binding.activateTaem.isVisible = true
                    binding.activateTaem.showAlignTop(requireActivity()
                        .getBalloon("You need to activate this team after you add products in order to be visible to other poeples to join the team" ,
                            "ACTIVATE_PACKAGE" , 0.5f, viewLifecycleOwner))
                }
                if(it.active) findNavController().navigate(R.id.accountFragment)
            }
        }

        binding.compseView.setContent {
            ZomaTunesTheme(true) {
                packageTeamComposable()
            }
        }


        binding.activateTaem.setOnClickListener {
            showasMainError = false
            if(teamViewModel.teamDetails.value!!.products!!.size > 1){
                binding.packageTeamProgressbar.isVisible = true
                teamViewModel.teamDetails.value?.let {
//              var teamSize = it.products?.map { product -> product?. }
                    var teamSize = 24
                    teamViewModel.activateTeam(teamId!! , teamSize).observe(viewLifecycleOwner){
                        binding.packageTeamProgressbar.isVisible = true
                        if(it == true) (requireActivity() as MainActivity).movetoTeamDetails(teamId!! , backstackId = R.id.packageTeamFragment)
                        else Toast.makeText(requireContext() , getString(R.string.error_message) , Toast.LENGTH_LONG).show()
                    }
                }
            }
            else Toast.makeText(requireContext() , getString(R.string.add_more_product) , Toast.LENGTH_LONG).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if(backstackId!= null) findNavController().popBackStack(backstackId!! , false)
            else findNavController().navigateUp()
        }

        teamViewModel.getError().observe(viewLifecycleOwner){}
        teamViewModel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                binding.packageTeamProgressbar.isVisible = false
                if(showasMainError) binding.errorTextview.isVisible = true
                else (requireActivity() as MainActivity)
                    .showErrorSnacbar("${getString(R.string.error_message)}.\n${it}" , "Dismiss"){}

                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                if(backstackId!= null) findNavController().popBackStack(backstackId!! , false)
                else findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    @Preview
    @Composable
    fun packageTeamComposable(){
        val teamInfo by teamViewModel.teamDetails.observeAsState()
        val teamProducts by teamViewModel.teamProducts.observeAsState()
        teamProducts?.let {products ->
            binding.packageTeamProgressbar.isVisible = false
            controllUIState(teamInfo!!)
            Box(modifier = Modifier.fillMaxHeight()){
                Column(
                    Modifier
                        .padding(horizontal = dimensionResource(R.dimen.default_margin_start) , vertical = 16.dp)
                        .fillMaxWidth() , verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center){

                        Column{
                            Text(text = "Invite ${teamInfo?.teamSize} people" , fontWeight = FontWeight.Bold , color = Color.Green , fontSize = 18.sp)
                            Text(text = "${teamInfo?.name}" , color = Color.White , fontWeight = FontWeight.Bold, fontSize = 20.sp  )

                            Text(text = "${teamInfo?.desc}" , color = Color.White , fontSize = 16.sp , modifier = Modifier.padding(bottom = 8.dp) )
                            Button(modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 0.dp, vertical = 8.dp) ,
                                onClick = {findNavController().navigate(R.id.productSearchFragment)},) {
                                Text(text = "Add more Products" , textAlign = TextAlign.Center)
                            }
                        }

                    }

                    Column(Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "${getString(R.string.products)}" , fontWeight = FontWeight.Bold , color = Color.White , fontSize = 20.sp)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp) ){
                            itemsIndexed(products){index, product ->
                                productListItemComposable(
                                    onclick = {},
                                    title = product.title!!,
                                    price = product.dscPrice!!.toInt(),
                                    image = product.gallery!![0].path ?: ""
                                ){
                                    OutlinedButton(onClick = {
                                        binding.packageTeamProgressbar.isVisible = true
                                        teamViewModel.removeProductFromTeam(teamInfo!!._id!! , product._id!!).observe(viewLifecycleOwner){
                                            binding.packageTeamProgressbar.isVisible = false
                                            teamViewModel.removeProductFromList(index)
                                        }
                                    }) {
                                        Text(text = stringResource(R.string.remove_from_package))
                                    }
                                }

                            }
                        }

                    }
                }
            }
        }

    }


    fun controllUIState(teamInfo : Team<Product>){
        var pref = PreferenceHelper.getInstance(requireContext())
        var userId = pref.get(AccountState.USER_ID , "")
        if(userId == teamInfo.creator){
            binding.activateTaem.isVisible = true
        }
    }


}