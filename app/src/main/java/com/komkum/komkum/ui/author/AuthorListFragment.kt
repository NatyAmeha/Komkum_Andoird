package com.komkum.komkum.ui.author

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Author
import com.komkum.komkum.data.model.RecyclerViewHelper
import com.komkum.komkum.databinding.FragmentAuthorListBinding
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthorListFragment : Fragment() , IRecyclerViewInteractionListener<Author<String, String>> {

    lateinit var binding : FragmentAuthorListBinding
    val authorViewmodel : AuthorViewModel by viewModels()

    var authorListType : Int = -1
    var toolbarTitle : String? = null

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {

                authorListType = it.getInt("AUTHOR_LIST_TYPE")

                when(authorListType){
                    Author.FAVORITE_AUTHORS_LIST -> {
                        toolbarTitle = "Favorite Authors"
                        authorViewmodel.getUserFavoriteAuthors()

                    }
                    Author.PREV_AUTHORS_LIST ->{
                        toolbarTitle = "Authors"
                        it.getParcelableArrayList<Author<String,String>>("AUTHOR_LIST")?.let {
                            authorViewmodel.authorsResult.value = it
                        }
                    }
                    else ->{}
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAuthorListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , toolbarTitle)
        var authorInfo = RecyclerViewHelper(interactionListener = this , owner = viewLifecycleOwner ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.authorInfo = authorInfo
        binding.authorViewmodel = authorViewmodel

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

    override fun onItemClick(data: Author<String, String>, position: Int, option: Int?) {
        (requireActivity() as MainActivity).movetoAuthor(data._id!!)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}


}