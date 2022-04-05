package com.komkum.komkum.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.toUpperCase
import androidx.recyclerview.widget.LinearLayoutManager
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentPodcastBinding
import com.komkum.komkum.databinding.FragmentPodcastRecommendationBinding
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PodcastRecommendationFragment : Fragment()  , IRecyclerViewInteractionListener<MusicBrowse> {
    lateinit var binding : FragmentPodcastRecommendationBinding

    var user : UserWithSubscription? = null
    var browseLists : List<MusicBrowse> = emptyList()

    val podcastRecommendationStatemanager : RecyclerviewStateManager<MusicBrowse> by lazy {
        RecyclerviewStateManager()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            user = it.getParcelable("USER")
            browseLists = it.getParcelableArrayList("BROWSE_LIST") ?: emptyList()
//            selectedBookCategories = it.getStringArrayList("BOOK_CATEGORY")?.toMutableList()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = FragmentPodcastRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        user?.let {usr ->
            browseLists.let{
                var browseInfo = RecyclerViewHelper(interactionListener = this , stateManager = podcastRecommendationStatemanager , owner = viewLifecycleOwner)
                var browseList = it.groupBy { browse -> browse.contentType }

                browseList[MusicBrowse.CONTENT_TYPE_PODCAST]?.let { browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.podcastCategoriesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.podcastCategoriesRecyclerview.adapter = categoryBrowseAdapter

                    usr.podcastCategory?.forEach { category ->
                        browseList.find { browse -> browse.name.equals(category , true)}?.let {
                            podcastRecommendationStatemanager.addOrRemoveItem(it)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var initialSize = podcastRecommendationStatemanager.multiselectedItems.value?.size ?: 0
        var currentSize = podcastRecommendationStatemanager.addOrRemoveItem(data)
        if(currentSize > initialSize) user?.podcastCategory?.add(data.name!!.toUpperCase())
        else user?.podcastCategory?.remove(data.name!!.toUpperCase())
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}