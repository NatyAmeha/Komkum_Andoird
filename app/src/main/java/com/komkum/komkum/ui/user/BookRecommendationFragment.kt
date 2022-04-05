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
import com.komkum.komkum.R
import com.komkum.komkum.data.model.MusicBrowse
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.data.model.UserWithSubscription
import com.komkum.komkum.databinding.FragmentBookRecommendationBinding
import com.komkum.komkum.util.adaper.BrowseCategoryAdapter
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.komkum.komkum.util.viewhelper.RecyclerviewStateManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookRecommendationFragment : Fragment()  , IRecyclerViewInteractionListener<MusicBrowse> {
    lateinit var binding : FragmentBookRecommendationBinding

    var user : UserWithSubscription? = null
    var browseLists : List<MusicBrowse> = emptyList()
//    var selectedBookCategories : MutableList<String>? = null

    val bookdRecommendationStatemanager : RecyclerviewStateManager<MusicBrowse> by lazy {
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
        binding = FragmentBookRecommendationBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        user?.let {usr ->
            browseLists.let{
                var browseInfo = RecyclerViewHelper(interactionListener = this , stateManager = bookdRecommendationStatemanager , owner = viewLifecycleOwner)
                var browseList = it.groupBy { browse -> browse.contentType }

                browseList[MusicBrowse.CONTENT_TYPE_BOOK]?.let { browseList ->
                    var list = browseList.groupBy { browse -> browse.category }
                    var categoryBrowseAdapter = BrowseCategoryAdapter(browseInfo , list)
                    binding.booksCategoryRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.booksCategoryRecyclerview.adapter = categoryBrowseAdapter

                    usr.bookGenre?.forEach { genre ->
                        browseList.find { browse -> browse.name.equals(genre , true)}?.let {
                            bookdRecommendationStatemanager.addOrRemoveItem(it)
                        }
                    }
                }
            }
        }
    }

    override fun onItemClick(data: MusicBrowse, position: Int, option: Int?) {
        var initialSize = bookdRecommendationStatemanager.multiselectedItems.value?.size ?: 0
        var currentSize = bookdRecommendationStatemanager.addOrRemoveItem(data)
        if(currentSize > initialSize) user?.bookGenre?.add(data.name!!.toUpperCase())
        else user?.bookGenre?.remove(data.name!!.toUpperCase())
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}