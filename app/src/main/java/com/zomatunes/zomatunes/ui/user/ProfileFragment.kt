package com.zomatunes.zomatunes.ui.user

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.data.model.User
import com.zomatunes.zomatunes.data.model.UserWithSubscription
import com.zomatunes.zomatunes.databinding.FragmentProfileBinding
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    lateinit var binding : FragmentProfileBinding
    val userViewmodel : UserViewModel by viewModels()
    var user : UserWithSubscription? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        arguments?.let {
            user = it.getParcelable("USER")
        }
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        configureActionBar(toolbar)
        binding.usernameEditText.setText(user?.username)
        binding.emailEditText.setText(user?.email)
        binding.phoneNumberEditText.setText(user?.phoneNumber)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profie_save_menu , menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.profile_save_menu_item -> {
                updateUserInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    fun updateUserInfo(){
        var email = binding.usernameEditText.text.toString()
        var phoneNumber = binding.phoneNumberEditText.text.toString()
        var username = binding.usernameEditText.text.toString()

        user?.let {
            it.username = username
            it.email = email
            it.phoneNumber = phoneNumber

            userViewmodel.updateUserInfo(it).observe(viewLifecycleOwner){
                if(it) Toast.makeText(requireContext() , "profile updated" , Toast.LENGTH_SHORT).show()
                else Toast.makeText(requireContext() , "unable to update profile" , Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }

    }


}