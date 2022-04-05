package com.komkum.komkum.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.toUpperCase
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.MainActivityViewmodel
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentMusicRecommendationBinding
import com.komkum.komkum.ui.onboarding.CategorySelectionAdapter
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MusicRecommendationFragment : Fragment() , IRecyclerViewInteractionListener<MusicBrowse> {

    lateinit var binding : FragmentMusicRecommendationBinding
    val userViewmodel : UserViewModel by viewModels()
    val mainActivityViewmodel : MainActivityViewmodel by activityViewModels()

    var user : UserWithSubscription? = null
    var browseLists : List<MusicBrowse> = emptyList()
//    var selectedCategories : MutableList<String>? = null
//    var selectedGenre : MutableList<String>? = null

    val musicCategoryStateManager : RecyclerviewStateManager<String> by lazy {
        RecyclerviewStateManager<String>()
    }

    val musicRecommendationStateManager : RecyclerviewStateManager<MusicBrowse> by lazy {
        RecyclerviewStateManager<MusicBrowse>()
    }

    var musicInteractionListener = object : IRecyclerViewInteractionListener<String>{
        override fun onItemClick(data: String, position: Int, option: Int?) {
            var initialSize = musicCategoryStateManager.multiselectedItems.value?.size ?: 0
            var currentSize = musicCategoryStateManager.addOrRemoveItem(data)
            if(currentSize > initialSize) user?.category?.add(data.toUpperCase())
            else user?.category?.remove(data.toUpperCase())
        }
        override fun activiateMultiSelectionMode() {}
        override fun onSwiped(position: Int) {}
        override fun onMoved(prevPosition: Int, newPosition: Int) {}

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        arguments?.let {
            user = it.getParcelable("USER")
            browseLists = it.getParcelableArrayList("BROWSE_LIST") ?: emptyList()
//            selectedCategories = it.getStringArrayList("MUSIC_CATEGORY")?.toMutableList()
//            selectedGenre = it.getStringArrayList("GENRE")?.toMutableList()
        }
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMusicRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        var browseInfo = RecyclerViewHelper(interactionListener = this , stateManager = musicRecommendationStateManager , owner = viewLifecycleOwner)
        user?.let { usr ->

            usr.category?.forEach { category -> musicCategoryStateManager.addOrRemoveItem(category) }

            var musicCategoryList = listOf("SECULAR" , "GOSPEL")
            var categoryAdapter = CategorySelectionAdapter(musicCategoryList , musicCategoryStateManager , musicInteractionListener , viewLifecycleOwner)
            binding.musicCategoryRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            binding.musicCategoryRecyclerview.adapter = categoryAdapter

            browseLists?.let {
                var browseList = it.groupBy { browse -> browse.contentType }
                browseList[MusicBrowse.CONTENTY_TYPE_MUSIC]?.let {browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.musicGenreRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.musicGenreRecyclerview.adapter = categoryBrowseAdapter

                    usr.musicGenre?.forEach { genre ->
                        it.find { browse -> browse.name.equals(genre , true)}?.let {
                            musicRecommendationStateManager.addOrRemoveItem(it)
                        }
                    }

                }


            }
        }


        userViewmodel.getError().observe(viewLifecycleOwner){}
        userViewmodel.error.observe(viewLifecycleOwner){error ->
            error.handleError(requireContext() , signupSource = mainActivityViewmodel.signUpSource){
                binding.errorTextview.isVisible = true
            }
        }
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var initialSize = musicRecommendationStateManager.multiselectedItems.value?.size ?: 0
        var currentSize = musicRecommendationStateManager.addOrRemoveItem(data)
        if(currentSize > initialSize) user?.musicGenre?.add(data.name!!.toUpperCase())
        else user?.musicGenre?.remove(data.name!!.toUpperCase())
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}