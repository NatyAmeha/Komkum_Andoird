package com.example.ethiomusic.ui.user

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ethiomusic.MainActivityViewmodel
import com.example.ethiomusic.OnboardingActivity
import com.example.ethiomusic.R
import com.example.ethiomusic.ui.setting.SettingsActivity
import com.example.ethiomusic.util.extensions.sendIntent
import com.example.ethiomusic.util.extensions.toControllerActivity
import com.facebook.login.LoginManager
import kotlinx.android.synthetic.main.user_fragment.*

class UserFragment : Fragment() {

    companion object {
        fun newInstance() = UserFragment()
    }

    private lateinit var viewModel: UserViewModel
//    val mainactivityViewmodel : MainActivityViewmodel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        this.toControllerActivity().hideBottomView()
        appsetting_layout.setOnClickListener {
            sendIntent(SettingsActivity::class.java)
        }

        logout_btn.setOnClickListener {
            this.toControllerActivity().Logout(requireContext())
//            mainactivityViewmodel.disconnectToService()
             LoginManager.getInstance().logOut()
            sendIntent(OnboardingActivity::class.java)
        }

        download_nav_btn.setOnClickListener {
            findNavController().navigate(R.id.downloadListFragment)
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        // TODO: Use the ViewModel
    }

}