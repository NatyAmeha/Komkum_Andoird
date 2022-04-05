package com.komkum.komkum.util.extensions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.work.impl.Schedulers.schedule
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.google.android.gms.location.*
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.skydoves.balloon.*
import com.komkum.komkum.ControllerActivity
import com.komkum.komkum.MainActivity
import com.komkum.komkum.R
import com.komkum.komkum.ZomaTunesApplication
import com.komkum.komkum.data.model.Address
import com.komkum.komkum.data.model.GeoSpacial
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.get
import com.komkum.komkum.util.PreferenceHelper.set
import com.komkum.komkum.util.notification.FcmService
import com.skydoves.balloon.overlay.BalloonOverlayRoundRect
import com.yariksoffice.lingver.Lingver
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

fun Fragment.toControllerActivity() : ControllerActivity{
    return requireActivity() as ControllerActivity
}

fun <T : ControllerActivity> Fragment.sendIntent(activity: Class<T> , signupSource : String? = FcmService.F_EPV_ORGANIC){
    var intent = Intent(context , activity)
    intent.putExtra(FcmService.F_EP_SIGN_UP_SOURCE , signupSource)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK  or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
}


fun Fragment.createdynamicLink(linktoOpen : String , metaTitle : String , desc : String , image : String , onFailure: (() -> Unit) , onSuccess: (shortLinkUrl : Uri) -> Unit) {
    var linkUrl = Uri.parse(linktoOpen)
    var dynamicLink = Firebase.dynamicLinks.shortLinkAsync {
        link = linkUrl
        domainUriPrefix = "https://komkum.page.link"
        androidParameters("com.komkum.komkum") {
            minimumVersion = 1
            fallbackUrl = Uri.parse("https://play.google.com/store/apps/details?id=com.komkum.komkum&hl=en")

        }
        iosParameters("com.komkum.ios") { }
        socialMetaTagParameters {
            title = metaTitle
            description = desc
            imageUrl = Uri.parse(image)
        }
        navigationInfoParameters {
            forcedRedirectEnabled = true
        }
    }.addOnSuccessListener { (shortLink, flowchartLink) ->
        if (shortLink != null) {
            onSuccess(shortLink)
        } else onFailure()
    }.addOnFailureListener {
        onFailure()
    }


}


fun <T : ControllerActivity> Activity.sendIntent(activity: Class<T> , signupSource : String? = FcmService.F_EPV_ORGANIC){
    var intent = Intent(this , activity)
    intent.putExtra(FcmService.F_EP_SIGN_UP_SOURCE , signupSource)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or  Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
}

fun NavController.isFragmentInBackStack(destinationId: Int) =
    try {
        getBackStackEntry(destinationId)
        true
    } catch (e: Exception) {
        false
    }

fun Fragment.configureActionBar(toolbar : Toolbar , title : String ?= "" , subtitle : String ?= null){
    var activity = requireActivity() as AppCompatActivity
    activity.setSupportActionBar(toolbar)
    activity.supportActionBar?.let {
        it.title = title
        it.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_left_24)
        it.setDisplayHomeAsUpEnabled(true)
    }
}

fun TextView.handleDescriptionState(text : String ,line : Int){
    this.text = text
    this.setOnClickListener {
        if(this.maxLines == 1000) this.maxLines = line
        else this.maxLines = 1000
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

fun Activity.showShareMenu(title : String , text: String){
    ShareCompat.IntentBuilder.from(this)
        .setChooserTitle("Share $title")
        .setText(text)
        .setType("text/plain")
        .startChooser()
}

fun Context.getBalloon(text : String, prefName : String? = null, arrowPos : Float = 0.9f , lifeyCycleOwner: LifecycleOwner? = null , showCount : Int = 1) : Balloon{
    var ballon =  createBalloon(this@getBalloon) {
        setArrowSize(10)
        setMarginLeft(10)
        setMarginRight(10)
        setWidth(BalloonSizeSpec.WRAP)
        setIsVisibleOverlay(true) // sets the visibility of the overlay for highlighting an anchor.
        setOverlayColorResource(R.color.gray_background) // background color of the overlay using a color resource.
        setOverlayShape(BalloonOverlayRoundRect(8f , 8f))
        setOverlayPadding(6f)
        setDismissWhenTouchOutside(false)
        setDismissWhenLifecycleOnPause(true)
        setLayout(R.layout.recommendation_tooltip_custom_view)
        setHeight(BalloonSizeSpec.WRAP)
        setArrowPosition(arrowPos)
        setCornerRadius(6f)
        setAlpha(1f)
        setPadding(10)
        setArrowOrientation(ArrowOrientation.TOP)
        setTextIsHtml(true)
        setBackgroundColorResource(R.color.light_blue_900)
        // sets preference name of the Balloon.
        if(prefName != null){
            setPreferenceName(prefName)
            setShowCounts(showCount)
        }
        setBalloonAnimation(BalloonAnimation.FADE)
        if(lifeyCycleOwner != null) setLifecycleOwner(lifeyCycleOwner)
    }

    var button = ballon.getContentView().findViewById<Button>(R.id.button2)
    var textview = ballon.getContentView().findViewById<TextView>(R.id.textView123)

    textview.text = text
    button.setOnClickListener { ballon.dismiss() }

    return ballon
}


 fun Context?.showDialog(title : String, message : String? = null, positiveButtonText : String = "Ok", negetiveAction : (()-> Unit)? = null, showNegative : Boolean = false,
                         autoDismiss : Boolean = true, isBottomSheet : Boolean = false,
                         owner : LifecycleOwner? = null, checkBoxPrompt : Boolean = false,
                         positiveAction : () -> Unit) : MaterialDialog?{
     var dialog = if(isBottomSheet) this?.let { MaterialDialog(it, BottomSheet(LayoutMode.WRAP_CONTENT)) }
     else this?.let { MaterialDialog(it) }
     dialog?.show {
         title(text = title)
         if(message != null) message(text = message)
         cancelOnTouchOutside(autoDismiss)
//         lifecycleOwner(owner)
         cornerRadius(literalDp = 14f)
         positiveButton(text = positiveButtonText){ positiveAction()}
         if(showNegative){
             negativeButton(text = this@showDialog?.getString(R.string.cancel)) {
                 if (negetiveAction != null) {
                     negetiveAction()
                 }
                 it.dismiss() }
         }
         if(checkBoxPrompt){
             checkBoxPrompt(R.string.dont_show_again){isChecked ->
                 this@showDialog?.let{
                     var pref = PreferenceHelper.getInstance(it)
                     pref[PreferenceHelper.SHOW_SHARE_DIALOG] = !isChecked
                 }
             }
         }
     }
     return dialog
 }


 fun Activity.showLanguageSelelectDialog(restart : Boolean = true , delay : Long = 0 , onClick : (() -> Unit)? = null) : MaterialDialog?{
     var pref = PreferenceHelper.getInstance(this)
     var showLanguageDialog = pref.get(PreferenceHelper.FIRST_TIME_FOR_LANGUAGE_DIALOG, true)
     if (showLanguageDialog) {
         pref[PreferenceHelper.FIRST_TIME_FOR_LANGUAGE_DIALOG] = false

         var languages = listOf("English", "Amharic")
         Lingver.getInstance()
             .setLocale(this@showLanguageSelelectDialog, ZomaTunesApplication.LANGUAGE_ENGLISH, "US")

         var dialog = MaterialDialog(this@showLanguageSelelectDialog)
             .title(text = "Select Language")
             .cancelOnTouchOutside(false)
             .listItemsSingleChoice(items = languages) { dialog, index, text ->
                 when (index) {
                     0 -> Lingver.getInstance().setLocale(
                         this@showLanguageSelelectDialog,
                         ZomaTunesApplication.LANGUAGE_ENGLISH,
                         "US"
                     )
                     1 -> Lingver.getInstance().setLocale(
                         this@showLanguageSelelectDialog,
                         ZomaTunesApplication.LANGUAGE_AMHARIC,
                         "ET"
                     )
                     else -> {
                     }
                 }.also {
                     pref["language"] = languages[index]
                     onClick?.let { it() }
                     if(restart){
                         var intent = Intent(this@showLanguageSelelectDialog, MainActivity::class.java)
                         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                         startActivity(intent)
                     }
                 }
             }
         if(delay > 0){
             Timer().schedule(delay){
                 runOnUiThread {
                     dialog.show()
                 }
             }
         }
         else dialog.show()
         return dialog

     }
     else return null
 }



fun Activity.isPermissionGranted(permission : String) = ContextCompat.checkSelfPermission(this , permission) == PackageManager.PERMISSION_GRANTED

fun Activity.isPermissionGrantedBeta(permissions: List<String>) =
    permissions.all { permission -> ContextCompat.checkSelfPermission(this , permission) == PackageManager.PERMISSION_GRANTED }

fun Activity.requestPermission(permissions: List<String> , reqCode: Int){
    this.requestPermissions(permissions.toTypedArray() , reqCode)
}

fun Activity.requestPermission(permissions : List<String> , message : String = "" , reqCode : Int = 999 , onFailure: (() -> Unit)? = null , onSuccess: () -> Unit){
    if(isPermissionGrantedBeta(permissions)){
        onSuccess()
    }
    else if(this.shouldShowRequestPermissionRationale(permissions.first())){
        (this as MainActivity).showDialog(getString(R.string.warning) , message ,
            getString(R.string.grant_permission) , showNegative = true , autoDismiss = false){
            this.requestPermissions(permissions.toTypedArray() , reqCode)
        }
        if (onFailure != null) onFailure()
    }
    else{
        this.requestPermissions(permissions.toTypedArray() , reqCode)
        if (onFailure != null) onFailure()
    }
}















fun Fragment.isPermissionGrantedBetaFragment(permissions: List<String>) =
    permissions.all { permission -> this.activity?.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED }


fun Fragment.requestPermissionFragment(permissions : List<String> , message : String = "" , reqCode : Int = 999 , onFailure: (() -> Unit)? = null , onSuccess: () -> Unit){
    if(isPermissionGrantedBetaFragment(permissions)){
        onSuccess()
    }
    else if(this.shouldShowRequestPermissionRationale(permissions.first())){
        (this as MainActivity).showDialog(getString(R.string.warning) , message ,
            getString(R.string.grant_permission) , showNegative = true , autoDismiss = false){
            this.requestPermissions(permissions.toTypedArray() , reqCode)
        }
        if (onFailure != null) onFailure()
    }
    else{
        this.requestPermissions(permissions.toTypedArray() , reqCode)
        if (onFailure != null) onFailure()
    }
}


fun Fragment.requestAndAccessLocationBetaFragment(onFailure: (() -> Unit)? = null , onSuccess : (location : Location) -> Unit){
    this.requestPermissionFragment(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
        getString(R.string.locatioin_permission) , 999,
        onFailure = {
            onFailure?.invoke()
        }){
        this.activity?.let {ac ->
//            onSuccess(null)
            getLocation(ac){
                onSuccess(it)
            }
        }
    }
}
































fun Activity.handlePermissionStatus(permissions : List<String> , message : String = "" , reqCode : Int = 999 , onFailure: (() -> Unit)? = null , onSuccess: () -> Unit){
    if(isPermissionGrantedBeta(permissions)){
        onSuccess()
    }
    else if(this.shouldShowRequestPermissionRationale(permissions.first())){
        (this as MainActivity).showDialog("Warning" , message ,
            getString(R.string.grant_permission) , showNegative = true , autoDismiss = false){
            this.requestPermissions(permissions.toTypedArray() , reqCode)
        }
    }
    else{
        if (onFailure != null) onFailure()
    }
}

fun Activity.gotoPermissionSetting(){
    var intent =  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
    var uri = Uri.fromParts("package", packageName, null);
    intent.setData(uri);
    startActivity(intent)
}

fun Activity.gotoNotificationSetting(){
    var intent =  Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    startActivity(intent)
}


fun Activity.requestAndAccessLocation(onSuccess : (address : Address) -> Unit){
    this.requestPermission(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
        getString(R.string.locatioin_permission) , 999 ){
        getLocation(this){
            var address = convertLocationToAddress(this , it.longitude , it.latitude)
            onSuccess(address)
        }
    }
}

fun Activity.isLocationServiceTurnedOn() : Boolean{
    var locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return try {
        var gpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var networkProviderEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        !(!gpsProviderEnabled && !networkProviderEnabled)
    }
    catch (ex : Throwable){
        Toast.makeText(this , "Location request error. Please go to setting and change location setting" , Toast.LENGTH_LONG).show()
        false
    }
}

fun Activity.requestAndAccessLocationBeta(onFailure: ((locationEnabled : Boolean?) -> Unit)? = null , onSuccess : (location : Location) -> Unit){
    var isLocationTurnedOn = isLocationServiceTurnedOn()
    if(!isLocationTurnedOn){
        onFailure?.invoke(false)
        return
    }

    this.requestPermission(listOf(Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_COARSE_LOCATION) ,
        getString(R.string.locatioin_permission) , 999,
    onFailure = {
        onFailure?.invoke(null)
    }){
        getLocation(this){
            onSuccess(it)
        }
    }
}



fun getLocation(context : Context , onResult :  (location : Location) -> Unit){
    var locationService = LocationServices.getFusedLocationProviderClient(context)
    var locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var location = p0.lastLocation
            onResult(location)
            locationService.removeLocationUpdates(this).addOnCompleteListener {}
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            super.onLocationAvailability(p0)
        }
    }
    var request = LocationRequest.create()
    request.interval = 1
    request.fastestInterval = 0
    request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationService.requestLocationUpdates(request , locationCallback , context.mainLooper)
}

fun convertLocationToAddress(context : Context , long : Double , lat: Double) : Address{
    var latlong =  GeoSpacial(type = "Point" , coordinates = listOf(long , lat))
    var addressInfo = Address(location =  latlong)
    try{
        var locationInfo = Geocoder(context).getFromLocation(lat , long , 1).firstOrNull()

        if(locationInfo != null){
            var country = locationInfo.countryName
            var city = locationInfo.locality
            var localAddress = locationInfo.getAddressLine(0)
            addressInfo.apply {
                country = country; city = city; address = localAddress
            }
        }
        return addressInfo
    }
    catch (ex : IOException){
        Toast.makeText(context , ex.message , Toast.LENGTH_LONG).show()
        return addressInfo
    }
}

fun getDistanceBetweenLocation(userLocation: Location, teamLat: Double, teamLong: Double) : Float {
    var teamLocation = Location("TeamLocation").apply {
        latitude = teamLat
        longitude = teamLong
    }
    return userLocation.distanceTo(teamLocation)
}



fun List<String>.showListDialog(context : Context , title : String , action : (index : Int , value : String) -> Unit){
    MaterialDialog(context).show {
        title(text =  title)
        listItems(items = this@showListDialog) { dialog, index, text ->
            for (i in this@showListDialog.indices) action(i , this@showListDialog[i])
        }
    }
}

 fun Context.showCustomDialog(title : String, message: String? = null, actionText : String? = null, pos_action_String : String,
                              neg_action_String : String, negetiveAction : () -> Unit, view : Int? = R.layout.near_location_selection_custom_view,
                              bottomSheet : Boolean = true, layoutMode : LayoutMode = LayoutMode.WRAP_CONTENT, positiveAction : () -> Unit){
     var dialog = if(bottomSheet) MaterialDialog(this, BottomSheet(layoutMode))
     else MaterialDialog(this)
     dialog.cancelOnTouchOutside(true).cornerRadius(literalDp = 14f).customView(view , scrollable = true)

     var customview =  dialog.getCustomView()

    var yesButton = customview.findViewById<Button>(R.id.yes_btn)
    var noButton = customview.findViewById<Button>(R.id.no_btn)
    var titleTextView = customview.findViewById<TextView>(R.id.b_title_textview)
    var messageTextView = customview.findViewById<TextView>(R.id.message_textview)
    var actionTExtview = customview.findViewById<TextView>(R.id.action_textview)

    titleTextView?.text = title
    messageTextView?.text = message
    actionTExtview?.text = actionText
    yesButton.text = pos_action_String
    noButton.text = neg_action_String

    noButton.setOnClickListener {
        dialog.dismiss()
        negetiveAction()}
    yesButton.setOnClickListener {

       dialog.dismiss()
        positiveAction()
    }
    dialog.show()
}



