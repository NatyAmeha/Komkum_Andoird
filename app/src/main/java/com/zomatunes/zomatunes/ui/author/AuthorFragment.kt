package com.zomatunes.zomatunes.ui.author

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.MainActivity
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.Author
import com.zomatunes.zomatunes.data.model.Book
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.databinding.AuthorFragmentBinding
import com.zomatunes.zomatunes.util.adaper.AdapterDiffUtil
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.extensions.handleSingleDataObservation
import com.zomatunes.zomatunes.util.viewhelper.CircleTransformation
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.appbar.AppBarLayout
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthorFragment : Fragment() {

    lateinit var binding : AuthorFragmentBinding
    val authorViewModel: AuthorViewModel by viewModels()

    var authorId : String? = null
    var books : List<Book<String>>? = null
    var audiobooks : List<Book<String>>? = null
    var isAuthorInFavorite = false

    init {
        lifecycleScope.launchWhenCreated {
            arguments?.let {
                authorId = it.getString("AUTHOR_ID")
                isAuthorInFavorite = authorViewModel.isAuthorInFavorite(authorId!!) ?: false
            }
            authorId?.let { authorViewModel.getAuthorData(it) }
        }
    }

    override fun onAttach(context: Context) {
        setHasOptionsMenu(true)
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding = AuthorFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.appbar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if(verticalOffset == 0) binding.toolbar.visibility = View.INVISIBLE
            else if(verticalOffset == -appBarLayout.totalScrollRange) binding.toolbar.visibility = View.VISIBLE
        })

        authorId?.let {

            authorViewModel.authorMetadataResult.observe(viewLifecycleOwner , Observer{
                requireActivity().configureActionBar(binding.toolbar , it.author?.name)
                books = it.author?.books
                audiobooks = it.author?.audiobooks
                Picasso.get().load(it.author?.profileImagePath?.replace("localhost" , AdapterDiffUtil.URL))
                    .transform(CircleTransformation()).placeholder(R.drawable.circularimg).fit().centerCrop().into(binding.authorProfileImageview)
            })
        }

        var bookInteractionListener = object : IRecyclerViewInteractionListener<Book<String>>{
            override fun onItemClick(data: Book<String>, position: Int, option: Int?) {
                var bundle = bundleOf("BOOK_ID" to data._id , "LOAD_FROM_CACHE" to false )
                if(data.format == Book.AudiobookFormat) findNavController().navigate(R.id.audiobookSellFragment , bundle)
                else findNavController().navigate(R.id.EBookSellFragment , bundle)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}
        }
        var authorInteractionListener = object : IRecyclerViewInteractionListener<Author<String,String>>{
            override fun onItemClick(data: Author<String,String>, position: Int, option: Int?) {
                (requireActivity() as MainActivity).movetoAuthor(data._id!!)
            }
            override fun activiateMultiSelectionMode() {}
            override fun onSwiped(position: Int) {}
            override fun onMoved(prevPosition: Int, newPosition: Int) {}

        }
        var bookInfo = RecyclerViewHelper<Book<String>>("BOOK" , interactionListener = bookInteractionListener , owner = viewLifecycleOwner)
        var authorInfo = RecyclerViewHelper<Author<String,String>>(interactionListener = authorInteractionListener , owner = viewLifecycleOwner ,
            layoutOrientation = RecyclerViewHelper.RECYCLERVIEW_ORIENTATION_GRID_3)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.authorViewmodel = authorViewModel
        binding.bookInfo = bookInfo
        binding.authorInfo = authorInfo

        binding.authorMoreAbookImageview.setOnClickListener {
            audiobooks?.let { it1 -> (requireActivity() as MainActivity).showBookList(it1,"Audiobooks" , false) }
        }

        binding.authorMoreBooksImageview.setOnClickListener {
            books?.let { it1 -> (requireActivity() as MainActivity).showBookList(it1,"Books" , false) }
        }

        authorViewModel.error.observe(viewLifecycleOwner , Observer{
            Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
        })
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.author_menu , menu)
    }


    override fun onPrepareOptionsMenu(menu: Menu) {
        if(isAuthorInFavorite) menu.removeItem(R.id.follow_author_menu_item)
        else menu.removeItem(R.id.unfollow_author_menu_item)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.follow_author_menu_item -> {
                authorViewModel.followAuthor(authorId!!).handleSingleDataObservation(viewLifecycleOwner){
                    if(it){
                        isAuthorInFavorite = true
                        requireActivity().invalidateOptionsMenu()
                        (requireActivity() as MainActivity).showSnacbar("Author added to Library")
                    }
                    else  Toast.makeText(requireContext() , "Error occured please try again" , Toast.LENGTH_SHORT).show()
                }
                true
            }
            R.id.unfollow_author_menu_item ->{
                authorViewModel.unfollowAuthor(authorId!!).handleSingleDataObservation(viewLifecycleOwner){
                    if(it){
                        isAuthorInFavorite = false
                        requireActivity().invalidateOptionsMenu()
                        (requireActivity() as MainActivity).showSnacbar("Author removed from Library")
                    }
                    else  Toast.makeText(requireContext() , "Error occured please try again" , Toast.LENGTH_LONG).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}