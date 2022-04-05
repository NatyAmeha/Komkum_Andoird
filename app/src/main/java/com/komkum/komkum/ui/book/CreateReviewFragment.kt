package com.komkum.komkum.ui.book

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.facebook.login.LoginManager
import com.google.android.material.chip.Chip
import com.skydoves.balloon.showAlignRight
import com.squareup.picasso.Picasso
import com.komkum.komkum.OnboardingActivity
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Review
import com.komkum.komkum.databinding.FragmentCreateReviewBinding
import com.komkum.komkum.ui.account.AccountState
import com.komkum.komkum.ui.store.ProductViewModel
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.extensions.configureActionBar
import com.komkum.komkum.util.extensions.getBalloon
import com.komkum.komkum.util.extensions.handleError
import com.komkum.komkum.util.extensions.sendIntent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateReviewFragment : Fragment() {

    lateinit var binding : FragmentCreateReviewBinding
    val productViewodel : ProductViewModel by viewModels()
    var contentId : String? = null
    var contentType : Int? = null
    var image : String? = null
    var title : String? = null
    var subTitle : String? = null

    var tags = mutableListOf<String>()

    var incompleteReview = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
             contentId = it.getString("CONTENT_ID")
            contentType = it.getInt("CONTENT_TYPE")
            image = it.getString("IMAGE")
            title = it.getString("TITLE")
            subTitle = it.getString("SUBTITLE")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCreateReviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , getString(R.string.add_review))
        if(!productViewodel.userRepo.userManager.isLoggedIn()){
            var pref = PreferenceHelper.getInstance(requireContext())
            pref[AccountState.IS_REDIRECTION] = true
            LoginManager.getInstance().logOut()
            requireActivity().sendIntent(OnboardingActivity::class.java)
        }


        Picasso.get().load(image).fit().centerCrop().placeholder(R.drawable.music_placeholder).into(binding.reviewImageview)
        binding.reviewTitleTextview.text = title
        binding.reviewSubtitleTextview.text = subTitle

        binding.submitReviewBtn.setOnClickListener {
            createReview()
        }

        binding.ratingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
            binding.submitReviewBtn.isVisible = fl > 0f
            incompleteReview = fl > 0f
        }

        binding.commentTextview.doOnTextChanged { text, start, before, count ->
            incompleteReview = !text.isNullOrEmpty()
        }

//        binding.tagEditText.setOnEditorActionListener { textView, i, keyEvent ->
//            when(i){
//                EditorInfo.IME_ACTION_DONE -> {
//                    var text = binding.tagEditText.text.toString()
//                    if(text.isNotBlank()){
//                        binding.tagEditText.setText("")
//                        tags.add(text)
//                        var chip = Chip(requireContext())
//                        chip.text = text
//                        chip.isCloseIconVisible = true
//                        chip.setCloseIconResource(R.drawable.ic_close_black_48dp)
//                        chip.setOnClickListener { binding.tagListChipGroup.removeView(chip) }
//                        binding.tagListChipGroup.addView(chip)
//                    }
//                    true
//                }
//                else -> false
//            }
//        }


       requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
           if(incompleteReview){
               MaterialDialog(requireContext()).show {
                   title(text = getString(R.string.discard_draft))
                   positiveButton(text = getString(R.string.discard)){findNavController().navigateUp()}
                   negativeButton(text = getString(R.string.keep)){dismiss()}
               }
           }
           else findNavController().navigateUp()
       }

        productViewodel.getErrors().observe(viewLifecycleOwner){}
        productViewodel.error.observe(viewLifecycleOwner){
            it.handleError(requireContext()){
                Toast.makeText(requireContext() , it , Toast.LENGTH_LONG).show()
                binding.errorTextview.isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.homeFragment -> {
                if(incompleteReview){
                    MaterialDialog(requireContext()).show {
                        title(text = getString(R.string.discard_draft))
                        positiveButton(text = getString(R.string.discard)){findNavController().navigateUp()}
                        negativeButton(text = getString(R.string.keep)){dismiss()}
                    }
                }
                else findNavController().navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun createReview(){
        if(contentId!= null && contentType != -1 ){
            var pref = PreferenceHelper.getInstance(requireContext())
            var userId = pref.get(AccountState.USER_ID , "")
            var userName = pref.get(AccountState.USERNAME , "")
            var userImage = pref.get(AccountState.PROFILE_IMAGE , "")
            if(userId.isNotBlank() && userName.isNotBlank()){
                binding.createReviewProgressbar.isVisible = true
                var comment = binding.commentTextview.text.toString()
                var rating = binding.ratingBar.rating

                var reviewInfo = Review(null , comment , userId , userName , rating , tags = tags , reviewerImage = userImage)
                if(contentType == Review.CONTENT_TYPE_PRODUCT) reviewInfo.product = contentId
                else reviewInfo.book = contentId

                productViewodel.reviewProduct(reviewInfo).observe(viewLifecycleOwner){
                    binding.createReviewProgressbar.isVisible = false
                    findNavController().navigateUp()
                }
            }
            else{
                pref[AccountState.IS_REDIRECTION] = true
                LoginManager.getInstance().logOut()
                var intent = Intent(context , OnboardingActivity::class.java)
                requireContext().startActivity(intent)
            }
        }

    }


}