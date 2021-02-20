package com.zomatunes.zomatunes.ui.setting

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.zomatunes.zomatunes.ControllerActivity
import com.zomatunes.zomatunes.Downloader.DownloadTracker
import com.zomatunes.zomatunes.Downloader.MediaDownloaderService
import com.zomatunes.zomatunes.ZomaTunesApplication
import com.zomatunes.zomatunes.R
import com.zomatunes.zomatunes.util.extensions.configureActionBar
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.novoda.downloadmanager.ConnectionType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.settings_activity.*
import javax.inject.Inject
class SettingsActivity :  ControllerActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
       configureActionBar(setting_toolbar , "Settings")
    }

    @AndroidEntryPoint
    class SettingsFragment : PreferenceFragmentCompat() , Preference.OnPreferenceChangeListener {
        @Inject
        lateinit var downloadTracker: DownloadTracker

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            var wifiDownloadPreference : Preference? = findPreference("download_on_wifi")
            wifiDownloadPreference?.onPreferenceChangeListener = this

        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
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
                else -> true
            }

        }

    }
}