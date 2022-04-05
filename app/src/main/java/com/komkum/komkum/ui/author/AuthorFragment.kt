package com.komkum.komkum.ui.author

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.databinding.AuthorFragmentBinding
import com.komkum.komkum.util.adaper.AdapterDiffUtil
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.handleSingleDataObservation
import com.komkum.komkum.util.viewhelper.CircleTransformation
import com.komkum.komkum.util.viewhelper.IRecyclerViewInteractionListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.squareup.picasso.Picasso
import com.komkum.komkum.data.model.*
import com.komkum.komkum.util.extensions.handleDescriptionState
import com.komkum.komkum.util.viewhelper.RadioSpan
import dagger.hilt.android.AndroidEntryPoint
import lt.neworld.spanner.Spanner
import lt.neworld.spanner.Spans

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
            authorViewModel.authorMetadataResult.observe(viewLifecycleOwner , Observer{authorMetadat ->
                authorMetadat?.let {
                    requireActivity().configureActionBar(binding.toolbar , it.author?.name)

                    books = it.author?.books
                    audiobooks = it.author?.audiobooks

                    var description = "${books?.size?.plus(audiobooks?.size ?: 0)} titles span ${it.author?.followersId?.size ?: 0} followers"
                    var span = Spanner(description).span("span" , Spans.custom {
                        RadioSpan(requireContext() , R.drawable.tab_item_not_active , resources.getInteger(R.integer.bullet_margin))
                    })
                    binding.donateAuthorBtn.isVisible = it.author?.donationEnabled ?: true
                    binding.authorFollowerTextview.text = span
                    Picasso.get().load(it.author?.profileImagePath?.replace("localhost" , AdapterDiffUtil.URL))
                        .transform(CircleTransformation()).placeholder(R.drawable.ic_person_black_24dp).fit().centerCrop().into(binding.authorProfileImageview)

                    it.author?.description?.let { it1 -> binding.authorBioTextview.handleDescriptionState(it1, 3) }

                    var chipGroup = binding.authorCategoryChipgroup
                    chipGroup.removeAllViews()
                    it.author?.genre?.forEach { genre ->
                        var chip = Chip(requireContext())
                        chip.text = genre
                        chip.setOnClickListener {
                            var data = MusicBrowse(genre , "GENRE" , MusicBrowse.CONTENT_TYPE_BOOK , queryInfo = BrowseQueryInfo(genre = genre))
                            var bundle = bundleOf("BROWSE" to data)
                            findNavController().navigate(R.id.bookBrowseFragment , bundle)
                        }
                        chipGroup.addView(chip)
                    }

                    binding.donateAuthorBtn.setOnClickListener {
                        (requireActivity() as MainActivity).moveToDonation(Donation.AUTHOR_DONATION , authorMetadat.author?.user!! , authorMetadat.author!!.name!! , authorId!! , authorMetadat.author!!.profileImagePath!!)
                    }

                } })


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
            binding.authorLoadingProgressbar.isVisible = false
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
            android.R.id.home -> {
                findNavController().navigateUp()
                true
            }

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