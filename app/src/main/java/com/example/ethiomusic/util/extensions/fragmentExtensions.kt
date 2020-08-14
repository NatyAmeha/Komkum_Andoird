package com.example.ethiomusic.util.extensions

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.ethiomusic.ControllerActivity
import com.example.ethiomusic.OnboardingActivity
import com.example.ethiomusic.R
import kotlin.reflect.KClass

fun Fragment.toControllerActivity() : ControllerActivity{
    return requireActivity() as ControllerActivity
}

fun <T : ControllerActivity> Fragment.sendIntent(activity: Class<T>){
    var intent = Intent(context , activity)
//    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    startActivity(intent)
}

fun <T : ControllerActivity> AppCompatActivity.sendIntent(activity: Class<T>){
    var intent = Intent(this , activity)
//    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and  Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
}

fun Fragment.configureActionBar(toolbar : Toolbar , title : String ?= "" , subtitle : String ?= null){
    var activity = requireActivity() as AppCompatActivity
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.let {
        it.title = title
        it.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_black_24dp)
        it.setDisplayHomeAsUpEnabled(true)

    }
}


fun Activity.configureActionBar(toolbar : Toolbar, title : String ?= "", subtitle : String ?= null){
    var activity = this as AppCompatActivity
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.let {
        it.title = title
        it.setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_black_24dp)
        it.setDisplayHomeAsUpEnabled(true)

    }
}