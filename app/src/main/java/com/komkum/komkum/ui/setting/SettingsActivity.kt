package com.komkum.komkum.ui.setting

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.komkum.komkum.ControllerActivity
import com.komkum.komkum.Downloader.DownloadTracker
import com.komkum.komkum.Downloader.MediaDownloaderService
import com.komkum.komkum.ZomaTunesApplication
import com.komkum.komkum.R
import com.komkum.komkum.util.extensions.configureActionBar
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.komkum.komkum.MainActivity
import com.novoda.downloadmanager.ConnectionType
import com.komkum.komkum.databinding.SettingsActivityBinding
import com.komkum.komkum.util.extensions.gotoNotificationSetting
import com.komkum.komkum.util.extensions.sendIntent
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject
class SettingsActivity :  ControllerActivity() {
    lateinit var binding : SettingsActivityBinding
    var isLanguageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
       configureActionBar(binding.settingToolbar , "Settings")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                if(isLanguageChanged){
                    var intent = Intent(this , MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else super.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if(isLanguageChanged){
            var intent = Intent(this , MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        else super.onBackPressed()
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() , Preference.OnPreferenceChangeListener {
        @Inject
        lateinit var downloadTracker: DownloadTracker

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setHasOptionsMenu(true)
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            var wifiDownloadPreference : Preference? = findPreference("download_on_wifi")
            wifiDownloadPreference?.onPreferenceChangeListener = this

            var notificationPref : Preference? = findPreference("notification")
            notificationPref?.setOnPreferenceClickListener {
                requireActivity().gotoNotificationSetting()
                true
            }

            var languagePref : ListPreference? = findPreference("language")
            languagePref?.onPreferenceChangeListener = this

        }


        override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
            return when(preference?.key){
                "download_on_wifi" -> {
                    var value = newValue as Boolean
                    if(value){
                        var requirements = Requirements(Requirements.NETWORK_UNMETERED)
                        DownloadService.sendSetRequirements(requireContext() , MediaDownloaderService::class.java , requirements , false)
                        (requireActivity().application as ZomaTunesApplication).downloadManeger.updateAllowedConnectionType(ConnectionType.UNMETERED)
                    }
                    else{
                        var requirements = Requirements(Requirements.NETWORK)
                        DownloadService.sendSetRequirements(requireContext() , MediaDownloaderService::class.java , requirements , false)
                        (requireActivity().application as ZomaTunesApplication).downloadManeger.updateAllowedConnectionType(ConnectionType.ALL)
                    }
                    true
                }
                "language"-> {
                    var value = newValue as String
                    if(value == "English"){

                        Lingver.getInstance().setLocale(requireContext() , ZomaTunesApplication.LANGUAGE_ENGLISH , "US")
                    }
                    else if(value == "Amharic"){
                        Lingver.getInstance().setLocale(requireContext() , ZomaTunesApplication.LANGUAGE_AMHARIC , "ET")
                    }
                    (requireActivity() as SettingsActivity).isLanguageChanged = true
                    true
                }
                else -> true
            }
        }



    }
}