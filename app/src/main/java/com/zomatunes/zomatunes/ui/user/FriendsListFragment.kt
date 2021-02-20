package com.zomatunes.zomatunes.ui.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.RecyclerViewHelper
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.databinding.FragmentFriendsListBinding
import com.zomatunes.zomatunes.ui.account.AccountState
import com.zomatunes.zomatunes.util.PreferenceHelper
import com.zomatunes.zomatunes.util.PreferenceHelper.get
import com.zomatunes.zomatunes.util.adaper.FriendsAdapter
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.zomatunes.zomatunes.util.isVisible
import com.zomatunes.zomatunes.util.viewhelper.IRecyclerViewInteractionListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsListFragment : Fragment() , IRecyclerViewInteractionListener<User> {

    lateinit var binding : FragmentFriendsListBinding
    val userViewmodel : UserViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentFriendsListBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(binding.toolbar , "Friends")
        var pref = PreferenceHelper.getInstance(requireContext())

        var userId = pref.get(AccountState.USER_ID, "")
        if(userId.isNotBlank()){
            userViewmodel.getUserFriendsData(userId).observe(viewLifecycleOwner , Observer{
                binding.userLoadingProgressbar.visibility = View.GONE
                binding.errorTextview.isVisible = it.isEmpty()
                if(it.isNotEmpty()){
                    var userINfo = RecyclerViewHelper(type = "USER" , interactionListener = this , owner = viewLifecycleOwner)
                    var adapter = FriendsAdapter(userINfo , it)
                    binding.friendsListRecyclerview.layoutManager = LinearLayoutManager(requireContext())
                    binding.friendsListRecyclerview.adapter = adapter
                }
            })
        }

    }

    override fun onItemClick(data: User, position: Int, option: Int?) {
         var bundle = bundleOf("USER_ID" to data._id)
        findNavController().navigate(R.id.friendActivityFragment , bundle)
    }

    override fun activiateMultiSelectionMode() {}
    override fun onSwiped(position: Int) {}
    override fun onMoved(prevPosition: Int, newPosition: Int) {}
}