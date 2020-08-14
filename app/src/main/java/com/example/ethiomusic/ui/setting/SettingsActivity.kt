package com.example.ethiomusic.ui.setting

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.ethiomusic.ControllerActivity
import com.example.ethiomusic.Downloader.MediaDownloaderService
import com.example.ethiomusic.R
import com.example.ethiomusic.util.extensions.configureActionBar
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import kotlinx.android.synthetic.main.settings_activity.*

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

    class SettingsFragment : PreferenceFragmentCompat() , Preference.OnPreferenceChangeListener {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            var wifiDownloadPreference : Preference? = findPreference("download_on_wifi")
            wifiDownloadPreference?.onPreferenceChangeListener = this

        }

        override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
            var value = newValue as Boolean
            if(value){
                var requirements = Requirements(Requirements.NETWORK_UNMETERED)
                DownloadService.sendSetRequirements(requireContext() , MediaDownloaderService::class.java , requirements , false)
            }
            else{
                var requirements = Requirements(Requirements.NETWORK)
                DownloadService.sendSetRequirements(requireContext() , MediaDownloaderService::class.java , requirements , false)
            }
            return true
        }

    }
}