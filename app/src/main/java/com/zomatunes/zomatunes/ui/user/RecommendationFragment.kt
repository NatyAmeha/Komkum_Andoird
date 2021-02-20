package com.zomatunes.zomatunes.ui.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.data.model.MusicBrowse
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.data.model.UserWithSubscription
import com.zomatunes.zomatunes.databinding.FragmentRecommendationBinding
import com.zomatunes.zomatunes.ui.onboarding.CategorySelectionAdapter
import com.zomatunes.zomatunes.util.adaper.BrowseCategoryAdapter
import com.zomatunes.zomatunes.util.extensions.handleError
import com.zomatunes.zomatunes.util.extensions.handleListDataObservation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.zomatunes.zomatunes.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecommendationFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {

    lateinit var binding : FragmentRecommendationBinding
    var musicInteractionListener: IRecyclerViewInteractionListener<String>
    val userViewmodel : UserViewModel by viewModels()
    val stateManager : RecyclerviewStateManager<MusicBrowse> by lazy {
        RecyclerviewStateManager<MusicBrowse>()
    }

    val musicCategoryStateManager : RecyclerviewStateManager<String> by lazy {
        RecyclerviewStateManager<String>()
    }

    var user : UserWithSubscription? = null
    var bookCategoryList = emptyList<MusicBrowse>()

    init {


        musicInteractionListener = object : IRecyclerViewInteractionListener<String>{
            override fun onItemClick(data: String, position: Int, option: Int?) {
                musicCategoryStateManager.addOrRemoveItem(data)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.let {
            user = it.getParcelable("USER")
        }
        binding = FragmentRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var bookCategoryInfo = RecyclerViewHelper("" , stateManager , this , viewLifecycleOwner)


        userViewmodel.getBookIntereset().observe(viewLifecycleOwner , Observer{ interest ->
            interest?.let {
                binding.progressBar3.isVisible = false
                binding.recommendationContainer.visibility = View.VISIBLE
                binding.doneBtn.visibility = View.VISIBLE

                var categoryMap = it.filter { browse -> browse.contentType == "BOOK" }?.groupBy { musicbrowse -> musicbrowse.category }
                bookCategoryList = categoryMap["GENRE"] ?: emptyList()
                var browseAdapter = BrowseCategoryAdapter(bookCategoryInfo , categoryMap)
                binding.bookCategoryRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                binding.bookCategoryRecyclerview.adapter = browseAdapter

                user?.bookGenre?.forEach { genre ->
                    bookCategoryList.find { browse -> browse.name.equals(genre , true)}?.let {
                        stateManager.addOrRemoveItem(it)
                    }
                }
            }
        })

        user?.let {
            it.category?.forEach { category -> musicCategoryStateManager.addOrRemoveItem(category) }
        }

        var musicCategoryList = listOf("SECULAR" , "GOSPEL")
        var categoryAdapter = CategorySelectionAdapter(musicCategoryList , musicCategoryStateManager , musicInteractionListener , viewLifecycleOwner)
        binding.musicCategoryRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.musicCategoryRecyclerview.adapter = categoryAdapter



        binding.doneBtn.setOnClickListener {
            var selectedMusicCategory = musicCategoryStateManager.multiselectedItems.value
            var selectedBookCategory = stateManager.multiselectedItems.value?.map { browse -> browse.name!! }
            user?.category = selectedMusicCategory
            user?.bookGenre = selectedBookCategory
            userViewmodel.updateUserInfo(user!!).observe(viewLifecycleOwner){
                if(it) Toast.makeText(requireContext() , "preference saved" , Toast.LENGTH_SHORT).show()
                else Toast.makeText(requireContext() , "unable to save preference" , Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

        userViewmodel.getError().observe(viewLifecycleOwner){}
            userViewmodel.error.observe(viewLifecycleOwner){error ->
                error.handleError(requireContext()){
                    binding.recommendationErrorTextview.isVisible = true
                    binding.progressBar3.isVisible = false
                }
            }

    }



    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        stateManager.addOrRemoveItem(data)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}